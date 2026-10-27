package com.presa.app.ui.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.presa.app.domain.model.KeyAction
import com.presa.app.domain.model.KeyChord
import com.presa.app.domain.model.KeyMaps
import com.presa.app.ui.components.ActionKeyButton
import com.presa.app.ui.components.KeyTone
import com.presa.app.ui.components.SectionLabel
import com.presa.app.ui.components.chordLabel

@Composable
fun ModeSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionLabel(text = title)
        Spacer(Modifier.height(9.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

/** Key button that always renders the effective (possibly remapped) chord. */
@Composable
fun PresaActionButton(
    action: KeyAction,
    overrides: Map<String, KeyChord>,
    enabled: Boolean,
    onPress: (KeyAction) -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
    tone: KeyTone = KeyTone.Neutral,
    big: Boolean = false,
    minHeight: Dp = 64.dp,
    icon: ImageVector? = null,
) {
    ActionKeyButton(
        label = stringResource(action.labelRes),
        hint = chordLabel(KeyMaps.effectiveChord(action, overrides)),
        icon = icon,
        tone = tone,
        enabled = enabled,
        big = big,
        minHeight = minHeight,
        modifier = modifier,
        onPress = { onPress(action) },
        onRelease = onRelease,
    )
}
