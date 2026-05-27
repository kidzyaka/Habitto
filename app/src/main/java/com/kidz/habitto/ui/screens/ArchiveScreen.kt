package com.kidz.habitto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kidz.habitto.R
import com.kidz.habitto.viewmodel.HabitViewModel
import com.kidz.habitto.ui.theme.*

@Composable
fun ArchiveScreen(viewModel: HabitViewModel) {
    val archivedHabits by viewModel.archivedHabits.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.nav_archive),
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        if (archivedHabits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(stringResource(R.string.no_archived), color = HabittoOnSurfaceSecondary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(archivedHabits) { habit ->
                    HabitListItem(
                        habit = habit,
                        onToggleCompletion = { _ -> }, // Can't complete archived habits
                        onArchive = { viewModel.unarchiveHabit(habit.id) }, // Tapping archive icon unarchives
                        onResetRequest = {} // Can't reset archived
                    )
                }
            }
        }
    }
}
