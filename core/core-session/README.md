# core-session

Data-слой сессий тренировки: доменные модели, контракты, реализация репозитория и доменные правила
прогрессии/итогов. Сессия — снапшот программы плюс фактический прогресс; данные общие для трекера
(пишет) и истории (читает), поэтому в core, а не в фиче.

## Публичный API

- Модели (`domain.model`): `WorkoutSessionEntity`, `WorkoutSessionItemEntity`, `SessionExerciseEntity`,
  `SessionSetEntity`, `SessionSetValueEntity`, `SessionSetResultEntity`, `SessionSetOutcomeEntity`,
  `SessionProgramEntity`.
- Контракты (`domain.repository`):
  - `SessionHistoryRepository` — узкий **read**-контракт (`getFinishedSessions`, `getSession`) для истории.
  - `WorkoutSessionRepository : SessionHistoryRepository` — **write**-поверхность трекинга (старт,
    completeSet/skipSet/saveSetNote/setCurrentExercise/finishSession, `getActiveSession`).
- Доменные правила: `SessionProgression` (`effectiveCurrentExercise`, `currentSet`, `hasPendingSets`),
  `SessionSetOutcomeUtils` (`outcome()`, `hasWeight`) — чистые функции, единый источник тонов трекинга и
  диффа истории.
- DI: `coreSessionModule()` — одна реализация под обоими интерфейсами (`WorkoutSessionRepository` и
  `SessionHistoryRepository`), `Clock.System` инъектируется здесь.

## Подключение

```kotlin
implementation(projects.core.coreSession)
```

Зависит от `:core:core-workout` (`api`, т.к. снапшот сессии несёт `WorkoutEntity`/`WorkoutExerciseEntity`),
`:core:core-db` (DAO), `:core:core-di`. `coreSessionModule()` регистрируется в `:shared` (`KoinInit`).

## Нюансы

- Инвариант «не более одной активной сессии» держит транзакция DAO (`:core:core-db`).
- ISP: история инжектит `SessionHistoryRepository`, трекер — `WorkoutSessionRepository`.

> При изменении исходников модуля проверь и при необходимости обнови этот README в том же коммите.
