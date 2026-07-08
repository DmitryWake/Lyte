package com.nikolaevskii.lyte.feature.workout.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
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
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsUiState
import com.nikolaevskii.lyte.feature.workout.presentation.viewmodel.WorkoutDetailsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun WorkoutDetailsScreen(
    id: Long,
    viewModel: WorkoutDetailsViewModel = koinViewModel { parametersOf(id) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    WorkoutDetailsContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailsContent(
    state: WorkoutDetailsUiState,
    onIntent: (WorkoutDetailsIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Тренировка") },
                navigationIcon = {
                    IconButton(onClick = { onIntent(WorkoutDetailsIntent.Back) }) {
                        Text("<")
                    }
                },
            )
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
private fun WorkoutDetailsContentPreview() {
    LyteTheme {
        WorkoutDetailsContent(
            state = WorkoutDetailsUiState(
                id = 1L,
                isLoading = false,
            ),
            onIntent = {},
        )
    }
}
