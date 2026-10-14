package com.flymaccin.lootcheerom

/**
 * Requested platforms are not equivalent to installed, working emulator cores.
 * Keep experimental native projects separate from the libretro ABI.
 */
enum class PlatformStage { REGISTERED_LIBRETRO, NATIVE_RESEARCH, UNAVAILABLE_ANDROID }
data class PlatformPlan(
    val platform: String,
    val stage: PlatformStage,
    val engine: String,
    val note: String
)

object UniversalPlatformPlan {
    val requested = listOf(
        PlatformPlan("Sega Genesis / Mega Drive", PlatformStage.REGISTERED_LIBRETRO, "clownmdemu", "Native core packaging and on-device testing required"),
        PlatformPlan("Sega Dreamcast", PlatformStage.REGISTERED_LIBRETRO, "flycast", "Vulkan/OpenGL and BIOS requirements vary"),
        PlatformPlan("Super Nintendo", PlatformStage.REGISTERED_LIBRETRO, "bsnes", "Check device performance"),
        PlatformPlan("Nintendo 64", PlatformStage.REGISTERED_LIBRETRO, "mupen64plus_next", "OpenGL ES runtime testing required"),
        PlatformPlan("Game Boy / Game Boy Color", PlatformStage.REGISTERED_LIBRETRO, "sameboy", "Original test ROM available"),
        PlatformPlan("Xbox (original)", PlatformStage.NATIVE_RESEARCH, "xemu / alternative ARM64 research", "No integrated Android native core"),
        PlatformPlan("PlayStation 1", PlatformStage.REGISTERED_LIBRETRO, "pcsx_rearmed", "Firmware may be required"),
        PlatformPlan("PlayStation 2", PlatformStage.REGISTERED_LIBRETRO, "play", "Compatibility and performance vary"),
        PlatformPlan("PlayStation 3", PlatformStage.NATIVE_RESEARCH, "RPCS3 ARM64 feasibility", "No integrated Android native core"),
        PlatformPlan("PlayStation 4", PlatformStage.NATIVE_RESEARCH, "Prosperity", "Experimental, not a libretro core"),
        PlatformPlan("PlayStation 5", PlatformStage.NATIVE_RESEARCH, "Prosperity PS5 Native Lab", "Experimental, no gameplay certification"),
        PlatformPlan("PlayStation Portable", PlatformStage.REGISTERED_LIBRETRO, "ppsspp", "Core packaging and on-device testing required"),
        PlatformPlan("PlayStation Vita", PlatformStage.NATIVE_RESEARCH, "Vita3K", "Separate Android-native integration and license review required"),
        PlatformPlan("Arcade cabinets (NAOMI / Atomiswave)", PlatformStage.REGISTERED_LIBRETRO, "flycast", "Specific arcade hardware only"),
        PlatformPlan("Arcade cabinets (MAME / FBNeo)", PlatformStage.NATIVE_RESEARCH, "FinalBurn Neo / MAME", "No generic arcade core registered yet")
    )

    /** Never equate registry metadata with installed native .so binaries. */
    fun installedLibretroCandidates() = requested.filter {
        it.stage == PlatformStage.REGISTERED_LIBRETRO &&
            CoreRegistry.specs.any { core -> core.id == it.engine }
    }

    fun nativeResearchTargets() = requested.filter { it.stage == PlatformStage.NATIVE_RESEARCH }
}
