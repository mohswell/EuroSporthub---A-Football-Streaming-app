package com.sportsapp.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@Service
public class EuroSyncService {
    private static final String COMPETITION_ID = "387";
    private static final int SEASON = 2024;
    private static final String TEAM_LOGO_BASE = "https://cdn.live-score-api.com/teams/";
    private static final Set<String> TEAM_ARRAY_NAMES = Set.of("participants", "teams", "results", "data");
    private static final Set<String> GROUP_ARRAY_NAMES = Set.of("groups", "results", "data");
    private static final Set<String> SQUAD_ARRAY_NAMES = Set.of("players", "squad", "results", "data");
    private static final Set<String> FIXTURE_ARRAY_NAMES = Set.of("match", "matches", "fixtures", "results", "data");
    private static final Set<String> TABLE_ARRAY_NAMES = Set.of("table", "standings", "teams", "results", "data");

    private final JdbcTemplate jdbcTemplate;
    private final LiveScoreApiClient liveScoreApiClient;
    private final ObjectMapper objectMapper;

    public EuroSyncService(
            JdbcTemplate jdbcTemplate,
            LiveScoreApiClient liveScoreApiClient,
            ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.liveScoreApiClient = liveScoreApiClient;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> sync() {
        boolean postgres = Boolean.TRUE.equals(jdbcTemplate.execute(
            (ConnectionCallback<Boolean>) connection -> "PostgreSQL".equalsIgnoreCase(
                connection.getMetaData().getDatabaseProductName())));
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> connection.prepareStatement("""
            INSERT INTO euro_sync_runs (endpoint, status)
            VALUES ('full-euro-2024-sync', 'running')
            """, Statement.RETURN_GENERATED_KEYS), keyHolder);
        Object generatedId = keyHolder.getKeyList().getFirst().entrySet().stream()
            .filter(entry -> entry.getKey().equalsIgnoreCase("id"))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Database did not return the sync run ID"));
        Long runId = ((Number) generatedId).longValue();
        int teams = 0;
            int groups = 0;
        int matches = 0;
        int standings = 0;
            int players = 0;
        try {
            JsonNode participants = liveScoreApiClient.get("competitions/participants.json", Map.of(
                    "competition_id", COMPETITION_ID,
                    "season", Integer.toString(SEASON)));
            upsertCompetition(participants, postgres);
            teams = syncTeams(participants, postgres);

                JsonNode groupResponse = liveScoreApiClient.get("competitions/groups.json", Map.of(
                    "competition_id", COMPETITION_ID));
                groups = syncGroups(groupResponse, postgres);

                Set<String> groupIds = new LinkedHashSet<>(jdbcTemplate.queryForList(
                    "SELECT id FROM euro_groups WHERE competition_id = ? ORDER BY name",
                    String.class, COMPETITION_ID));
            for (String groupId : groupIds) {
                JsonNode table = liveScoreApiClient.get("groups/table.json", Map.of("group_id", groupId));
                standings += syncStandings(table, groupId, false, postgres);
                JsonNode liveTable = liveScoreApiClient.get("standings/live.json", Map.of(
                        "competition_id", COMPETITION_ID,
                        "group_id", groupId));
                standings += syncStandings(liveTable, groupId, true, postgres);
            }
            matches = syncHistory(postgres);
            players = syncSquads(postgres);

            Map<String, Object> result = Map.of(
                    "status", "succeeded",
                    "competitionId", COMPETITION_ID,
                    "season", SEASON,
                    "teamsUpserted", teams,
                    "groupsUpserted", groups,
                    "matchesUpserted", matches,
                    "standingsUpserted", standings,
                    "playersUpserted", players,
                    "message", "EURO 2024 participants, groups, results, standings, and available squads synchronized.");
            jdbcTemplate.update("""
                    UPDATE euro_sync_runs SET status = 'succeeded', records_upserted = ?,
                        details = ?::jsonb, finished_at = now() WHERE id = ?
                    """, teams + groups + matches + standings + players, json(result), runId);
            return result;
        } catch (RuntimeException exception) {
            jdbcTemplate.update("""
                    UPDATE euro_sync_runs SET status = 'failed', records_upserted = ?,
                        details = ?::jsonb, finished_at = now() WHERE id = ?
                    """, teams + groups + matches + standings + players,
                    json(Map.of("message", safeMessage(exception))), runId);
            throw exception;
        }
    }

