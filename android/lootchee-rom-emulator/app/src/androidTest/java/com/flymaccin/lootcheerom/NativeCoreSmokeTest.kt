package com.flymaccin.lootcheerom

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(AndroidJUnit4::class)
class NativeCoreSmokeTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun open(id: String): CoreSpec {
        val spec = CoreRegistry.specs.first { it.id == id }
        assertTrue("Missing native core: $id", CoreRegistry.installed(context, spec))
        assertTrue("Core open failed: $id", NativeBridge.open(
            CoreRegistry.libraryPath(context, spec),
            context.filesDir.absolutePath,
            context.filesDir.absolutePath
        ))
        return spec
    }

    @Test fun sameBoyLoadsAndProducesVideo() {
        open("sameboy")
        try {
            assertTrue("Game Boy test ROM rejected: ${NativeBridge.diagnostic()}", NativeBridge.loadGame(null, TestRom.build()))
            var audioSamples = 0
            repeat(180) { NativeBridge.runFrame(); audioSamples += NativeBridge.drainAudio().size }
            assertTrue("No Game Boy video callbacks", NativeBridge.videoCount() > 0)
            assertTrue("No Game Boy frame pixels", NativeBridge.frame().isNotEmpty())
            assertTrue("No Game Boy dimensions", NativeBridge.frameWidth() > 0 && NativeBridge.frameHeight() > 0)
            assertTrue("No Game Boy audio", audioSamples > 0)
            val saved = NativeBridge.saveState()
            assertNotNull("Game Boy save state unsupported", saved)
            assertTrue("Game Boy save state empty", saved!!.isNotEmpty())
            assertTrue("Game Boy save state restore failed", NativeBridge.loadState(saved))
        } finally { NativeBridge.close() }
    }

    @Test fun dosBoxPureLoadsOriginalDosProgram() {
        open("dosbox_pure")
        try {
            val zip = File(context.cacheDir, "lootchee-dos-test.dosz")
            ZipOutputStream(zip.outputStream()).use { stream ->
                stream.putNextEntry(ZipEntry("HELLO.COM"))
                // DOS .COM program: print 'OK' using INT 21h and exit.
                stream.write(byteArrayOf(
                    0xBA.toByte(), 0x0C, 0x01, 0xB4.toByte(), 0x09,
                    0xCD.toByte(), 0x21, 0xB8.toByte(), 0x00, 0x4C,
                    0xCD.toByte(), 0x21, 0x4F, 0x4B, 0x24
                ))
                stream.closeEntry()
            }
            val bytes = zip.readBytes()
            assertTrue("DOSBox Pure rejected test archive: ${NativeBridge.diagnostic()}",
                NativeBridge.loadGame(zip.absolutePath, bytes))
            repeat(180) { NativeBridge.runFrame(); NativeBridge.drainAudio() }
            assertTrue("No DOS video callbacks", NativeBridge.videoCount() > 0)
            assertTrue("No DOS frame pixels", NativeBridge.frame().isNotEmpty())
        } finally { NativeBridge.close() }
    }
}
