"use client";

import {
  Activity,
  CalendarDays,
  ChevronDown,
  CircleAlert,
  CloudDownload,
  Clock3,
  Flag,
  Play,
  RefreshCw,
  Search,
  Shirt,
  Shield,
  ShieldCheck,
  Users,
} from "lucide-react";
import Link from "next/link";
import { useMemo, useState } from "react";
import type { View } from "./euro/types";
import { Crest, EmptyState, formatKickoff, isLive } from "./components/EuroPrimitives";
import { useEuroData } from "./euro/useEuroData";

export default function Home() {
  const [view, setView] = useState<View>("fixtures");
  const [groupId, setGroupId] = useState("");
  const [fixtureGroupId, setFixtureGroupId] = useState("");
  const [teamId, setTeamId] = useState("");
  const [search, setSearch] = useState("");
  const [liveTable, setLiveTable] = useState(false);
  const { summary, teams, fixtures, groups, table, players, syncRuns, loading, syncing, syncMessage, error, refresh, runSync } = useEuroData({
    view, groupId, teamId, liveTable,
  });

  const teamById = useMemo(() => new Map(teams.map((team) => [team.id, team])), [teams]);
  const normalizedSearch = search.trim().toLowerCase();
  const visibleFixtures = fixtures.filter((fixture) => {
    if (fixtureGroupId && fixture.groupId !== fixtureGroupId) return false;
    if (teamId && fixture.homeTeamId !== teamId && fixture.awayTeamId !== teamId) return false;
    return !normalizedSearch || `${fixture.homeName} ${fixture.awayName} ${fixture.groupName ?? ""}`.toLowerCase().includes(normalizedSearch);
  });
  const visibleTeams = teams.filter((team) => !normalizedSearch || team.name.toLowerCase().includes(normalizedSearch));
  const featured = visibleFixtures[0];
  const featuredFixtures = useMemo(() => {
    const stageRank = (stage?: string) => {
      if (!stage) return 4;
      if (/\bfinal\b/i.test(stage)) return 0;
      if (/semi.?final/i.test(stage)) return 1;
      if (/quarter.?final/i.test(stage)) return 2;
      if (/round of 16|last 16/i.test(stage)) return 3;
      return 4;
    };
    return [...fixtures]
      .filter((fixture) => fixture.homeScore != null && fixture.awayScore != null)
      .sort((left, right) => {
        const stageDifference = stageRank(left.stage) - stageRank(right.stage);
        if (stageDifference !== 0) return stageDifference;
        const leftGoals = (left.homeScore ?? 0) + (left.awayScore ?? 0);
        const rightGoals = (right.homeScore ?? 0) + (right.awayScore ?? 0);
        if (leftGoals !== rightGoals) return rightGoals - leftGoals;
        return (left.kickoffAt ?? "").localeCompare(right.kickoffAt ?? "");
      })
      .slice(0, 3);
  }, [fixtures]);
  const latestSync = syncRuns[0];
  const viewLabels: { id: View; label: string; icon: typeof CalendarDays }[] = [
    { id: "fixtures", label: "Fixtures", icon: CalendarDays },
    { id: "groups", label: "Group tables", icon: Activity },
    { id: "teams", label: "Teams", icon: Shield },
    { id: "players", label: "Players", icon: Users },
  ];

  return (
    <main className="app-shell">
      <header className="topbar">
        <a className="brand" href="#top" aria-label="EuroSportHub home">
          <span className="brand-mark"><Flag size={19} strokeWidth={2.5} /></span>
          <span>EURO<span className="brand-light">SPORT</span><b>HUB</b></span>
        </a>
        <div className="topbar-right">
          <span className="edition"><span className="edition-dot" /> GERMANY 2024</span>
          <button className="sync-button" onClick={runSync} disabled={syncing} title="Sync EURO data from LiveScore into Supabase">
            <CloudDownload size={16} /> <span>{syncing ? "Syncing" : "Sync data"}</span>
          </button>
          <button className="refresh-button" onClick={refresh} aria-label="Refresh data" title="Refresh data">
            <RefreshCw size={17} /> <span>Refresh</span>
          </button>
        </div>
      </header>

      <section className="competition-hero" id="top">
        <div className="hero-grid" aria-hidden="true" />
        <div className="hero-copy">
          <p className="eyebrow"><span>UEFA</span> / MEN&apos;S NATIONAL TEAMS</p>
          <h1>EURO <em>2024</em></h1>
          <div className="hero-meta"><span>GERMANY</span><span className="meta-divider" /><span>14 JUN — 14 JUL</span></div>
        </div>
        <div className="hero-orbit" aria-hidden="true">
          <div className="orbit-ring orbit-ring-one" />
          <div className="orbit-ring orbit-ring-two" />
          <span className="orbit-star">✳</span>
          <span className="orbit-year">24</span>
          <span className="orbit-caption">ONE<br />GAME<br />AT A TIME</span>
        </div>
        <div className="hero-bottom">
          <span className="data-state">
            <span className={error ? "state-dot state-error" : teams.length ? "state-dot" : "state-dot state-idle"} />
            {error ? "API OFFLINE" : teams.length ? "TOURNAMENT DATA" : "AWAITING DATA SYNC"}
          </span>
          <span className="hero-stat"><b>{summary.team_count ?? teams.length}</b><small>NATIONS</small></span>
          <span className="hero-stat"><b>{summary.match_count ?? fixtures.length}</b><small>FIXTURES</small></span>
          <span className="hero-stat"><b>{summary.player_count ?? 0}</b><small>PLAYERS</small></span>
        </div>
      </section>

      <section className="content-wrap">
        <div className="section-heading">
          <div>
            <p className="eyebrow dark-eyebrow">THE TOURNAMENT</p>
            <h2>Follow every <span>moment.</span></h2>
          </div>
          <div className="last-sync"><Clock3 size={15} />
            {latestSync ? `${latestSync.status.toUpperCase()} · ${formatKickoff(latestSync.finishedAt ?? latestSync.startedAt).toUpperCase()}` : "NO SYNC YET"}
          </div>
        </div>
        {syncMessage && <div className="sync-result" role="status"><ShieldCheck size={15} /> Supabase updated: {syncMessage}</div>}

        <div className="toolbar">
          <nav className="view-tabs" aria-label="Tournament sections">
            {viewLabels.map(({ id, label, icon: Icon }) => (
              <button key={id} className={view === id ? "view-tab active" : "view-tab"} onClick={() => setView(id)} aria-pressed={view === id}>
                <Icon size={16} /><span>{label}</span>
              </button>
            ))}
          </nav>
          <label className="search-field">
            <Search size={16} />
            <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Find a team or match" aria-label="Find a team or match" />
          </label>
        </div>

        {error && (
          <div className="alert-banner" role="alert">
            <CircleAlert size={18} />
            <div><strong>Euro data is unavailable.</strong><span>{error}</span></div>
            <button onClick={refresh}>Retry</button>
          </div>
        )}

        {loading ? (
          <div className="loading-state"><span className="loading-pulse" /> Loading tournament data</div>
        ) : view === "fixtures" ? (
          <>
          {featuredFixtures.length > 0 && <section className="featured-matches" aria-label="Featured EURO matches">
            <div className="featured-heading"><div><span className="panel-kicker">TOURNAMENT SPOTLIGHT</span><h3>Featured matches</h3></div><span className="count-pill">KNOCKOUT HIGHLIGHTS</span></div>
            <div className="featured-grid">{featuredFixtures.map((fixture) => <Link className="featured-card" href={`/watch/${fixture.id}`} key={fixture.id} aria-label={`Watch page for ${fixture.homeName} versus ${fixture.awayName}`}>
              <div className="featured-card-top"><span>{fixture.stage ?? "EURO 2024"}</span><span>{fixture.groupName ?? "GERMANY"}</span></div>
              <div className="featured-card-match">
                <div><Crest team={teamById.get(fixture.homeTeamId ?? "")} label={fixture.homeName} /><strong>{fixture.homeName}</strong></div>
                <span className="featured-card-score">{fixture.homeScore}<i>:</i>{fixture.awayScore}</span>
                <div><Crest team={teamById.get(fixture.awayTeamId ?? "")} label={fixture.awayName} /><strong>{fixture.awayName}</strong></div>
              </div>
              <div className="featured-card-bottom"><span>{formatKickoff(fixture.kickoffAt)}</span><span>{fixture.venue ?? "Germany"}</span></div>
            </Link>)}</div>
          </section>}
          <div className="main-grid">
            <section className="fixtures-panel">
              <div className="panel-heading"><div><span className="panel-kicker">MATCH CENTRE</span><h3>Fixtures & results</h3></div><span className="count-pill">{visibleFixtures.length} MATCHES</span></div>
              <label className="fixture-group-filter"><span>GROUP</span><select value={fixtureGroupId} onChange={(event) => setFixtureGroupId(event.target.value)} aria-label="Filter fixtures by group"><option value="">All groups</option>{groups.map((group) => <option key={group.groupId} value={group.groupId}>{group.name}</option>)}</select></label>
              {visibleFixtures.length ? (
                <div className="fixture-list">
                  {visibleFixtures.map((fixture) => {
                    const live = isLive(fixture.status);
                    return (
                      <article className="fixture-row" key={fixture.id}>
                        <div className="fixture-time"><time>{formatKickoff(fixture.kickoffAt)}</time><span>{fixture.groupName ?? fixture.stage ?? "EURO 2024"}</span></div>
                        <div className="fixture-team home-team"><Crest team={teamById.get(fixture.homeTeamId ?? "")} label={fixture.homeName} /><strong>{fixture.homeName}</strong></div>
                        <div className={live ? "score live-score" : "score"}>
                          {fixture.homeScore == null || fixture.awayScore == null ? <span className="versus">VS</span> : <><b>{fixture.homeScore}</b><i>:</i><b>{fixture.awayScore}</b></>}
                          <small>{live ? "LIVE" : fixture.status ?? "SCHEDULED"}</small>
                        </div>
                        <div className="fixture-team away-team"><Crest team={teamById.get(fixture.awayTeamId ?? "")} label={fixture.awayName} /><strong>{fixture.awayName}</strong></div>
                        <div className="fixture-venue">{fixture.venue ?? "Germany"}</div>
                        <Link className="fixture-watch" href={`/watch/${fixture.id}`} aria-label={`Open stream page for ${fixture.homeName} versus ${fixture.awayName}`}><Play size={14} /></Link>
                      </article>
                    );
                  })}
                </div>
              ) : <EmptyState title="No fixtures stored" detail="Once the provider sync succeeds, fixtures and results will appear here." />}
            </section>
            <aside className="side-column">
              <section className="next-match-panel">
                <div className="panel-heading"><div><span className="panel-kicker">FEATURED FIXTURE</span><h3>{featured ? featured.stage ?? "EURO 2024" : "Matchday"}</h3></div><CalendarDays size={18} /></div>
                {featured ? <div className="featured-match">
                  <p>{featured.groupName ?? "GERMANY 2024"}</p>
                  <div className="featured-sides">
                    <div><Crest team={teamById.get(featured.homeTeamId ?? "")} label={featured.homeName} /><strong>{featured.homeName}</strong></div>
                    <span>VS</span>
                    <div><Crest team={teamById.get(featured.awayTeamId ?? "")} label={featured.awayName} /><strong>{featured.awayName}</strong></div>
                  </div>
                  <div className="featured-time"><Clock3 size={15} />{formatKickoff(featured.kickoffAt)}</div>
                </div> : <div className="feature-empty"><span className="feature-mark">✳</span><strong>Ready for kick-off?</strong><small>Competition data will populate this match centre.</small></div>}
              </section>
              <section className="quick-filter-panel">
                <div className="panel-heading"><div><span className="panel-kicker">NARROW IT DOWN</span><h3>Team filter</h3></div><ChevronDown size={17} /></div>
                <select value={teamId} onChange={(event) => setTeamId(event.target.value)} aria-label="Filter by team">
                  <option value="">All teams</option>
                  {teams.map((team) => <option key={team.id} value={team.id}>{team.name}</option>)}
                </select>
                <div className="filter-foot"><ShieldCheck size={15} /> {teams.length} participating nations</div>
              </section>
            </aside>
          </div>
          </>
        ) : view === "groups" ? (
          <section className="standings-panel">
            <div className="panel-heading"><div><span className="panel-kicker">ROAD TO BERLIN</span><h3>Group standings</h3></div>
              <label className="group-select"><span>GROUP</span><select value={groupId} onChange={(event) => setGroupId(event.target.value)} aria-label="Select group">
                {groups.map((group) => <option key={group.groupId} value={group.groupId}>{group.name}</option>)}
              </select><ChevronDown size={15} /></label>
            </div>
            <div className="table-mode" role="group" aria-label="Standings mode">
              <button className={!liveTable ? "mode-active" : ""} onClick={() => setLiveTable(false)}>Final table</button>
              <button className={liveTable ? "mode-active" : ""} onClick={() => setLiveTable(true)}><span className="mini-live-dot" /> Live table</button>
            </div>
            {table.length ? <div className="standings-scroll"><table className="standings-table">
              <thead><tr><th>#</th><th>TEAM</th><th>P</th><th>W</th><th>D</th><th>L</th><th>GF</th><th>GA</th><th>GD</th><th>PTS</th></tr></thead>
              <tbody>{table.map((row) => <tr key={row.teamId}><td className="position">{String(row.position ?? "-").padStart(2, "0")}</td><td><div className="table-team"><Crest team={teamById.get(row.teamId)} label={row.teamName} /><strong>{row.teamName}</strong></div></td><td>{row.played ?? 0}</td><td>{row.won ?? 0}</td><td>{row.drawn ?? 0}</td><td>{row.lost ?? 0}</td><td>{row.goalsFor ?? 0}</td><td>{row.goalsAgainst ?? 0}</td><td>{row.goalDifference ?? 0}</td><td className="points">{row.points ?? 0}</td></tr>)}</tbody>
            </table></div> : <EmptyState title="No standings stored" detail="Group tables are saved when the LiveScore standings endpoints are available." />}
          </section>
        ) : view === "teams" ? (
          <section className="teams-panel">
            <div className="panel-heading"><div><span className="panel-kicker">24 NATIONS · ONE TROPHY</span><h3>Participating teams</h3></div><span className="count-pill">{visibleTeams.length} TEAMS</span></div>
            {visibleTeams.length ? <div className="team-grid">{visibleTeams.map((team) => <button className="team-card" key={team.id} onClick={() => { setTeamId(team.id); setView("fixtures"); }}>
              <Crest team={team} label={team.name} /><span><strong>{team.name}</strong><small>{team.groupName ?? "QUALIFIED"}</small></span><span className="team-arrow">↗</span>
            </button>)}</div> : <EmptyState title="No teams stored" detail="The participant list will appear here after a successful competition sync." />}
          </section>
        ) : (
          <section className="players-panel">
            <div className="panel-heading"><div><span className="panel-kicker">EURO 2024 SQUADS</span><h3>Players <span className="roster-count">{players.length}</span></h3></div><label className="team-player-filter"><span>TEAM</span><select value={teamId} onChange={(event) => setTeamId(event.target.value)} aria-label="Filter players by team"><option value="">All teams</option>{teams.map((team) => <option key={team.id} value={team.id}>{team.name}</option>)}</select></label></div>
            {players.length ? <div className="player-list">{players.map((player) => <article className="player-row" key={player.id}>
              <span className="player-shirt" aria-label={`Shirt number ${player.shirtNumber ?? "not assigned"}`}><Shirt size={39} strokeWidth={1.5} /><b>{player.shirtNumber ?? "—"}</b></span>
              <div className="player-row-main"><strong>{player.name}</strong><span>{player.position ?? "SQUAD"}</span></div>
              <span className="player-row-team">{player.teamName ?? teamById.get(player.teamId)?.name}</span>
            </article>)}</div> : <EmptyState title="No squad players synced" detail="Use Sync data to load the verified EURO 2024 squads and shirt numbers from LiveScore." />}
          </section>
        )}

        <footer className="app-footer"><span><i /> SUPABASE DATA STORE</span><span>EUROSPORT HUB <b>·</b> GERMANY 2024</span><span>{latestSync ? `${latestSync.recordsUpserted} ROWS IN LAST SYNC` : "ALL TIMES CET / CEST"}</span></footer>
      </section>
    </main>
  );
}

