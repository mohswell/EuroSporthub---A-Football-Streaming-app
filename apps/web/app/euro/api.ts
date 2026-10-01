import type { Fixture, Group, Player, Standing, Summary, SyncRun, Team } from "./types";

const API_BASE = (process.env.NEXT_PUBLIC_EURO_API_URL ?? "/api/euro-2024").replace(/\/$/, "");

export async function request<T>(path: string, signal?: AbortSignal): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, { cache: "no-store", signal });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.detail ?? body?.message ?? `Euro API request failed (${response.status})`);
  }
  return response.json() as Promise<T>;
}

export function loadTournamentData(signal: AbortSignal) {
  return Promise.all([
    request<Summary>("/summary", signal),
    request<Team[]>("/teams", signal),
    request<Fixture[]>("/fixtures", signal),
    request<Group[]>("/groups", signal),
    request<SyncRun[]>("/sync-runs", signal),
  ]);
}

export function loadStandings(groupId: string, live: boolean, signal: AbortSignal) {
  return request<Standing[]>(`/groups/${encodeURIComponent(groupId)}/table?live=${live}`, signal);
}

export function loadPlayers(teamId: string, signal: AbortSignal) {
  const query = teamId ? `?teamId=${encodeURIComponent(teamId)}` : "";
  return request<Player[]>(`/players${query}`, signal);
}

export async function syncTournament() {
  const response = await fetch("/api/euro/sync", { method: "POST", cache: "no-store" });
  const payload = await response.json().catch(() => null);
  if (!response.ok) {
    throw new Error(payload?.message ?? `Sync failed (${response.status})`);
  }
  return payload as {
    teamsUpserted: number;
    groupsUpserted: number;
    matchesUpserted: number;
    standingsUpserted: number;
  };
}
