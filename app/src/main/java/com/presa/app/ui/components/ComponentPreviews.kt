package com.presa.app.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material.icons.rounded.ViewWeek
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.presa.app.R
import com.presa.app.domain.model.ThemeMode
import com.presa.app.ui.theme.PresaTheme

/**
 * Label-focused previews: every font scale/locale/theme/orientation combination
 * renders the large navigation keys, segmented tabs, long action labels and the
 * numeric keypad. Nothing here has a fixed text height.
 */
@Composable
private fun LabelPreviewLab(themeMode: ThemeMode) {
    val shiftF = "${stringResource(R.string.key_mod_shift)}+F"
    PresaTheme(themeMode = themeMode) {
        PresaBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionKeyButton(
                        label = stringResource(R.string.visualizer_prev),
                        hint = stringResource(R.string.visualizer_prev_hint),
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        tone = KeyTone.Previous,
                        big = true,
                        minHeight = 112.dp,
                        onPress = {},
                        modifier = Modifier.weight(1f),
                    )
                    ActionKeyButton(
                        label = stringResource(R.string.visualizer_next),
                        hint = stringResource(R.string.visualizer_next_hint),
                        icon = Icons.AutoMirrored.Rounded.ArrowForward,
                        tone = KeyTone.Next,
                        big = true,
                        minHeight = 112.dp,
                        onPress = {},
                        modifier = Modifier.weight(1f),
                    )
                }

                SegmentedControl(
                    options = listOf(
                        SegmentOption("visualizer", stringResource(R.string.mode_visualizer)),
                        SegmentOption("slides", stringResource(R.string.mode_slides)),
                    ),
                    selectedId = "visualizer",
                    onSelect = {},
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionKeyButton(
                        label = stringResource(R.string.visualizer_presentation),
                        hint = "P",
                        icon = Icons.Rounded.Slideshow,
                        onPress = {},
                        modifier = Modifier.weight(1f),
                    )
                    ActionKeyButton(
                        label = stringResource(R.string.visualizer_panels),
                        hint = "D",
                        icon = Icons.Rounded.ViewWeek,
                        onPress = {},
                        modifier = Modifier.weight(1f),
                    )
                    ActionKeyButton(
                        label = stringResource(R.string.visualizer_fit),
                        hint = "0",
                        onPress = {},
                        modifier = Modifier.weight(1f),
                    )
                }

                ActionKeyButton(
                    label = stringResource(R.string.visualizer_fullscreen),
                    hint = shiftF,
                    icon = Icons.Rounded.Fullscreen,
                    onPress = {},
                    modifier = Modifier.fillMaxWidth(),
                )

                NumberPadCard(
                    title = stringResource(R.string.slides_goto_slide),
                    enabled = true,
                    onSend = {},
                )
            }
        }
    }
}

@Preview(name = "FR clair portrait 1.0", locale = "fr", fontScale = 1f, widthDp = 360, heightDp = 900, uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "FR clair paysage 1.0", locale = "fr", fontScale = 1f, widthDp = 800, heightDp = 360, uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "FR sombre portrait 1.0", locale = "fr", fontScale = 1f, widthDp = 360, heightDp = 900, uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "FR sombre paysage 1.0", locale = "fr", fontScale = 1f, widthDp = 800, heightDp = 360, uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "EN clair portrait 1.0", locale = "en", fontScale = 1f, widthDp = 360, heightDp = 900, uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "EN clair paysage 1.0", locale = "en", fontScale = 1f, widthDp = 800, heightDp = 360, uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "EN sombre portrait 1.0", locale = "en", fontScale = 1f, widthDp = 360, heightDp = 900, uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "EN sombre paysage 1.0", locale = "en", fontScale = 1f, widthDp = 800, heightDp = 360, uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LabelPreviewFont100() = LabelPreviewLab(
    themeMode = ThemeMode.SYSTEM,
)

@Preview(name = "FR clair portrait 1.3", locale = "fr", fontScale = 1.3f, widthDp = 360, heightDp = 900, uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "FR clair paysage 1.3", locale = "fr", fontScale = 1.3f, widthDp = 800, heightDp = 360, uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "FR sombre portrait 1.3", locale = "fr", fontScale = 1.3f, widthDp = 360, heightDp = 900, uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "FR sombre paysage 1.3", locale = "fr", fontScale = 1.3f, widthDp = 800, heightDp = 360, uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "EN clair portrait 1.3", locale = "en", fontScale = 1.3f, widthDp = 360, heightDp = 900, uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "EN clair paysage 1.3", locale = "en", fontScale = 1.3f, widthDp = 800, heightDp = 360, uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "EN sombre portrait 1.3", locale = "en", fontScale = 1.3f, widthDp = 360, heightDp = 900, uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "EN sombre paysage 1.3", locale = "en", fontScale = 1.3f, widthDp = 800, heightDp = 360, uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LabelPreviewFont130() = LabelPreviewLab(
    themeMode = ThemeMode.SYSTEM,
)

@Preview(name = "FR clair portrait 2.0", locale = "fr", fontScale = 2f, widthDp = 360, heightDp = 900, uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "FR clair paysage 2.0", locale = "fr", fontScale = 2f, widthDp = 800, heightDp = 360, uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "FR sombre portrait 2.0", locale = "fr", fontScale = 2f, widthDp = 360, heightDp = 900, uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "FR sombre paysage 2.0", locale = "fr", fontScale = 2f, widthDp = 800, heightDp = 360, uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "EN clair portrait 2.0", locale = "en", fontScale = 2f, widthDp = 360, heightDp = 900, uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "EN clair paysage 2.0", locale = "en", fontScale = 2f, widthDp = 800, heightDp = 360, uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "EN sombre portrait 2.0", locale = "en", fontScale = 2f, widthDp = 360, heightDp = 900, uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "EN sombre paysage 2.0", locale = "en", fontScale = 2f, widthDp = 800, heightDp = 360, uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LabelPreviewFont200() = LabelPreviewLab(
    themeMode = ThemeMode.SYSTEM,
)
