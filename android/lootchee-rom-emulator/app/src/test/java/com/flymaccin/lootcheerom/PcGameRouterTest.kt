package com.flymaccin.lootcheerom

import org.junit.Assert.*
import org.junit.Test

class PcGameRouterTest {
    @Test fun dosArchiveUsesRegisteredCore() {
        val route = PcGameRouter.classify("MY_GAME.DOSZ")
        assertEquals(PcRuntime.DOSBOX_PURE, route.runtime)
        assertEquals("dosbox_pure", route.coreId)
        assertTrue(route.supportedNow)
    }

    @Test fun windowsExecutablesCannotLaunch() {
        for (name in listOf("setup.exe", "installer.MSI", "AUTOEXEC.BAT", "game.com", "shortcut.pif")) {
            val route = PcGameRouter.classify(name)
            assertEquals(name, PcRuntime.WINDOWS_COMPATIBILITY_RESEARCH, route.runtime)
            assertNull(route.coreId)
            assertFalse(route.supportedNow)
        }
    }

    @Test fun romIsNotMistakenForPcGame() {
        val route = PcGameRouter.classify("test.gb")
        assertEquals(PcRuntime.NOT_PC_CONTENT, route.runtime)
        assertNull(route.coreId)
    }
}
