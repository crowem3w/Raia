package org.example.test

/**
 * One category in the Components picker's left-hand icon rail.
 *
 * [kinds] are the parts shown in the grid when this category is selected; a category with no
 * mapped [PartKind] yet (e.g. Prototype) just shows an empty-state message.
 */
enum class ComponentCategory(
    val label: String,
    val iconRes: Int,
    val kinds: List<PartKind>,
) {
    STRUCTURE("Structure", R.drawable.ic_components, listOf(PartKind.TOP_APP_BAR, PartKind.NAV_BAR, PartKind.CARD)),
    LAYOUT("Layout", R.drawable.ic_cat_layout, emptyList()),
    TYPOGRAPHY("Typography", R.drawable.ic_cat_typography, listOf(PartKind.TEXT)),
    SHAPES("Shapes", R.drawable.ic_shapes, listOf(PartKind.CARD, PartKind.IMAGE, PartKind.CHIP)),
    MEDIA("Media", R.drawable.ic_media, listOf(PartKind.IMAGE)),
    NAVIGATION("Navigation", R.drawable.ic_cat_navigation, listOf(PartKind.TOP_APP_BAR, PartKind.NAV_BAR)),
    INPUT("Input", R.drawable.ic_cat_input, listOf(PartKind.TEXT_FIELD, PartKind.CHECKBOX, PartKind.SWITCH)),
    ACTIONS("Actions", R.drawable.ic_cat_actions, listOf(PartKind.BUTTON, PartKind.FAB, PartKind.CHIP)),
    CONTENT("Content", R.drawable.ic_cat_content, listOf(PartKind.TEXT, PartKind.CARD, PartKind.IMAGE)),
    FEEDBACK("Feedback", R.drawable.ic_cat_feedback, emptyList()),
    MOBILE("Mobile", R.drawable.ic_cat_mobile, listOf(PartKind.TOP_APP_BAR, PartKind.NAV_BAR)),
    PROTOTYPE("Prototype", R.drawable.ic_link, emptyList()),
}
