package com.flymaccin.lootcheerom

/**
 * PC content classification only. A classification is not a compatibility guarantee.
 * Never attempt to execute Windows PE binaries directly on Android.
 */
enum class PcRuntime { DOSBOX_PURE, WINDOWS_COMPATIBILITY_RESEARCH, NOT_PC_CONTENT }

data class PcGameRoute(
    val runtime: PcRuntime,
    val coreId: String?,
    val supportedNow: Boolean,
    val explanation: String
)

object PcGameRouter {
    fun classify(filename: String): PcGameRoute {
        val extension = filename.substringAfterLast('.', "").lowercase(java.util.Locale.ROOT)
        return when (extension) {
            "dosz" -> PcGameRoute(
                PcRuntime.DOSBOX_PURE,
                "dosbox_pure",
                true,
                "DOSBox Pure core candidate. Requires an installed compatible Android Libretro binary."
            )
            "exe", "msi", "bat", "com", "pif" -> PcGameRoute(
                PcRuntime.WINDOWS_COMPATIBILITY_RESEARCH,
                null,
                false,
                "PC executable detected. No Windows compatibility runtime is installed; execution is disabled."
            )
            else -> PcGameRoute(PcRuntime.NOT_PC_CONTENT, null, false, "Not classified as PC content.")
        }
    }
}
