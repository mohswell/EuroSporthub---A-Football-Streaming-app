"use client";

import { useEffect, useState } from "react";
import { loadPlayers, loadStandings, loadTournamentData, syncTournament } from "./api";
import type { Fixture, Group, Player, Standing, Summary, SyncRun, Team, View } from "./types";

export function useEuroData({ view, groupId, teamId, liveTable }: {
  view: View;
  groupId: string;
  teamId: string;
  liveTable: boolean;
}) {
  const [summary, setSummary] = useState<Summary>({});
  const [teams, setTeams] = useState<Team[]>([]);
  const [fixtures, setFixtures] = useState<Fixture[]>([]);
  const [groups, setGroups] = useState<Group[]>([]);
  const [table, setTable] = useState<Standing[]>([]);
  const [players, setPlayers] = useState<Player[]>([]);
  const [syncRuns, setSyncRuns] = useState<SyncRun[]>([]);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);
  const [syncMessage, setSyncMessage] = useState("");
  const [error, setError] = useState("");
  const [reload, setReload] = useState(0);
  const effectiveGroupId = groupId || groups[0]?.groupId || "";

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError("");
    loadTournamentData(controller.signal)
      .then(([nextSummary, nextTeams, nextFixtures, nextGroups, nextSyncRuns]) => {
        setSummary(nextSummary);
        setTeams(nextTeams);
        setFixtures(nextFixtures);
        setGroups(nextGroups);
        setSyncRuns(nextSyncRuns);
      })
      .catch((cause) => {
        if (!controller.signal.aborted) setError(cause instanceof Error ? cause.message : "Could not reach the Euro API.");
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, [reload]);

  useEffect(() => {
    const controller = new AbortController();
    if (!effectiveGroupId) return () => controller.abort();
    loadStandings(effectiveGroupId, liveTable, controller.signal)
      .then(setTable)
      .catch(() => { if (!controller.signal.aborted) setTable([]); });
    return () => controller.abort();
  }, [effectiveGroupId, liveTable, reload]);

  useEffect(() => {
    const controller = new AbortController();
    if (view !== "players") return () => controller.abort();
    loadPlayers(teamId, controller.signal)
      .then(setPlayers)
      .catch(() => { if (!controller.signal.aborted) setPlayers([]); });
    return () => controller.abort();
  }, [view, teamId, reload]);

  async function runSync() {
    setSyncing(true);
    setSyncMessage("");
    setError("");
    try {
      const payload = await syncTournament();
      setSyncMessage(`${payload.teamsUpserted} teams · ${payload.groupsUpserted} groups · ${payload.matchesUpserted} matches · ${payload.standingsUpserted} standings`);
      setReload((value) => value + 1);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not sync tournament data.");
    } finally {
      setSyncing(false);
    }
  }

  return {
    summary, teams, fixtures, groups, table, players, syncRuns,
    loading, syncing, syncMessage, error,
    refresh: () => setReload((value) => value + 1),
    runSync,
  };
}
