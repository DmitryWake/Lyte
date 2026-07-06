package com.nikolaevskii.lyte.feature.history.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistoryIntent
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistoryUiState
import com.nikolaevskii.lyte.feature.history.presentation.viewmodel.HistoryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HistoryContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
    state: HistoryUiState,
    onIntent: (HistoryIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("История") })
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            Text("Пока пусто")
        }
    }
}

@Composable
@Preview
private fun HistoryContentPreview() {
    LyteTheme {
        HistoryContent(
            state = HistoryUiState(),
            onIntent = {},
        )
    }
}