    private void upsertCompetition(JsonNode payload, boolean postgres) {
        if (!postgres) {
            jdbcTemplate.update("""
                    MERGE INTO euro_competitions (id, name, season, country, provider_payload, updated_at)
                    KEY (id) VALUES (?, 'UEFA EURO 2024', ?, 'Germany', ?::jsonb, now())
                    """, COMPETITION_ID, SEASON, json(payload));
            return;
        }
        jdbcTemplate.update("""
                INSERT INTO euro_competitions (id, name, season, country, provider_payload)
                VALUES (?, 'UEFA EURO 2024', ?, 'Germany', ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, season = EXCLUDED.season,
                    country = EXCLUDED.country, provider_payload = EXCLUDED.provider_payload, updated_at = now()
                """, COMPETITION_ID, SEASON, json(payload));
    }

    private int syncGroups(JsonNode response, boolean postgres) {
        int count = 0;
        for (JsonNode row : records(response, GROUP_ARRAY_NAMES)) {
            String id = firstText(row, "id", "group_id", "groupId");
            String name = firstText(row, "name", "group_name", "groupName");
            if (id == null || name == null) {
                continue;
            }
            String displayName = groupTitle(name);
            String stage = firstText(row, "stage", "stage_name");
            if (postgres) {
                jdbcTemplate.update("""
                        INSERT INTO euro_groups (id, competition_id, name, stage, provider_payload)
                        VALUES (?, ?, ?, ?, ?::jsonb)
                        ON CONFLICT (id) DO UPDATE SET competition_id = EXCLUDED.competition_id,
                            name = EXCLUDED.name, stage = EXCLUDED.stage,
                            provider_payload = EXCLUDED.provider_payload, updated_at = now()
                        """, id, COMPETITION_ID, displayName, stage, json(row));
            } else {
                jdbcTemplate.update("""
                        MERGE INTO euro_groups (id, competition_id, name, stage, provider_payload, updated_at)
                        KEY (id) VALUES (?, ?, ?, ?, ?::jsonb, now())
                        """, id, COMPETITION_ID, displayName, stage, json(row));
            }
            count++;
        }
        return count;
    }

    private int syncHistory(boolean postgres) {
        int totalPages = 1;
        int count = 0;
        Map<String, String> groupByTeam = new HashMap<>();
        jdbcTemplate.queryForList("""
                SELECT id AS "teamId", group_id AS "groupId"
                FROM euro_teams WHERE competition_id = ?
                """, COMPETITION_ID).forEach(row -> {
            Object teamId = row.get("teamId");
            Object groupId = row.get("groupId");
            if (teamId != null && groupId != null) {
                groupByTeam.put(teamId.toString(), groupId.toString());
            }
        });
        Map<String, String> groupNames = new HashMap<>();
        jdbcTemplate.queryForList("SELECT id, name FROM euro_groups WHERE competition_id = ?", COMPETITION_ID)
                .forEach(row -> groupNames.put(row.get("id").toString(), row.get("name").toString()));

        for (int page = 1; page <= totalPages; page++) {
            JsonNode response = liveScoreApiClient.get("scores/history.json", Map.of(
                    "competition_id", COMPETITION_ID,
                    "from", "2024-06-14",
                    "to", "2024-07-14",
                    "page", Integer.toString(page)));
            if (page == 1) {
                Integer pages = firstInteger(response.path("data"), "total_pages");
                if (pages != null) {
                    totalPages = Math.min(Math.max(pages, 1), 20);
                }
            }
            count += syncFixtures(response, groupByTeam, groupNames, postgres);
        }
        return count;
    }

