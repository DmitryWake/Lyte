package com.nikolaevskii.lyte.feature.workout.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListUiState
import com.nikolaevskii.lyte.feature.workout.presentation.viewmodel.WorkoutListViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WorkoutListScreen(
    viewModel: WorkoutListViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    WorkoutListContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListContent(
    state: WorkoutListUiState,
    onIntent: (WorkoutListIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Тренировки") })
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.errorMessage != null -> Text(state.errorMessage)
                state.isLoading -> CircularProgressIndicator()
            }
        }
    }
}

@Composable
@Preview
private fun WorkoutListContentPreview() {
    LyteTheme {
        WorkoutListContent(
            state = WorkoutListUiState(isLoading = false),
            onIntent = {},
        )
    }
}
