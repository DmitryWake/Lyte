package com.nikolaevskii.lyte.core.mvi

/**
 * Типизированная ошибка уровня презентации: то, что экран умеет показать пользователю, не завися от
 * сырого `Throwable.message` (Room/SQLite-текст, тексты `checkNotNull` и т.п. наружу не утекают —
 * экран мапит [LyteError] в локализованную строку через `stringResource`).
 *
 * Источник — [toLyteError]; воронка — `BaseViewModel.handleError`.
 */
sealed interface LyteError {

    /** Запрошенных данных нет (см. [LyteNotFoundException]). */
    data object NotFound : LyteError

    /** Сбой локального хранилища (см. [LyteStorageException]). */
    data object Storage : LyteError

    /** Всё прочее; [cause] — для логирования, но не для показа пользователю. */
    data class Unknown(val cause: Throwable) : LyteError
}

/** Доменная «не найдено»: репозиторий/VM бросает её вместо `checkNotNull`, чтобы получить [LyteError.NotFound]. */
class LyteNotFoundException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

/** Сбой локального хранилища: data-слой оборачивает в неё ошибку Room/SQLite, чтобы получить [LyteError.Storage]. */
class LyteStorageException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

/** Нормализует любой [Throwable] в [LyteError]. `CancellationException` сюда попадать не должна — её отсеивает воронка. */
fun Throwable.toLyteError(): LyteError = when (this) {
    is LyteNotFoundException -> LyteError.NotFound
    is LyteStorageException -> LyteError.Storage
    else -> LyteError.Unknown(cause = this)
}