    private int syncTeams(JsonNode response, boolean postgres) {
        List<JsonNode> rows = records(response, TEAM_ARRAY_NAMES);
        int count = 0;
        for (JsonNode row : rows) {
            String id = firstText(row, "team_id", "id", "teamId");
            String name = firstText(row, "team_name", "name", "country_name");
            if (id == null || name == null) {
                continue;
            }
            String groupId = firstText(row, "group_id", "groupId");
            String groupName = firstText(row, "group_name", "groupName", "group");
            String logo = firstText(row, "logo_url", "logo", "image", "crest");
            if (logo == null) {
                logo = TEAM_LOGO_BASE + id + ".png";
            }
            String country = firstText(row, "country", "country_name");
            if (postgres) {
                jdbcTemplate.update("""
                        INSERT INTO euro_teams (id, competition_id, name, country, group_id, group_name, logo_url, provider_payload)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb)
                        ON CONFLICT (id) DO UPDATE SET competition_id = EXCLUDED.competition_id,
                            name = EXCLUDED.name, country = EXCLUDED.country, group_id = EXCLUDED.group_id,
                            group_name = EXCLUDED.group_name, logo_url = EXCLUDED.logo_url,
                            provider_payload = EXCLUDED.provider_payload, updated_at = now()
                        """, id, COMPETITION_ID, name, country, groupId, groupName, logo, json(row));
            } else {
                jdbcTemplate.update("""
                        MERGE INTO euro_teams (id, competition_id, name, country, group_id, group_name,
                            logo_url, provider_payload, updated_at)
                        KEY (id) VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, now())
                        """, id, COMPETITION_ID, name, country, groupId, groupName, logo, json(row));
            }
            count++;
        }
        return count;
    }

    private int syncSquads(boolean postgres) {
        List<String> teamIds = jdbcTemplate.queryForList("""
                SELECT id FROM euro_teams WHERE competition_id = ? ORDER BY name
                """, String.class, COMPETITION_ID);
        int count = 0;
        for (String teamId : teamIds) {
            JsonNode response = liveScoreApiClient.get("competitions/squads.json", Map.of(
                    "competition_id", COMPETITION_ID,
                    "team_id", teamId));
            for (JsonNode row : records(response, SQUAD_ARRAY_NAMES)) {
                String playerId = firstText(row, "id", "player_id", "playerId");
                String name = firstText(row, "name", "player_name", "playerName");
                if (playerId == null || name == null) {
                    continue;
                }
                Integer shirtNumber = firstInteger(row, "shirt_number", "shirtNumber", "number");
                String position = firstText(row, "position", "position_short", "pos");
                String nationality = firstText(row, "nationality", "country");
                String photoUrl = firstText(row, "photo_url", "photo", "image_url", "image");
                if (postgres) {
                    jdbcTemplate.update("""
                            INSERT INTO euro_players (id, team_id, name, position, shirt_number,
                                nationality, photo_url, provider_payload)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb)
                            ON CONFLICT (id) DO UPDATE SET team_id = EXCLUDED.team_id,
                                name = EXCLUDED.name, position = EXCLUDED.position,
                                shirt_number = EXCLUDED.shirt_number, nationality = EXCLUDED.nationality,
                                photo_url = EXCLUDED.photo_url, provider_payload = EXCLUDED.provider_payload,
                                updated_at = now()
                            """, playerId, teamId, name, position, shirtNumber, nationality, photoUrl, json(row));
                } else {
                    jdbcTemplate.update("""
                            MERGE INTO euro_players (id, team_id, name, position, shirt_number,
                                nationality, photo_url, provider_payload, updated_at)
                            KEY (id) VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, now())
                            """, playerId, teamId, name, position, shirtNumber, nationality, photoUrl, json(row));
                }
                count++;
            }
        }
        return count;
    }

