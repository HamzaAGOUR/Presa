package com.presa.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presa.app.BuildConfig
import com.presa.app.R
import com.presa.app.ui.components.LocalHaptics
import com.presa.app.ui.components.LocalHapticsEnabled
import com.presa.app.ui.components.PresaCard
import com.presa.app.ui.components.PresaMark
import com.presa.app.ui.components.PresaScreenHeader
import com.presa.app.ui.components.SectionLabel
import com.presa.app.ui.theme.LocalPresaColors

private const val VISUALIZER_URL = "https://github.com/MehdiCherkane/dsa-visualizer"
private const val PRESA_URL = "https://github.com/HamzaAGOUR/Presa"

/**
 * About screen reachable from Settings.
 *
 * The presentation paragraph is shown in French first and English second in
 * every locale. Credit names are kept verbatim from the DSA Visualizer.
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalPresaColors.current

    Column(modifier = modifier.fillMaxSize()) {
        PresaScreenHeader(
            title = stringResource(R.string.about_title),
            onBack = onBack,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PresaCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PresaMark()
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.ink,
                        )
                        Text(
                            text = stringResource(R.string.app_tagline),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.muted,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.muted,
                )
                Text(
                    text = stringResource(R.string.settings_license),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.muted,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.about_description_fr),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.ink2,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.about_description_en),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted,
                )
            }

            PresaCard {
                SectionLabel(text = stringResource(R.string.about_section_credits))
                Spacer(Modifier.height(10.dp))
                stringArrayResource(R.array.about_credit_names).forEach { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.ink,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.about_institution),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.ink2,
                )
                Text(
                    text = stringResource(R.string.about_source),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                )
            }

            PresaCard {
                SectionLabel(text = stringResource(R.string.about_section_links))
                Spacer(Modifier.height(4.dp))
                AboutLink(
                    label = stringResource(R.string.about_link_visualizer),
                    url = VISUALIZER_URL,
                    onOpenUrl = onOpenUrl,
                )
                AboutLink(
                    label = stringResource(R.string.about_link_presa),
                    url = PRESA_URL,
                    onOpenUrl = onOpenUrl,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AboutLink(
    label: String,
    url: String,
    onOpenUrl: (String) -> Unit,
) {
    val colors = LocalPresaColors.current
    val haptics = LocalHaptics.current
    val hapticsEnabled = LocalHapticsEnabled.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) {
                haptics?.press(hapticsEnabled)
                onOpenUrl(url)
            }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = colors.navy,
            )
            Text(
                text = url,
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
            contentDescription = null,
            tint = colors.gold,
            modifier = Modifier.size(18.dp),
        )
    }
}
