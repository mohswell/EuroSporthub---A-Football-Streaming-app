CREATE TABLE euro_groups (
    id text PRIMARY KEY,
    competition_id text NOT NULL REFERENCES euro_competitions(id) ON DELETE CASCADE,
    name text NOT NULL,
    stage text,
    provider_payload jsonb NOT NULL DEFAULT '{}'::jsonb,
    updated_at timestamp with time zone NOT NULL DEFAULT now()
);

CREATE INDEX euro_groups_competition_name_idx ON euro_groups (competition_id, name);
