# FME Cloud Brain v0.1.0

Private, containerized AI gateway for FlyMaccin Ent / Demonic AI Studio Hut / Pocket Potna.

## What this first build does

- Exposes an OpenAI-compatible `POST /v1/chat/completions` endpoint.
- Proxies chat to a self-hosted vLLM model backend.
- Supports streaming responses.
- Provides a private FME document library using SQLite FTS5.
- Retrieves relevant FME library records and injects them into model context automatically.
- Marks library records as authoritative/non-authoritative.
- Provides authenticated library ingest/search endpoints.
- Provides health/readiness endpoints for deployment platforms.
- Runs as a non-root application container.
- Keeps model and gateway credentials in environment variables instead of source code.

## Architecture

```text
Demonic AI Studio Hut / Pocket Potna
              |
              | HTTPS + Bearer token
              v
        FME Cloud Brain API
          |            |
          |            +--> FME Library (SQLite FTS5)
          |
          +--> OpenAI-compatible model backend
                         |
                         +--> vLLM + your selected open-weight model
```

## Required secrets

Do not commit real credentials.

- `FME_API_KEY`: client-facing API secret used by Demonic AI Studio Hut.
- `MODEL_API_KEY`: separate secret protecting the internal model server.
- `HUGGING_FACE_HUB_TOKEN`: only when the selected model requires authenticated download.

Generate strong keys locally, for example:

```bash
python -c "import secrets; print(secrets.token_urlsafe(48))"
```

## Run on a GPU host

1. Copy `.env.example` to `.env`.
2. Set `MODEL_ID` to the exact model repository you are legally permitted to deploy.
3. Set unique `FME_API_KEY` and `MODEL_API_KEY` values.
4. Ensure Docker, Docker Compose, NVIDIA drivers, and NVIDIA Container Toolkit are installed.
5. Start:

```bash
docker compose up -d --build
```

Check the gateway:

```bash
curl http://localhost:8080/health
```

Check model readiness:

```bash
curl -H "Authorization: Bearer $FME_API_KEY" http://localhost:8080/ready
```

## Add FME knowledge

```bash
curl -X POST http://localhost:8080/v1/fme/library/documents \
  -H "Authorization: Bearer $FME_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"title":"Example canon record","content":"Authoritative FME material goes here.","tags":["canon"],"authoritative":true}'
```

## Chat

```bash
curl -X POST http://localhost:8080/v1/chat/completions \
  -H "Authorization: Bearer $FME_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"messages":[{"role":"user","content":"What does the FME library say about this project?"}]}'
```

## Deployment boundary

This repository intentionally does **not** contain a private signing key, cloud account credential, paid-provider key, or proprietary model weight. Deployment credentials belong in the selected cloud provider's secret manager.

The gateway is cloud-provider neutral. The same container can sit in front of a GPU on RunPod, a managed GPU VM, or another Docker-capable host. The app only needs the HTTPS endpoint and its `FME_API_KEY`.

## Next production gates

- Select and license-check the exact open-weight model.
- Pin the model image by version/digest for repeatable deployment.
- Put the public API behind TLS and a reverse proxy/load balancer.
- Add persistent encrypted backup for the FME library volume.
- Add rate limits and structured audit logging.
- Integrate the Android client with secure local credential storage.
- Replace lexical retrieval with a vector/embedding index when the FME library reaches sufficient scale.
