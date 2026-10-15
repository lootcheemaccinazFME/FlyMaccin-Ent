export type GateState = 'PASS' | 'BLOCKED' | 'IMPLEMENTED' | 'IN_PROGRESS';

export type ProductionEvidence = {
  publicMcp: boolean;
  connectorAuth: boolean;
  providerCommand: boolean;
  command1821: boolean;
  androidResultSync: boolean;
  physicalAndroidE2E: boolean;
  backupVerified: boolean;
  auditEnabled: boolean;
  privacyReviewed: boolean;
  apkCompatible: boolean;
};

export function evaluateProductionRelease(e: ProductionEvidence) {
  const gates = {
    public_mcp: e.publicMcp,
    connector_auth: e.connectorAuth,
    provider_command: e.providerCommand,
    command_1821: e.command1821,
    android_result_sync: e.androidResultSync,
    physical_android_e2e: e.physicalAndroidE2E,
    backup_verified: e.backupVerified,
    audit_enabled: e.auditEnabled,
    privacy_reviewed: e.privacyReviewed,
    apk_compatible: e.apkCompatible,
  };
  const missing = Object.entries(gates).filter(([, ok]) => !ok).map(([key]) => key);
  return { status: missing.length === 0 ? 'PASS' as const : 'BLOCKED' as const, missing, gates };
}