    private int syncFixtures(
            JsonNode response,
            Map<String, String> groupByTeam,
            Map<String, String> groupNames,
            boolean postgres) {
        List<JsonNode> rows = records(response, FIXTURE_ARRAY_NAMES);
        int count = 0;
        for (JsonNode row : rows) {
            String id = firstText(row, "fixture_id", "match_id", "id");
            String homeId = firstText(row, "home_id", "home_team_id", "team1_id");
            String awayId = firstText(row, "away_id", "away_team_id", "team2_id");
            String homeName = firstText(row, "home_name", "home_team_name", "team1_name");
            String awayName = firstText(row, "away_name", "away_team_name", "team2_name");
            if (homeName == null) {
                homeName = nestedText(row, "home", "name", "home_team", "name");
            }
            if (awayName == null) {
                awayName = nestedText(row, "away", "name", "away_team", "name");
            }
            if (id == null || homeName == null || awayName == null) {
                continue;
            }

            String groupId = firstText(row, "group_id", "groupId");
            if (groupId == null) {
                String homeGroup = groupByTeam.get(homeId);
                String awayGroup = groupByTeam.get(awayId);
                if (homeGroup != null && homeGroup.equals(awayGroup)) {
                    groupId = homeGroup;
                }
            }
            String groupName = firstText(row, "group_name", "groupName");
            if (groupName == null) {
                groupName = nestedText(row, "group", "name");
            }
            if (groupName == null && groupId != null) {
                groupName = groupNames.get(groupId);
            }
            Integer homeScore = firstInteger(row, "home_score", "score_home", "team1_score");
            Integer awayScore = firstInteger(row, "away_score", "score_away", "team2_score");
            int[] scorePair = parseScore(firstText(row, "score", "ft_score", "result"));
            if (homeScore == null) {
                homeScore = scorePair[0] < 0 ? null : scorePair[0];
            }
            if (awayScore == null) {
                awayScore = scorePair[1] < 0 ? null : scorePair[1];
            }

            String status = firstText(row, "status", "match_status", "status_name");
            String stage = firstText(row, "stage", "round", "round_name");
            Timestamp kickoffAt = kickoff(row);
            if (stage == null) {
                stage = inferStage(groupId, kickoffAt);
            }
            String venue = firstText(row, "venue", "stadium", "location");
            if (postgres) {
                jdbcTemplate.update("""
                        INSERT INTO euro_matches (id, competition_id, group_id, group_name,
                            home_team_id, away_team_id, home_name, away_name, home_score, away_score,
                            status, stage, kickoff_at, venue, provider_payload)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)
                        ON CONFLICT (id) DO UPDATE SET group_id = EXCLUDED.group_id,
                            group_name = EXCLUDED.group_name, home_team_id = EXCLUDED.home_team_id,
                            away_team_id = EXCLUDED.away_team_id, home_name = EXCLUDED.home_name,
                            away_name = EXCLUDED.away_name, home_score = EXCLUDED.home_score,
                            away_score = EXCLUDED.away_score, status = EXCLUDED.status,
                            stage = EXCLUDED.stage, kickoff_at = EXCLUDED.kickoff_at,
                            venue = EXCLUDED.venue, provider_payload = EXCLUDED.provider_payload,
                            updated_at = now()
                        """, id, COMPETITION_ID, groupId, groupName, homeId, awayId, homeName, awayName,
                        homeScore, awayScore, status, stage, kickoffAt, venue, json(row));
            } else {
                jdbcTemplate.update("""
                        MERGE INTO euro_matches (id, competition_id, group_id, group_name,
                            home_team_id, away_team_id, home_name, away_name, home_score, away_score,
                            status, stage, kickoff_at, venue, provider_payload, updated_at)
                        KEY (id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, now())
                        """, id, COMPETITION_ID, groupId, groupName, homeId, awayId, homeName, awayName,
                        homeScore, awayScore, status, stage, kickoffAt, venue, json(row));
            }
            count++;
        }
        return count;
    }

