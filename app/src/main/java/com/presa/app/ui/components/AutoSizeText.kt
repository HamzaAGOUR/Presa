package com.presa.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

/**
 * Text that shrinks its font size until it fits its width and [maxLines].
 *
 * Compose 1.7 does not expose `autoSize` on [Text], so the layout callback drives
 * the reduction. The size never goes below [minScale] of the supplied style;
 * ellipsis is only used when even that floor cannot fit the label.
 */
@Composable
fun AutoSizeText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    minScale: Float = 0.65f,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
) {
    val originalSize = style.fontSize
    val minSize = remember(style, minScale) {
        if (originalSize.isSpecified) originalSize * minScale else originalSize
    }
    var fittedStyle by remember(text, style) { mutableStateOf(style) }

    Text(
        text = text,
        modifier = modifier,
        style = fittedStyle,
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        softWrap = true,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { result ->
            val currentSize = fittedStyle.fontSize
            if (
                (result.didOverflowWidth || result.didOverflowHeight) &&
                currentSize.isSpecified &&
                minSize.isSpecified &&
                currentSize > minSize
            ) {
                val scaledSize = currentSize * 0.92f
                val nextSize = if (scaledSize.value < minSize.value) minSize else scaledSize
                val ratio = nextSize.value / originalSize.value
                fittedStyle = fittedStyle.copy(
                    fontSize = nextSize,
                    lineHeight = if (style.lineHeight.isSpecified) {
                        (style.lineHeight.value * ratio).sp
                    } else {
                        fittedStyle.lineHeight
                    },
                )
            }
        },
    )
}
