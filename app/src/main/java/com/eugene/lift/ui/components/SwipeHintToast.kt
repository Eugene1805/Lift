package com.eugene.lift.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eugene.lift.R
import kotlinx.coroutines.delay

/**
 * A transient animated hint pill that tells the user they can swipe left to delete a set.
 * Appears at the bottom of the first set row and auto-dismisses after [displayMs] milliseconds.
 *
 * @param visible      True while the hint should be shown.
 * @param displayMs    How long the hint stays visible before fading out.
 * @param onDismiss    Called when the hint is done (use to mark hint as seen in DataStore).
 */
@Composable
fun SwipeHintToast(
    visible: Boolean,
    displayMs: Long = 3000L,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var show by remember { mutableStateOf(visible) }

    LaunchedEffect(visible) {
        if (visible) {
            show = true
            delay(displayMs)
            show = false
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = show,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(24.dp))
                .background(
                    MaterialTheme.colorScheme.inverseSurface,
                    RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.swipe_hint_delete_set),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface
            )
        }
    }
}