    private int syncStandings(JsonNode response, String groupId, boolean live, boolean postgres) {
        List<JsonNode> rows = records(response, TABLE_ARRAY_NAMES);
        int count = 0;
        String responseGroupName = firstText(response.path("data").path("group"), "name");
        String normalizedGroupName = responseGroupName == null ? null : groupTitle(responseGroupName);
        for (JsonNode row : rows) {
            String teamId = firstText(row, "team_id", "id", "teamId");
            String teamName = firstText(row, "team_name", "name");
            if (teamId == null) {
                teamId = nestedText(row, "team", "id");
            }
            if (teamName == null) {
                teamName = nestedText(row, "team", "name");
            }
            if (teamId == null && teamName != null) {
                List<String> foundIds = jdbcTemplate.queryForList("""
                        SELECT id FROM euro_teams WHERE competition_id = ? AND lower(name) = lower(?) LIMIT 1
                        """, String.class, COMPETITION_ID, teamName);
                if (!foundIds.isEmpty()) {
                    teamId = foundIds.getFirst();
                }
            }
            if (teamId == null) {
                continue;
            }
            String groupName = firstText(row, "group_name", "groupName");
            if (groupName == null) {
                groupName = normalizedGroupName;
            }
            if (groupName == null) {
                groupName = jdbcTemplate.queryForList(
                        "SELECT name FROM euro_groups WHERE id = ?", String.class, groupId)
                        .stream().findFirst().orElse(null);
            }
            jdbcTemplate.update("""
                    UPDATE euro_teams SET group_id = ?, group_name = ?,
                        logo_url = coalesce(?, logo_url), updated_at = now()
                    WHERE id = ?
                    """, groupId, groupName, nestedText(row, "team", "logo"), teamId);
            Integer position = firstInteger(row, "position", "rank", "pos");
            Integer played = firstInteger(row, "played", "played_games", "matches", "p");
            Integer won = firstInteger(row, "won", "wins", "w");
            Integer drawn = firstInteger(row, "drawn", "draws", "d");
            Integer lost = firstInteger(row, "lost", "losses", "l");
            Integer goalsFor = firstInteger(row, "goals_for", "goalsFor", "goals_scored", "gf");
            Integer goalsAgainst = firstInteger(row, "goals_against", "goalsAgainst", "goals_conceded", "ga");
            Integer goalDifference = firstInteger(row, "goal_difference", "goalDifference", "goal_diff", "gd");
            Integer points = firstInteger(row, "points", "pts");
            if (postgres) {
                jdbcTemplate.update("""
                        INSERT INTO euro_group_standings (competition_id, group_id, team_id, group_name,
                            position, played, won, drawn, lost, goals_for, goals_against, goal_difference,
                            points, is_live, provider_payload)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)
                        ON CONFLICT (competition_id, group_id, team_id, is_live) DO UPDATE SET
                            group_name = EXCLUDED.group_name, position = EXCLUDED.position,
                            played = EXCLUDED.played, won = EXCLUDED.won, drawn = EXCLUDED.drawn,
                            lost = EXCLUDED.lost, goals_for = EXCLUDED.goals_for,
                            goals_against = EXCLUDED.goals_against, goal_difference = EXCLUDED.goal_difference,
                            points = EXCLUDED.points, provider_payload = EXCLUDED.provider_payload, updated_at = now()
                        """, COMPETITION_ID, groupId, teamId, groupName, position, played, won, drawn, lost,
                        goalsFor, goalsAgainst, goalDifference, points, live, json(row));
            } else {
                jdbcTemplate.update("""
                        MERGE INTO euro_group_standings (competition_id, group_id, team_id, group_name,
                            position, played, won, drawn, lost, goals_for, goals_against, goal_difference,
                            points, is_live, provider_payload, updated_at)
                        KEY (competition_id, group_id, team_id, is_live)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, now())
                        """, COMPETITION_ID, groupId, teamId, groupName, position, played, won, drawn, lost,
                        goalsFor, goalsAgainst, goalDifference, points, live, json(row));
            }
            count++;
        }
        return count;
    }

    private List<JsonNode> records(JsonNode response, Set<String> arrayNames) {
        JsonNode array = findArray(response.path("data"), arrayNames);
        if (array == null) {
            array = findArray(response, arrayNames);
        }
        if (array == null || !array.isArray()) {
            return List.of();
        }
        List<JsonNode> rows = new ArrayList<>();
        array.forEach(rows::add);
        return rows;
    }

