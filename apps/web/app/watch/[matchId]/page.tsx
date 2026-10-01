import Link from "next/link";
import { ArrowLeft, CalendarDays, CircleAlert, Play, Radio, Shield } from "lucide-react";

const mockMatches: Record<string, { home: string; away: string; score: string; stage: string; date: string; venue: string }> = {
  "1676021": { home: "Spain", away: "Croatia", score: "3 : 0", stage: "Group B", date: "15 Jun, 16:00", venue: "Olympiastadion, Berlin" },
  "1696579": { home: "Spain", away: "England", score: "2 : 1", stage: "Final", date: "14 Jul, 19:00", venue: "Olympiastadion, Berlin" },
};

const fallbackMatch = {
  home: "Germany",
  away: "Scotland",
  score: "5 : 1",
  stage: "Group A",
  date: "14 Jun, 19:00",
  venue: "Allianz Arena, Munich",
};

export default async function WatchMatch({ params }: { params: Promise<{ matchId: string }> }) {
  const { matchId } = await params;
  const match = mockMatches[matchId] ?? fallbackMatch;

  return (
    <main className="watch-page">
      <header className="watch-topbar">
        <Link className="watch-brand" href="/" aria-label="Back to EuroSportHub">
          <span className="watch-brand-mark"><Shield size={17} /></span>
          EURO<span>SPORT</span><b>HUB</b>
        </Link>
        <span className="watch-edition"><span /> EURO 2024 · MATCH CENTRE</span>
      </header>

      <div className="watch-wrap">
        <Link className="back-link" href="/"><ArrowLeft size={16} /> Back to fixtures</Link>

        <section className="watch-heading">
          <div>
            <p className="eyebrow dark-eyebrow">{match.stage}</p>
            <h1>{match.home} <em>vs</em> {match.away}</h1>
            <p className="watch-meta"><CalendarDays size={15} /> {match.date} <i /> {match.venue}</p>
          </div>
          <div className="watch-score"><strong>{match.score}</strong><span>FULL TIME</span></div>
        </section>

        <section className="video-stage" aria-label="Unavailable match stream">
          <div className="video-noise" />
          <div className="video-message">
            <span className="video-icon"><CircleAlert size={24} /></span>
            <strong>This stream is unavailable</strong>
            <p>There is no broadcast link available for this match yet.</p>
            <span className="video-status"><Radio size={13} /> BROADCAST OFFLINE</span>
          </div>
          <span className="video-corner video-corner-left">EURO 2024 / {match.stage}</span>
          <span className="video-corner video-corner-right">LIVE FEED 00:00</span>
        </section>

        <section className="watch-lower">
          <div className="watch-card"><span className="watch-card-label">MATCH INFORMATION</span><h2>{match.home} <span>{match.score}</span> {match.away}</h2><p>{match.date} · {match.venue}</p></div>
          <div className="watch-card watch-coming"><span className="watch-card-label">BROADCAST</span><Play size={19} /><strong>Watch links will appear here</strong><p>Streaming availability depends on broadcast rights and provider coverage.</p></div>
        </section>
      </div>
    </main>
  );
}
