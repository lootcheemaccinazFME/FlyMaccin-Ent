BEGIN;

CREATE TABLE IF NOT EXISTS fme_job_attempts (
  id text PRIMARY KEY,
  job_id text NOT NULL,
  attempt_no integer NOT NULL CHECK (attempt_no > 0),
  state text NOT NULL,
  provider_id text,
  idempotency_key text NOT NULL,
  error_code text,
  error_detail text,
  started_at timestamptz NOT NULL DEFAULT now(),
  completed_at timestamptz,
  next_retry_at timestamptz,
  UNIQUE(job_id, attempt_no),
  UNIQUE(idempotency_key)
);
CREATE INDEX IF NOT EXISTS idx_fme_job_attempts_retry ON fme_job_attempts(state, next_retry_at);

CREATE TABLE IF NOT EXISTS fme_dead_letter_jobs (
  id text PRIMARY KEY,
  job_id text NOT NULL UNIQUE,
  reason text NOT NULL,
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  failed_attempts integer NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now(),
  replayed_at timestamptz,
  replay_job_id text
);

CREATE TABLE IF NOT EXISTS fme_device_sessions (
  id text PRIMARY KEY,
  device_id text NOT NULL,
  user_id text NOT NULL,
  token_fingerprint text NOT NULL UNIQUE,
  status text NOT NULL DEFAULT 'ACTIVE',
  event_cursor bigint NOT NULL DEFAULT 0,
  last_seen_at timestamptz,
  revoked_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_fme_device_sessions_user ON fme_device_sessions(user_id, status);

CREATE TABLE IF NOT EXISTS fme_sync_receipts (
  id text PRIMARY KEY,
  device_id text NOT NULL,
  event_id text NOT NULL,
  entity_type text NOT NULL,
  entity_id text NOT NULL,
  entity_version bigint NOT NULL DEFAULT 1,
  result text NOT NULL,
  details jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(device_id, event_id)
);

CREATE TABLE IF NOT EXISTS fme_release_manifests (
  id text PRIMARY KEY,
  release_name text NOT NULL UNIQUE,
  gateway_version text NOT NULL,
  apk_version text NOT NULL,
  apk_version_code integer NOT NULL,
  schema_version integer NOT NULL,
  status text NOT NULL DEFAULT 'CANDIDATE',
  evidence jsonb NOT NULL DEFAULT '{}'::jsonb,
  sha256 text,
  created_at timestamptz NOT NULL DEFAULT now(),
  promoted_at timestamptz,
  rolled_back_at timestamptz
);

CREATE TABLE IF NOT EXISTS fme_deployment_receipts (
  id text PRIMARY KEY,
  release_manifest_id text NOT NULL,
  environment text NOT NULL,
  action text NOT NULL,
  status text NOT NULL,
  commit_sha text,
  deployment_ref text,
  evidence jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamptz NOT NULL DEFAULT now()
);

COMMIT;
