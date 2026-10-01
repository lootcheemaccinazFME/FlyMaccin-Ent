package com.flymaccin.lootcheerom
import android.net.Uri
interface EmulatorCore {
    val id: String
    val systems: Set<String>
    fun load(uri: Uri): Result<Unit>
    fun start(): Result<Unit>
    fun pause(); fun resume(); fun reset(); fun stop()
    fun button(action: GameAction, pressed: Boolean)
    fun axis(action: GameAction, value: Float)
}
enum class GameAction { UP, DOWN, LEFT, RIGHT, A, B, X, Y, L1, R1, L2, R2, START, SELECT, LX, LY, RX, RY }
