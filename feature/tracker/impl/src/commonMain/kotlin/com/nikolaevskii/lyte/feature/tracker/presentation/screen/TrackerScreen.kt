package com.nikolaevskii.lyte.feature.tracker.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerUiState
import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.TrackerViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    TrackerContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerContent(
    state: TrackerUiState,
    onIntent: (TrackerIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Трекер") })
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Тренировок сегодня: ${state.completedWorkoutsToday}")
            Button(onClick = { onIntent(TrackerIntent.OpenWorkouts) }) {
                Text("К тренировкам")
            }
        }
    }
}

@Composable
@Preview
private fun TrackerContentPreview() {
    LyteTheme {
        TrackerContent(
            state = TrackerUiState(completedWorkoutsToday = 1),
            onIntent = {},
        )
    }
}
