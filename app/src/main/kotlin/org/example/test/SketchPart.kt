package org.example.test

/** A part placed on the sketch canvas. Coordinates and size are in real device pixels. */
data class SketchPart(
    val id: Long,
    val kind: PartKind,
    var x: Float,
    var y: Float,
    var w: Float,
    var h: Float,
    var label: String = "",
)