    private JsonNode findArray(JsonNode node, Set<String> names) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isArray()) {
            return node;
        }
        if (!node.isObject()) {
            return null;
        }
        for (String name : names) {
            JsonNode child = node.get(name);
            if (child != null && child.isArray()) {
                return child;
            }
        }
        var fields = node.fields();
        while (fields.hasNext()) {
            JsonNode found = findArray(fields.next().getValue(), names);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private String firstText(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode value = node.path(name);
            if (value.isValueNode() && !value.isNull()) {
                String text = value.asText().trim();
                if (!text.isEmpty() && !"null".equalsIgnoreCase(text)) {
                    return text;
                }
            }
        }
        return null;
    }

    private String nestedText(JsonNode node, String first, String firstField, String second, String secondField) {
        String value = firstText(node.path(first), firstField);
        return value == null ? firstText(node.path(second), secondField) : value;
    }

    private String nestedText(JsonNode node, String parent, String field) {
        return firstText(node.path(parent), field);
    }

    private Integer firstInteger(JsonNode node, String... names) {
        String value = firstText(node, names);
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.replace("+", "").trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private int[] parseScore(String score) {
        if (score == null) {
            return new int[]{-1, -1};
        }
        var matcher = java.util.regex.Pattern.compile("(\\d+)\\s*[-:]\\s*(\\d+)").matcher(score);
        if (!matcher.find()) {
            return new int[]{-1, -1};
        }
        return new int[]{Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))};
    }

    private Timestamp kickoff(JsonNode row) {
        String dateTime = firstText(row, "kickoff_at", "datetime", "date_time", "date");
        String time = firstText(row, "scheduled", "time", "kickoff_time");
        if (dateTime == null) {
            return null;
        }
        if (time != null && !dateTime.contains("T") && !dateTime.contains(" ")) {
            dateTime += "T" + time;
        }
        try {
            return Timestamp.from(Instant.parse(dateTime));
        } catch (DateTimeParseException ignored) {
        }
        try {
            return Timestamp.from(OffsetDateTime.parse(dateTime).toInstant());
        } catch (DateTimeParseException ignored) {
        }
        try {
            return Timestamp.from(LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atZone(ZoneId.of("Europe/Berlin")).toInstant());
        } catch (DateTimeParseException ignored) {
        }
        try {
            return Timestamp.from(LocalDate.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE)
                    .atStartOfDay(ZoneId.of("Europe/Berlin")).toInstant());
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private String inferStage(String groupId, Timestamp kickoffAt) {
        if (groupId != null) {
            return "Group Stage";
        }
        if (kickoffAt == null) {
            return null;
        }
        LocalDate matchDate = kickoffAt.toInstant().atZone(ZoneId.of("Europe/Berlin")).toLocalDate();
        if (!matchDate.isBefore(LocalDate.of(2024, 6, 29))
                && !matchDate.isAfter(LocalDate.of(2024, 7, 2))) {
            return "Round of 16";
        }
        if (!matchDate.isBefore(LocalDate.of(2024, 7, 5))
                && !matchDate.isAfter(LocalDate.of(2024, 7, 6))) {
            return "Quarter-finals";
        }
        if (!matchDate.isBefore(LocalDate.of(2024, 7, 9))
                && !matchDate.isAfter(LocalDate.of(2024, 7, 10))) {
            return "Semi-finals";
        }
        if (matchDate.equals(LocalDate.of(2024, 7, 14))) {
            return "Final";
        }
        return "Knockout Stage";
    }

    private String json(JsonNode value) {
        if (value == null) {
            return "{}";
        }
        JsonNode sanitized = value.deepCopy();
        redactProviderLinks(sanitized);
        return sanitized.toString();
    }

    private void redactProviderLinks(JsonNode node) {
        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node;
            List<String> fields = new ArrayList<>();
            object.fieldNames().forEachRemaining(fields::add);
            for (String field : fields) {
                if (Set.of("key", "secret", "api_key", "api_secret", "h2h", "events", "next_page", "prev_page")
                        .contains(field.toLowerCase())) {
                    object.remove(field);
                } else {
                    redactProviderLinks(object.get(field));
                }
            }
        } else if (node.isArray()) {
            node.forEach(this::redactProviderLinks);
        }
    }

    private String groupTitle(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        return name.regionMatches(true, 0, "Group ", 0, 6) ? name : "Group " + name;
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ignored) {
            return "{}";
        }
    }

    private String safeMessage(RuntimeException exception) {
        if (exception instanceof org.springframework.web.server.ResponseStatusException responseException) {
            return responseException.getReason() == null ? "LiveScore sync failed" : responseException.getReason();
        }
        return "LiveScore sync failed; inspect server configuration and connectivity";
    }
}
