import { Flag } from "lucide-react";
import type { Team } from "../euro/types";

export function Crest({ team, label }: { team?: Team; label: string }) {
  return (
    <span className="crest" aria-label={`${label} crest`}>
      {team?.logoUrl && <img src={team.logoUrl} alt="" loading="lazy" onError={(event) => { event.currentTarget.style.display = "none"; }} />}
      <span>{label.slice(0, 3).toUpperCase()}</span>
    </span>
  );
}

export function EmptyState({ title, detail }: { title: string; detail: string }) {
  return <div className="empty-state"><span className="empty-icon"><Flag size={21} /></span><strong>{title}</strong><p>{detail}</p></div>;
}

export function formatKickoff(value?: string) {
  if (!value) return "Date TBC";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat("en-GB", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
    timeZone: "Europe/Berlin",
  }).format(date);
}

export function isLive(status?: string) {
  return Boolean(status && /live|playing|in.?progress/i.test(status));
}
