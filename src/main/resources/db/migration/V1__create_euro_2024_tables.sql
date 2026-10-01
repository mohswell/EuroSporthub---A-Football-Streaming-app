CREATE TABLE euro_competitions (
    id text PRIMARY KEY,
    name text NOT NULL,
    season integer NOT NULL,
    country text,
    logo_url text,
    provider_payload jsonb NOT NULL DEFAULT '{}'::jsonb,
    updated_at timestamp with time zone NOT NULL DEFAULT now()
);

CREATE TABLE euro_teams (
    id text PRIMARY KEY,
    competition_id text NOT NULL REFERENCES euro_competitions(id) ON DELETE CASCADE,
    name text NOT NULL,
    country text,
    group_id text,
    group_name text,
    logo_url text,
    provider_payload jsonb NOT NULL DEFAULT '{}'::jsonb,
    updated_at timestamp with time zone NOT NULL DEFAULT now()
);

CREATE INDEX euro_teams_competition_group_idx ON euro_teams (competition_id, group_id, name);

CREATE TABLE euro_players (
    id text PRIMARY KEY,
    team_id text REFERENCES euro_teams(id) ON DELETE SET NULL,
    name text NOT NULL,
    position text,
    shirt_number integer,
    nationality text,
    photo_url text,
    provider_payload jsonb NOT NULL DEFAULT '{}'::jsonb,
    updated_at timestamp with time zone NOT NULL DEFAULT now()
);

CREATE INDEX euro_players_team_name_idx ON euro_players (team_id, name);

CREATE TABLE euro_matches (
    id text PRIMARY KEY,
    competition_id text NOT NULL REFERENCES euro_competitions(id) ON DELETE CASCADE,
    group_id text,
    group_name text,
    home_team_id text REFERENCES euro_teams(id) ON DELETE SET NULL,
    away_team_id text REFERENCES euro_teams(id) ON DELETE SET NULL,
    home_name text NOT NULL,
    away_name text NOT NULL,
    home_score integer,
    away_score integer,
    status text,
    stage text,
    kickoff_at timestamp with time zone,
    venue text,
    provider_payload jsonb NOT NULL DEFAULT '{}'::jsonb,
    updated_at timestamp with time zone NOT NULL DEFAULT now()
);

CREATE INDEX euro_matches_competition_kickoff_idx ON euro_matches (competition_id, kickoff_at);
CREATE INDEX euro_matches_group_idx ON euro_matches (competition_id, group_id, kickoff_at);
CREATE INDEX euro_matches_teams_idx ON euro_matches (home_team_id, away_team_id);

CREATE TABLE euro_group_standings (
    competition_id text NOT NULL REFERENCES euro_competitions(id) ON DELETE CASCADE,
    group_id text NOT NULL,
    team_id text NOT NULL REFERENCES euro_teams(id) ON DELETE CASCADE,
    group_name text,
    position integer,
    played integer,
    won integer,
    drawn integer,
    lost integer,
    goals_for integer,
    goals_against integer,
    goal_difference integer,
    points integer,
    is_live boolean NOT NULL DEFAULT false,
    provider_payload jsonb NOT NULL DEFAULT '{}'::jsonb,
    updated_at timestamp with time zone NOT NULL DEFAULT now(),
    PRIMARY KEY (competition_id, group_id, team_id, is_live)
);

CREATE TABLE euro_sync_runs (
    id bigserial PRIMARY KEY,
    endpoint text NOT NULL,
    status text NOT NULL,
    records_upserted integer NOT NULL DEFAULT 0,
    details jsonb NOT NULL DEFAULT '{}'::jsonb,
    started_at timestamp with time zone NOT NULL DEFAULT now(),
    finished_at timestamp with time zone
);

CREATE INDEX euro_sync_runs_started_idx ON euro_sync_runs (started_at DESC);
