export type RetryDecision = { retry: boolean; delayMs: number; deadLetter: boolean };

export const MAX_PROVIDER_ATTEMPTS = 5;
const BASE_DELAY_MS = 2_000;
const MAX_DELAY_MS = 60_000;

export function retryDecision(attempt: number, retryable: boolean): RetryDecision {
  if (!retryable || attempt >= MAX_PROVIDER_ATTEMPTS) {
    return { retry: false, delayMs: 0, deadLetter: true };
  }
  const exponential = BASE_DELAY_MS * Math.pow(2, Math.max(0, attempt - 1));
  const delayMs = Math.min(MAX_DELAY_MS, exponential);
  return { retry: true, delayMs, deadLetter: false };
}

export function stableAttemptKey(jobId: string, providerId: string, attempt: number): string {
  if (!jobId || !providerId || attempt < 1) throw new Error('Invalid attempt identity');
  return `${jobId}:${providerId}:${attempt}`;
}

export function mayApplyProviderResult(currentVersion: number, incomingVersion: number): boolean {
  return incomingVersion > currentVersion;
}
