# Waypoint AI Chat — Cloudflare Worker

The app sends chat requests to a Cloudflare Worker (`POST /chat`). The Worker holds the server-side `OPENROUTER_API_KEY` secret and forwards requests to OpenRouter. The Android app never needs the key.

## Configure the Worker URL in the app

Edit the `workerUrl` parameter in:
```
app/src/main/java/com/example/data/remote/OpenRouterAiService.kt
```
Replace `https://waypoint-ai-worker.your-account.workers.dev/chat` with your deployed Worker URL.

## Deploy the Worker (Windows)

1. Install `wrangler` (Cloudflare CLI) globally:
```powershell
npm install -g wrangler
```
2. Log in:
```powershell
wrangler login
```
3. Set the secret (do NOT put this in source):
```powershell
wrangler secret put OPENROUTER_API_KEY
# Paste your real OpenRouter key when prompted
```
4. Deploy:
```powershell
wrangler deploy
```
5. Note the deployed URL (e.g. `https://waypoint-ai-worker.your-account.workers.dev/chat`) and put it in the Kotlin file above.

## What the Worker does
- Receives `POST /chat` from the Android app with `{ model, messages, temperature, max_tokens }`.
- Adds the server-side `OPENROUTER_API_KEY` via `env.OPENROUTER_API_KEY`.
- Forwards to `https://openrouter.ai/api/v1/chat/completions`.
- Returns the OpenRouter JSON response to the app.
- If the Worker or OpenRouter fails, the Android app falls back to `generateSmartFallback()` (offline response) without exposing the key.
