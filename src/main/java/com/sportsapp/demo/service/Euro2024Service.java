package com.sportsapp.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class Euro2024Service {
    private final JdbcTemplate jdbcTemplate;
    private final String competitionId;

    public Euro2024Service(
            JdbcTemplate jdbcTemplate,
            @Value("${app.euro.competition-id:387}") String competitionId) {
        this.jdbcTemplate = jdbcTemplate;
        this.competitionId = competitionId;
    }

    public Map<String, Object> summary() {
        return jdbcTemplate.queryForMap("""
                SELECT
                    (SELECT count(*) FROM euro_teams WHERE competition_id = ?) AS "team_count",
                    (SELECT count(*) FROM euro_matches WHERE competition_id = ?) AS "match_count",
                    (SELECT count(*) FROM euro_players p JOIN euro_teams t ON t.id = p.team_id
                        WHERE t.competition_id = ?) AS "player_count",
                    (SELECT max(finished_at) FROM euro_sync_runs WHERE status = 'succeeded') AS "last_synced_at"
                """, competitionId, competitionId, competitionId);
    }

    public List<Map<String, Object>> syncRuns() {
        return jdbcTemplate.queryForList("""
                SELECT id AS "id", endpoint AS "endpoint", status AS "status",
                    records_upserted AS "recordsUpserted", details AS "details",
                    started_at AS "startedAt", finished_at AS "finishedAt"
                FROM euro_sync_runs
                ORDER BY id DESC
                LIMIT 20
                """);
    }

    public List<Map<String, Object>> teams() {
        return jdbcTemplate.queryForList("""
                SELECT id AS "id", name AS "name", country AS "country",
                    group_id AS "groupId", group_name AS "groupName", logo_url AS "logoUrl"
                FROM euro_teams
                WHERE competition_id = ?
                ORDER BY group_name NULLS LAST, name
                """, competitionId);
    }

    public List<Map<String, Object>> fixtures(String groupId, String teamId, String status) {
        StringBuilder sql = new StringBuilder("""
                SELECT id AS "id", group_id AS "groupId", group_name AS "groupName",
                    home_team_id AS "homeTeamId", away_team_id AS "awayTeamId",
                    home_name AS "homeName", away_name AS "awayName",
                    home_score AS "homeScore", away_score AS "awayScore",
                    status AS "status", stage AS "stage", kickoff_at AS "kickoffAt", venue AS "venue"
                FROM euro_matches WHERE competition_id = ?
                """);
        List<Object> parameters = new ArrayList<>(List.of(competitionId));
        if (groupId != null && !groupId.isBlank()) {
            sql.append(" AND group_id = ?");
            parameters.add(groupId);
        }
        if (teamId != null && !teamId.isBlank()) {
            sql.append(" AND (home_team_id = ? OR away_team_id = ?)");
            parameters.add(teamId);
            parameters.add(teamId);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND lower(status) = lower(?)");
            parameters.add(status);
        }
        sql.append(" ORDER BY kickoff_at NULLS LAST, id");
        return jdbcTemplate.queryForList(sql.toString(), parameters.toArray());
    }

    public List<Map<String, Object>> groups() {
        return jdbcTemplate.queryForList("""
                SELECT g.id AS "groupId", g.name AS "name", count(t.id) AS "team_count"
                FROM euro_groups g
                LEFT JOIN euro_teams t ON t.competition_id = g.competition_id AND t.group_id = g.id
                WHERE g.competition_id = ?
                GROUP BY g.id, g.name
                ORDER BY g.name
                """, competitionId);
    }

    public List<Map<String, Object>> table(String groupId, boolean live) {
        return jdbcTemplate.queryForList("""
                SELECT s.team_id AS "teamId", t.name AS "teamName", t.logo_url AS "logoUrl",
                    s.group_name AS "groupName", s.position AS "position", s.played AS "played",
                    s.won AS "won", s.drawn AS "drawn", s.lost AS "lost",
                    s.goals_for AS "goalsFor", s.goals_against AS "goalsAgainst",
                    s.goal_difference AS "goalDifference", s.points AS "points", s.is_live AS "isLive"
                FROM euro_group_standings s
                JOIN euro_teams t ON t.id = s.team_id
                WHERE s.competition_id = ? AND s.group_id = ? AND s.is_live = ?
                ORDER BY s.position NULLS LAST, s.points DESC, s.goal_difference DESC, t.name
                """, competitionId, groupId, live);
    }

    public List<Map<String, Object>> players(String teamId) {
        if (teamId == null || teamId.isBlank()) {
            return jdbcTemplate.queryForList("""
                    SELECT p.id AS "id", p.team_id AS "teamId", t.name AS "teamName", p.name AS "name",
                        p.position AS "position", p.shirt_number AS "shirtNumber",
                        p.nationality AS "nationality", p.photo_url AS "photoUrl"
                    FROM euro_players p
                    LEFT JOIN euro_teams t ON t.id = p.team_id
                    JOIN euro_teams competition_team ON competition_team.id = p.team_id
                    WHERE competition_team.competition_id = ?
                    ORDER BY t.name, p.shirt_number NULLS LAST, p.name
                    """, competitionId);
        }
        return jdbcTemplate.queryForList("""
                SELECT p.id AS "id", p.team_id AS "teamId", t.name AS "teamName", p.name AS "name",
                    p.position AS "position", p.shirt_number AS "shirtNumber",
                    p.nationality AS "nationality", p.photo_url AS "photoUrl"
                FROM euro_players p
                LEFT JOIN euro_teams t ON t.id = p.team_id
                WHERE p.team_id = ?
                ORDER BY p.shirt_number NULLS LAST, p.name
                """, teamId);
    }
}
