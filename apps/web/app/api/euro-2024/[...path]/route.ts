import { NextRequest } from "next/server";

export const runtime = "nodejs";
export const dynamic = "force-dynamic";

async function proxy(request: NextRequest, context: { params: Promise<{ path: string[] }> }) {
  const apiBase = process.env.EURO_API_URL?.replace(/\/$/, "");
  if (!apiBase) {
    return Response.json({ message: "EURO_API_URL is not configured." }, { status: 503 });
  }

  const { path } = await context.params;
  const upstreamUrl = `${apiBase}/${path.map(encodeURIComponent).join("/")}${request.nextUrl.search}`;
  try {
    const response = await fetch(upstreamUrl, {
      method: request.method,
      headers: { Accept: "application/json" },
      cache: "no-store",
      signal: AbortSignal.timeout(30_000),
    });
    const body = await response.arrayBuffer();
    return new Response(body, {
      status: response.status,
      headers: { "Content-Type": response.headers.get("content-type") ?? "application/json" },
    });
  } catch {
    return Response.json({ message: "The Euro API could not be reached." }, { status: 502 });
  }
}

export async function GET(request: NextRequest, context: { params: Promise<{ path: string[] }> }) {
  return proxy(request, context);
}
