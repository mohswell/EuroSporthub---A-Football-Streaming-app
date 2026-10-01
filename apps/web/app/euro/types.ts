export type View = "fixtures" | "groups" | "teams" | "players";

export type Team = {
  id: string;
  name: string;
  country?: string;
  groupId?: string;
  groupName?: string;
  logoUrl?: string;
};

export type Fixture = {
  id: string;
  groupId?: string;
  groupName?: string;
  homeTeamId?: string;
  awayTeamId?: string;
  homeName: string;
  awayName: string;
  homeScore?: number;
  awayScore?: number;
  status?: string;
  stage?: string;
  kickoffAt?: string;
  venue?: string;
};

export type Group = { groupId: string; name: string; team_count: number };

export type Standing = {
  teamId: string;
  teamName: string;
  logoUrl?: string;
  position?: number;
  played?: number;
  won?: number;
  drawn?: number;
  lost?: number;
  goalsFor?: number;
  goalsAgainst?: number;
  goalDifference?: number;
  points?: number;
};

export type Player = {
  id: string;
  teamId: string;
  teamName?: string;
  name: string;
  position?: string;
  shirtNumber?: number;
  nationality?: string;
  photoUrl?: string;
};

export type Summary = {
  team_count?: number;
  match_count?: number;
  player_count?: number;
  last_synced_at?: string | null;
};

export type SyncRun = {
  id: number;
  status: string;
  recordsUpserted: number;
  startedAt: string;
  finishedAt?: string | null;
};
