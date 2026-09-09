package org.example.test

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.text.InputType
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton

/** Modal shown on long-press: pick a part to drop at that spot, or generate the prompt. */
fun showToolsSheet(
    context: Context,
    hasParts: Boolean,
    onPick: (PartKind) -> Unit,
    onGeneratePrompt: () -> Unit,
    onClear: () -> Unit,
) {
    val dialog = BottomSheetDialog(context)
    val d = context.resources.displayMetrics.density
    fun dp(v: Int) = (v * d).toInt()

    val root = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(20), dp(20), dp(20), dp(20))
    }

    root.addView(TextView(context).apply {
        text = "Add to sketch"
        textSize = 18f
        setTypeface(typeface, Typeface.BOLD)
        setPadding(0, 0, 0, dp(12))
    })

    val grid = GridLayout(context).apply { columnCount = 3 }
    for (kind in PartKind.values()) {
        grid.addView(MaterialButton(context).apply {
            text = kind.displayLabel
            isAllCaps = false
            textSize = 12f
            layoutParams = GridLayout.LayoutParams().apply {
                width = dp(104)
                height = GridLayout.LayoutParams.WRAP_CONTENT
                setMargins(dp(4), dp(4), dp(4), dp(4))
            }
            setOnClickListener {
                onPick(kind)
                dialog.dismiss()
            }
        })
    }
    root.addView(grid)

    root.addView(MaterialButton(context).apply {
        text = "Generate prompt"
        isAllCaps = false
        isEnabled = hasParts
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { topMargin = dp(16) }
        setOnClickListener {
            onGeneratePrompt()
            dialog.dismiss()
        }
    })

    if (hasParts) {
        root.addView(MaterialButton(context).apply {
            text = "Clear canvas"
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = dp(8) }
            setOnClickListener {
                onClear()
                dialog.dismiss()
            }
        })
    }

    dialog.setContentView(ScrollView(context).apply { addView(root) })
    dialog.show()
}

/** Tapping an existing part on the canvas: rename its label or delete it. */
fun showPartOptionsDialog(
    context: Context,
    part: SketchPart,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val d = context.resources.displayMetrics.density
    val pad = (16 * d).toInt()
    val input = EditText(context).apply {
        setText(part.label)
        hint = part.kind.displayLabel
        inputType = InputType.TYPE_CLASS_TEXT
        setPadding(pad, pad, pad, pad)
    }
    AlertDialog.Builder(context)
        .setTitle(part.kind.displayLabel)
        .setView(input)
        .setPositiveButton("Save") { _, _ -> onRename(input.text.toString()) }
        .setNegativeButton("Delete") { _, _ -> onDelete() }
        .setNeutralButton("Cancel", null)
        .show()
}

/** Shows the generated prompt text with a Copy button. */
fun showPromptDialog(context: Context, prompt: String) {
    val d = context.resources.displayMetrics.density
    val pad = (20 * d).toInt()
    val textView = TextView(context).apply {
        text = prompt
        setTextIsSelectable(true)
        setPadding(pad, pad, pad, pad)
    }
    AlertDialog.Builder(context)
        .setTitle("Prompt")
        .setView(ScrollView(context).apply { addView(textView) })
        .setPositiveButton("Copy") { _, _ ->
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("M3E prompt", prompt))
            Toast.makeText(context, "Prompt copied", Toast.LENGTH_SHORT).show()
        }
        .setNegativeButton("Close", null)
        .show()
}
