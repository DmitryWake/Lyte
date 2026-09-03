package com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi

import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import com.nikolaevskii.lyte.core.design.component.session.LyteTrackSetState
import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionCurrentUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionLastSetLabel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionOverlayUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionSwitcherRowUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.toTrackSetStates
import kotlin.time.Instant

/**
 * Активная сессия (спека 4.3). Что отрисовать, решает [content] — sealed-состояние [ActiveSessionContent],
 * а не комбинация nullable-полей.
 *
 * Остальные поля — сквозные, ортогональные контенту: [startedAt] — источник секундомера (`null`, пока
 * сессия не загружена), [elapsedSeconds] пересчитывается от него по wall-clock каждый тик; [isMutating] —
 * guard от повторных записей по дабл-тапу, сбрасывается по завершении мутации, и он же гасит всё, чем
 * можно начать новую запись; [mutationError] — какая запись не удалась.
 *
 * [mutationError] живёт снаружи армов намеренно: писать умеют оба ([Tracking] — подход и заметку,
 * [AllDone] — финализацию), поэтому внутри [Tracking] поле было бы недостижимо для ошибки сохранения
 * с экрана-итога. Текст баннера выбирает операция, а не арм: досрочное завершение проваливается на
 * [Tracking] и обязано сказать про тренировку, а не про несохранённый подход.
 */
data class ActiveSessionUiState(
    val content: ActiveSessionContent = ActiveSessionContent.Loading,
    val startedAt: Instant? = null,
    val elapsedSeconds: Int = 0,
    val isMutating: Boolean = false,
    val mutationError: ActiveSessionMutationError? = null,
    val previousSetResults: Map<String, LyteSetValue> = emptyMap(),
) : UiState {

    /** Что экран показывает прямо сейчас: один `when` без вложенных проверок на `null`. */
    sealed interface ActiveSessionContent {

        /** Идёт загрузка сессии по id. */
        data object Loading : ActiveSessionContent

        /** Сессию не удалось загрузить: полноэкранная ошибка с повтором и уходом на лендинг. */
        data object Error : ActiveSessionContent

        /**
         * Трекинг текущего подхода. [current] гарантированно есть (есть незакрытый подход).
         * [draftReps]/[draftWeight] — черновик степперов.
         *
         * [trackSets] — те же подходы, но уже в виде состояний компонента списка, и [lastSetLabel] —
         * выбор подписи его хвоста. Оба считает маппер (`toTrackSetStates`/`lastSetLabel`), а не
         * экран: экрану остаётся отрисовать готовое и подставить строковый ресурс. Хранимые поля, а
         * не вычисляемые свойства: список пересобирается только при смене подхода или драфта, и
         * Compose не должен получать новый инстанс на каждую рекомпозицию.
         */
        data class Tracking(
            val current: ActiveSessionCurrentUiModel,
            val trackSets: List<LyteTrackSetState>,
            val lastSetLabel: ActiveSessionLastSetLabel?,
            val switcherRows: List<ActiveSessionSwitcherRowUiModel>,
            val draftReps: Int,
            val draftWeight: Double,
            val overlay: ActiveSessionOverlayUiModel,
        ) : ActiveSessionContent {

            /**
             * Правит черновик степпера и пересобирает [trackSets] под него — иначе фокус-карточка
             * показывала бы прежнее число. Единственный путь смены драфтов.
             */
            fun withDrafts(reps: Int = draftReps, weight: Double = draftWeight): Tracking = copy(
                trackSets = current.toTrackSetStates(draftReps = reps, draftWeight = weight),
                draftReps = reps,
                draftWeight = weight,
            )
        }

        /**
         * Все подходы разрешены: экран-итог со сводкой и кнопкой сохранения.
         *
         * [setTones] — по сегменту на каждый подход сессии, в порядке упражнений; считает маппер, а не
         * экран (то же правило, что для [Tracking.trackSets]). [completedCount] не выводится из тонов:
         * это «выполнено без пропущенных», и словом «выполнено» в сводке названо именно оно.
         */
        data class AllDone(
            val programName: String,
            val completedCount: Int,
            val totalCount: Int,
            val setTones: List<LyteProgressTone>,
        ) : ActiveSessionContent
    }
}

/**
 * Что именно не удалось записать. Различие не косметическое: провал записи подхода оставляет сессию в
 * работе («не удалось сохранить изменение»), провал финализации — говорит про тренировку целиком,
 * и оба исхода достижимы на обоих армах контента.
 */
enum class ActiveSessionMutationError {

    /** Подход, заметка, переключение упражнения — всё, что пишет прогресс внутри сессии. */
    Write,

    /** Завершение сессии: и досрочное с трекинга, и «Сохранить тренировку» с экрана-итога. */
    Finish,
}

sealed interface ActiveSessionIntent : UiIntent {

    /** Повторить загрузку после полноэкранной ошибки. */
    data object OnRetryClicked : ActiveSessionIntent

    /** Уйти на лендинг из полноэкранной ошибки (сессию не трогаем). */
    data object OnBackToLandingClicked : ActiveSessionIntent

    data class OnDraftRepsChanged(val reps: Int) : ActiveSessionIntent

    data class OnDraftWeightChanged(val weight: Double) : ActiveSessionIntent

    /** «Готово» — записать текущий подход с драфт-значениями степперов. */
    data object OnCompleteSetClicked : ActiveSessionIntent

    /** «Пропустить» — отметить текущий подход пропущенным. */
    data object OnSkipSetClicked : ActiveSessionIntent

    data object OnOpenExerciseSheetClicked : ActiveSessionIntent

    /** Ручной выбор упражнения в шторке; закрытые упражнения не выбираются. */
    data class OnExerciseSelected(val exerciseId: String) : ActiveSessionIntent

    data object OnOpenNoteSheetClicked : ActiveSessionIntent

    data class OnNoteDraftChanged(val text: String) : ActiveSessionIntent

    data object OnSaveNoteClicked : ActiveSessionIntent

    /** Крестик в шапке — показать диалог досрочного завершения. */
    data object OnEndEarlyClicked : ActiveSessionIntent

    /** Подтверждение досрочного завершения: оставшиеся подходы — пропущенные, уход на лендинг. */
    data object OnEndEarlyConfirmed : ActiveSessionIntent

    /** «Сохранить тренировку» на экране-итоге. */
    data object OnFinishClicked : ActiveSessionIntent

    /** Закрыть текущий оверлей (тап по скриму, свайп, «Отмена»). */
    data object OnDismissOverlay : ActiveSessionIntent
}
