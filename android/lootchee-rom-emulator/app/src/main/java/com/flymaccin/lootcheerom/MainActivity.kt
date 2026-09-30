package com.flymaccin.lootcheerom
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.*
class MainActivity : Activity() {
    private lateinit var status: TextView
    private var core: EmulatorCore? = null
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32,32,32,32) }
        root.addView(TextView(this).apply { text = "LOOTCHEE OS • RETRO EMULATOR"; textSize = 24f })
        status = TextView(this).apply { text = "ROM Vault ready. No ROM loaded."; textSize = 16f }; root.addView(status)
        root.addView(Button(this).apply { text = "IMPORT USER-OWNED / HOMEBREW ROM"; setOnClickListener { pickRom() } })
        root.addView(Button(this).apply { text = "PAUSE"; setOnClickListener { core?.pause(); status.append("\nPaused") } })
        root.addView(Button(this).apply { text = "RESET"; setOnClickListener { core?.reset(); status.append("\nReset") } })
        setContentView(root)
    }
    private fun pickRom() {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE); type = "application/octet-stream"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }, 100)
    }
    @Deprecated("legacy callback kept for minSdk compatibility")
    override fun onActivityResult(req: Int, result: Int, data: Intent?) {
        super.onActivityResult(req, result, data)
        if (req == 100 && result == RESULT_OK && data?.data != null) {
            val uri = data.data!!
            runCatching { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            runCatching { RomVault.inspect(this, uri) }
                .onSuccess { record -> status.text = "ROM VERIFIED\n" + record.name + "\n" + record.size + " bytes\nSHA-256 " + record.sha256 + "\nWaiting for installed emulator core." }
                .onFailure { error -> status.text = "ROM import failed: " + error.message }
        }
    }
    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        if ((e.source and InputDevice.SOURCE_GAMEPAD) != 0 || (e.source and InputDevice.SOURCE_JOYSTICK) != 0) {
            val action = when(e.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> GameAction.UP; KeyEvent.KEYCODE_DPAD_DOWN -> GameAction.DOWN
                KeyEvent.KEYCODE_DPAD_LEFT -> GameAction.LEFT; KeyEvent.KEYCODE_DPAD_RIGHT -> GameAction.RIGHT
                KeyEvent.KEYCODE_BUTTON_A -> GameAction.A; KeyEvent.KEYCODE_BUTTON_B -> GameAction.B
                KeyEvent.KEYCODE_BUTTON_X -> GameAction.X; KeyEvent.KEYCODE_BUTTON_Y -> GameAction.Y
                KeyEvent.KEYCODE_BUTTON_START -> GameAction.START; KeyEvent.KEYCODE_BUTTON_SELECT -> GameAction.SELECT
                else -> null
            }
            if (action != null) { core?.button(action, e.action == KeyEvent.ACTION_DOWN); return true }
        }
        return super.dispatchKeyEvent(e)
    }
    override fun onGenericMotionEvent(e: MotionEvent): Boolean {
        if ((e.source and InputDevice.SOURCE_JOYSTICK) != 0) {
            core?.axis(GameAction.LX, e.getAxisValue(MotionEvent.AXIS_X)); core?.axis(GameAction.LY, e.getAxisValue(MotionEvent.AXIS_Y))
            core?.axis(GameAction.RX, e.getAxisValue(MotionEvent.AXIS_Z)); core?.axis(GameAction.RY, e.getAxisValue(MotionEvent.AXIS_RZ)); return true
        }
        return super.onGenericMotionEvent(e)
    }
}
