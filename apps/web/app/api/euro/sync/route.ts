import { loadEnvConfig } from "@next/env";
import { existsSync } from "node:fs";
import { resolve } from "node:path";

const workingDirectory = process.cwd();
const repositoryRoot = existsSync(resolve(workingDirectory, "package-lock.json"))
  ? workingDirectory
  : resolve(workingDirectory, "../..");

loadEnvConfig(repositoryRoot);

export const runtime = "nodejs";
export const dynamic = "force-dynamic";

export async function POST(request: Request) {
  const origin = request.headers.get("origin");
  const allowedOrigins = (process.env.WEB_ORIGIN ?? "")
    .split(",")
    .map((value) => value.trim());
  if (!origin || allowedOrigins.length === 0 || !allowedOrigins.includes(origin)) {
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
