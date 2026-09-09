package org.example.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var canvas: SketchCanvasView
    private var nextId = 1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        canvas = SketchCanvasView(this)
        setContentView(canvas)

        canvas.listener = object : SketchCanvasView.Listener {
            override fun onLongPressEmptySpace(x: Float, y: Float) = openTools(x, y)

            override fun onPartTapped(part: SketchPart) {
                showPartOptionsDialog(
                    context = this@MainActivity,
                    part = part,
                    onRename = { newLabel ->
                        part.label = newLabel
                        canvas.invalidate()
                    },
                    onDelete = { canvas.removePart(part) },
                )
            }

            override fun onPartsChanged() {
                // Hook for future persistence (e.g. saving the sketch to disk).
            }
        }
    }

    private fun openTools(x: Float, y: Float) {
        showToolsSheet(
            context = this,
            hasParts = canvas.parts.isNotEmpty(),
            onPick = { kind -> addPart(kind, x, y) },
            onGeneratePrompt = { generatePrompt() },
            onClear = { canvas.clearAll() },
        )
    }

    private fun addPart(kind: PartKind, x: Float, y: Float) {
        val density = resources.displayMetrics.density
        val fullWidth = kind == PartKind.TOP_APP_BAR || kind == PartKind.NAV_BAR
        val w = if (fullWidth) canvas.width.toFloat() else kind.defaultW * density
        val h = kind.defaultH * density
        val px = if (fullWidth) 0f else (x - w / 2f).coerceIn(0f, (canvas.width - w).coerceAtLeast(0f))
        val py = when (kind) {
            PartKind.TOP_APP_BAR -> 0f
            PartKind.NAV_BAR -> (canvas.height - h).coerceAtLeast(0f)
            else -> (y - h / 2f).coerceIn(0f, (canvas.height - h).coerceAtLeast(0f))
        }
        canvas.addPart(SketchPart(nextId++, kind, px, py, w, h))
    }

    private fun generatePrompt() {
        val density = resources.displayMetrics.density
        val prompt = PromptGenerator.build(canvas.parts, canvas.width, canvas.height, density)
        showPromptDialog(this, prompt)
    }
}
