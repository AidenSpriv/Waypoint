export default {
  async fetch(request, env, ctx) {
    // Only accept POST /chat
    if (request.method !== "POST") {
      return new Response("Only POST /chat is allowed", { status: 405 });
    }
    const url = new URL(request.url);
    if (url.pathname !== "/chat") {
      return new Response("Not found. Use POST /chat", { status: 404 });
    }

    try {
      const body = await request.json();

      // Forward the same chat payload shape the Android app sends,
      // but inject the server-side secret.
      const openRouterPayload = {
        model: body.model || "openai/gpt-4o-mini",
        messages: body.messages || body.history || [],
        temperature: body.temperature ?? 0.7,
        max_tokens: body.max_tokens ?? 800,
      };

      const openRouterUrl = "https://openrouter.ai/api/v1/chat/completions";

      const upstreamResponse = await fetch(openRouterUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${env.OPENROUTER_API_KEY}`,
          "HTTP-Referer": "https://waypoint.doha.app",
          "X-Title": "WAYPOINT Walking Navigation",
        },
        body: JSON.stringify(openRouterPayload),
      });

      const responseBody = await upstreamResponse.text();

      if (!upstreamResponse.ok) {
        return new Response(JSON.stringify({
          error: "OpenRouter returned an error",
          status: upstreamResponse.status,
          upstreamBody: responseBody,
        }), { status: upstreamResponse.status, headers: { "Content-Type": "application/json" } });
      }

      // Pass the OpenRouter response straight through so the Android
      // app can parse the same JSON fields it expects.
      return new Response(responseBody, {
        status: upstreamResponse.status,
        headers: {
          "Content-Type": "application/json",
          // CORS for development; tighten in production if needed
          "Access-Control-Allow-Origin": "*",
        },
      });
    } catch (e) {
      return new Response(JSON.stringify({ error: e.message || "Worker error" }), {
        status: 500,
        headers: { "Content-Type": "application/json" },
      });
    }
  },
};
