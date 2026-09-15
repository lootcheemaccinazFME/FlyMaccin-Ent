export type SessionStatus = 'ACTIVE' | 'REVOKED';
export type SyncDecision = 'APPLY' | 'DUPLICATE' | 'STALE' | 'CONFLICT';

export function canUseSession(status: SessionStatus, revokedAt?: string | null): boolean {
  return status === 'ACTIVE' && !revokedAt;
}

export function decideSync(
  alreadyReceipted: boolean,
  localVersion: number,
  incomingVersion: number
): SyncDecision {
  if (alreadyReceipted) return 'DUPLICATE';
  if (incomingVersion < localVersion) return 'STALE';
  if (incomingVersion === localVersion) return 'CONFLICT';
  return 'APPLY';
}

export function advanceCursor(current: number, delivered: number): number {
  if (delivered < current) return current;
  return delivered;
}
