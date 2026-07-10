package com.nikolaevskii.lyte.feature.workout.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

/**
 * Привязывает `ViewModel` вложенного composable к его времени жизни в композиции, а не к экрану.
 *
 * По умолчанию `LocalViewModelStoreOwner` — это `NavBackStackEntry` экрана, поэтому ViewModel
 * шторки пережила бы её закрытие: при повторном открытии пришло бы старое состояние — с прошлым
 * поисковым запросом и, что хуже, с уже выставленным терминальным результатом
 * (`ExercisePickerUiState.result`), который тут же сработал бы повторно и добавил упражнение
 * второй раз. Свой стор с очисткой в `onDispose` даёт шторке свежую ViewModel на каждое открытие.
 */
@Composable
internal fun SheetViewModelStoreOwner(content: @Composable () -> Unit) {
    val storeOwner = remember { SheetViewModelStoreOwnerImpl() }
    DisposableEffect(storeOwner) {
        onDispose { storeOwner.viewModelStore.clear() }
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides storeOwner, content = content)
}

private class SheetViewModelStoreOwnerImpl : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}
