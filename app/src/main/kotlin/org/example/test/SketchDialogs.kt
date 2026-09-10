package org.example.test

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton

/**
 * Modal used by the bottom panel's Shapes/Text/Media/Components tabs (and the empty-state
 * prompt / long-press-empty-space gesture): pick a part to drop onto the canvas.
 * [title] and [kinds] let callers scope the grid to what that tab represents.
 */
fun showPartPickerSheet(
    context: Context,
    title: String,
    kinds: List<PartKind>,
    onPick: (PartKind) -> Unit,
    onDismiss: () -> Unit = {},
) {
    val dialog = BottomSheetDialog(context)
    val d = context.resources.displayMetrics.density
    fun dp(v: Int) = (v * d).toInt()

    val root = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(20), dp(20), dp(20), dp(20))
    }

    root.addView(TextView(context).apply {
        text = title
        textSize = 18f
        setTypeface(typeface, Typeface.BOLD)
        setPadding(0, 0, 0, dp(12))
    })

    val grid = GridLayout(context).apply { columnCount = 3 }
    for (kind in kinds) {
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

    dialog.setOnDismissListener { onDismiss() }
    dialog.setContentView(ScrollView(context).apply { addView(root) })
    dialog.show()
}

/**
 * Full "Components" picker opened from the bottom panel's Components tab: a search bar, a
 * vertical rail of category icons (one per [ComponentCategory]) on the left, and a grid of
 * parts for whichever category is selected on the right.
 */
fun showComponentsPickerSheet(
    context: Context,
    onPick: (PartKind) -> Unit,
    onDismiss: () -> Unit = {},
) {
    val dialog = BottomSheetDialog(context)
    val d = context.resources.displayMetrics.density
    fun dp(v: Int) = (v * d).toInt()

    val panelBg = Color.parseColor("#15161F")
    val mutedText = Color.parseColor("#9A9AA5")

    val root = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(panelBg)
        setPadding(dp(16), dp(12), dp(16), dp(16))
    }

    // Drag handle
    root.addView(View(context).apply {
        setBackgroundResource(R.drawable.bg_drag_handle)
        layoutParams = LinearLayout.LayoutParams(dp(36), dp(4)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            bottomMargin = dp(14)
        }
    })

    // Title row
    root.addView(LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { bottomMargin = dp(14) }

        addView(TextView(context).apply {
            text = "Components"
            setTextColor(Color.WHITE)
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        addView(ImageButton(context).apply {
            setImageResource(R.drawable.ic_close)
            setColorFilter(mutedText)
            background = null
            layoutParams = LinearLayout.LayoutParams(dp(28), dp(28))
            setOnClickListener { dialog.dismiss() }
        })
    })

    // Search row
    root.addView(LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setBackgroundResource(R.drawable.bg_search_bar)
        setPadding(dp(12), dp(10), dp(12), dp(10))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { bottomMargin = dp(14) }

        addView(ImageView(context).apply {
            setImageResource(R.drawable.ic_search)
            setColorFilter(mutedText)
            layoutParams = LinearLayout.LayoutParams(dp(18), dp(18)).apply { marginEnd = dp(8) }
        })
        addView(EditText(context).apply {
            hint = "Search components"
            setHintTextColor(mutedText)
            setTextColor(Color.WHITE)
            background = null
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        addView(ImageView(context).apply {
            setImageResource(R.drawable.ic_filter)
            setColorFilter(mutedText)
            layoutParams = LinearLayout.LayoutParams(dp(18), dp(18)).apply { marginStart = dp(8) }
        })
    })

    // Category rail + content grid
    val contentContainer = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    val railItems = mutableListOf<Pair<ComponentCategory, FrameLayout>>()

    fun renderCategory(category: ComponentCategory) {
        for ((cat, frame) in railItems) {
            val active = cat == category
            frame.setBackgroundResource(if (active) R.drawable.bg_tab_selected else 0)
            (frame.getChildAt(0) as ImageView).setColorFilter(if (active) Color.WHITE else mutedText)
        }

        contentContainer.removeAllViews()
        contentContainer.addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = dp(10) }

            addView(TextView(context).apply {
                text = category.label
                setTextColor(Color.WHITE)
                textSize = 14f
                setTypeface(typeface, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(ImageView(context).apply {
                setImageResource(R.drawable.ic_more_vert)
                setColorFilter(mutedText)
                layoutParams = LinearLayout.LayoutParams(dp(18), dp(18))
            })
        })

        if (category.kinds.isEmpty()) {
            contentContainer.addView(TextView(context).apply {
                text = "No components in this category yet"
                setTextColor(mutedText)
                textSize = 13f
                setPadding(0, dp(8), 0, dp(8))
            })
            return
        }

        val grid = GridLayout(context).apply { columnCount = 2 }
        for (kind in category.kinds) {
            grid.addView(FrameLayout(context).apply {
                setBackgroundResource(R.drawable.bg_component_card)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = dp(126)
                    height = dp(70)
                    setMargins(dp(0), dp(0), dp(10), dp(10))
                }
                addView(TextView(context).apply {
                    text = kind.displayLabel
                    setTextColor(Color.WHITE)
                    textSize = 12.5f
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER,
                    )
                })
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    onPick(kind)
                    dialog.dismiss()
                }
            })
        }
        contentContainer.addView(grid)
    }

    val rail = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    for (category in ComponentCategory.values()) {
        val frame = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40)).apply { bottomMargin = dp(10) }
            isClickable = true
            isFocusable = true
            addView(ImageView(context).apply {
                setImageResource(category.iconRes)
                layoutParams = FrameLayout.LayoutParams(dp(20), dp(20), Gravity.CENTER)
            })
            setOnClickListener { renderCategory(category) }
        }
        railItems.add(category to frame)
        rail.addView(frame)
    }

    root.addView(LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(420))

        addView(ScrollView(context).apply {
            isVerticalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(dp(40), LinearLayout.LayoutParams.MATCH_PARENT)
            addView(rail)
        })
        addView(ScrollView(context).apply {
            isVerticalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                marginStart = dp(14)
            }
            addView(contentContainer)
        })
    })

    renderCategory(ComponentCategory.STRUCTURE)

    dialog.setOnDismissListener { onDismiss() }
    dialog.setContentView(root)
    dialog.behavior.skipCollapsed = true
    dialog.behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
    dialog.show()
}

/** Modal opened from the top bar's "..." button: sketch-level actions that aren't tied to a part. */
fun showMoreMenu(
    context: Context,
    hasParts: Boolean,
    onGeneratePrompt: () -> Unit,
    onExportProject: () -> Unit,
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
        text = "Sketch options"
        textSize = 18f
        setTypeface(typeface, Typeface.BOLD)
        setPadding(0, 0, 0, dp(12))
    })

    root.addView(MaterialButton(context).apply {
        text = "Generate prompt"
        isAllCaps = false
        isEnabled = hasParts
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )
        setOnClickListener {
            onGeneratePrompt()
            dialog.dismiss()
        }
    })

    root.addView(MaterialButton(context).apply {
        text = "Export project (.zip)"
        isAllCaps = false
        isEnabled = hasParts
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { topMargin = dp(8) }
        setOnClickListener {
            onExportProject()
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
