
# EuroSportHub

EuroSportHub is a Next.js match-centre UI and Spring Boot API for UEFA EURO 2024. The API stores competition participants, fixtures, group tables, standings, and available player records in Supabase PostgreSQL. LiveScore credentials are used only by the server.

## Workspace

- `apps/web`: Next.js 16 dashboard (fixtures, group tables, teams, players).
- `src/main`: Spring Boot API, LiveScore client, Supabase JDBC configuration, and Flyway migrations.
- `src/main/resources/db/migration/V1__create_euro_2024_tables.sql`: creates the EURO data tables on API startup.

## Requirements

- JDK 25
- Node.js 22.23.3 (`.nvmrc`)
- npm 9+
- A Supabase PostgreSQL connection with network access from the API host
- A LiveScore API account with access enabled for the competition endpoints

## Configuration

Keep local credentials in the ignored root `.env` file. Use `.env.example` as a key-name reference; do not commit `.env` or place provider credentials in `NEXT_PUBLIC_*` variables.

Required settings:

- `LIVE_SCORE_API_KEY` and `LIVE_SCORE_API_SECRET`
- `SUPABASE_DB_URL` as a `postgresql://user:password@host:port/database` URL
- `SUPABASE_DB_PASSWORD`
- `SUPABASE_DB_HOST`, `SUPABASE_DB_PORT`, and `SUPABASE_DB_USER` may override the direct URL host/user when using Supabase's IPv4 session pooler. Use the pooler host and project-qualified username shown in Project Settings > Database.
- `EURO_SYNC_TOKEN`, a long random value required by the protected sync endpoint
- `WEB_ORIGIN`, defaulting to `http://localhost:3000`

The database connection uses SSL. Flyway baselines an existing manually created V1 schema, then applies newer migrations such as the groups table. On a fresh schema it applies all migrations. The migration set creates the competition, teams, players, matches, group standings, and sync-history tables.

## Run Locally

Start the API from the repository root:

```sh
bash ./mvnw spring-boot:run
```

Select Node 22.23.3, install the web workspace, and start Next.js:

```sh
nvm install
nvm use
npm install
npm run dev:web
```

Open `http://localhost:3000`. The browser reads the API at `http://localhost:8080/api/euro-2024` by default.

## Data Sync

The dashboard's **Sync data** command is available in development and sends the sync token through a server-only Next.js route; it is never placed in browser code. The API sync endpoint uses the verified LiveScore response modules and EURO 2024 identifiers:

- Participants: `competitions/participants.json` with `competition_id=387` and `season=2024`
- Groups: `competitions/groups.json` with `competition_id=387`
- Completed results: `scores/history.json` with `competition_id=387`, `from=2024-06-14`, and `to=2024-07-14`; the sync follows `total_pages`
- Upcoming fixtures: `fixtures/list.json`; this returns no rows for the completed EURO 2024 tournament
- Group table: `groups/table.json` with a `group_id`
- Live group table: `standings/live.json` with `competition_id=387` and a `group_id`; EURO 2024 is complete, so this can be empty
- Team crests: `https://cdn.live-score-api.com/teams/{teamId}.png`

Run a sync from a trusted operator shell after configuring `EURO_SYNC_TOKEN`:

```sh
curl -X POST http://localhost:8080/api/euro-2024/sync \
  -H "X-Sync-Token: $EURO_SYNC_TOKEN"
```

The API stores provider payloads as JSONB alongside queryable columns and records each sync attempt. Player records have a dedicated table and read endpoint; a roster sync is intentionally not guessed because the available LiveScore endpoint documentation does not confirm a squad endpoint. Enable a documented roster feed before expecting player rows.

## Read API

- `GET /api/euro-2024/summary`
- `GET /api/euro-2024/sync-runs`
- `GET /api/euro-2024/teams`
- `GET /api/euro-2024/participants`
- `GET /api/euro-2024/fixtures?groupId=2763`
- `GET /api/euro-2024/fixtures?teamId=1438`
- `GET /api/euro-2024/fixtures/group/2763`
- `GET /api/euro-2024/fixtures/team/1438`
- `GET /api/euro-2024/groups`
- `GET /api/euro-2024/groups/2763/table?live=false`
- `GET /api/euro-2024/groups/2763/standings`
- `GET /api/euro-2024/groups/2763/standings/live`
- `GET /api/euro-2024/players?teamId=1438`
- `GET /api/euro-2024/players/team/1438`

## Verify

```sh
bash ./mvnw test
npm audit
PATH=/path/to/node-22.23.3/bin:$PATH npm run build:web
```

Tests use H2 in PostgreSQL mode and apply the Flyway migration without requiring Supabase network access.
