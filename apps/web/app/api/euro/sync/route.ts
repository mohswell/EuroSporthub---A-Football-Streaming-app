export const runtime = "nodejs";
export const dynamic = "force-dynamic";
export const maxDuration = 300;

export async function POST(request: Request) {
  const origin = request.headers.get("origin");
  const requestOrigin = new URL(request.url).origin;
  if (!origin || origin !== requestOrigin) {
    return Response.json({ message: "Sync requests must originate from the configured web origin." }, { status: 403 });
  }

  const token = process.env.EURO_SYNC_TOKEN;
  if (!token) {
    return Response.json({ message: "Set EURO_SYNC_TOKEN in the root .env before syncing." }, { status: 503 });
  }

  const apiBase = process.env.EURO_API_URL?.replace(/\/$/, "");
  if (!apiBase) {
    return Response.json({ message: "EURO_API_URL is not configured." }, { status: 503 });
  }
  try {
    const response = await fetch(`${apiBase}/sync`, {
      method: "POST",
      headers: { "X-Sync-Token": token },
      cache: "no-store",
      signal: AbortSignal.timeout(180_000),
    });
    const payload = await response.json().catch(() => ({ message: "The Euro API returned an invalid response." }));
    return Response.json(payload, { status: response.status });
  } catch {
    return Response.json({ message: "The Euro API could not be reached." }, { status: 502 });
  }
}
