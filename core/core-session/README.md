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
  `SessionSetOutcomeUtils` (`outcome()`, `hasWeight`), `SessionPlanProgression`
  (`WorkoutSessionEntity.applyProgressionTo(workout)`) — чистые функции, единый источник тонов трекинга,
  диффа истории и прогрессии плана.
- DI: `coreSessionModule()` — одна реализация под обоими интерфейсами (`WorkoutSessionRepository` и
  `SessionHistoryRepository`), `Clock.System` инъектируется здесь. `WorkoutRepository` для прогрессии
  плана приходит из `coreWorkoutModule()`.

## Подключение

```kotlin
implementation(projects.core.coreSession)
```

Зависит от `:core:core-workout` (`api`, т.к. снапшот сессии несёт `WorkoutEntity`/`WorkoutExerciseEntity`),
`:core:core-db` (DAO), `:core:core-di`. `coreSessionModule()` регистрируется в `:shared` (`KoinInit`).

## Нюансы

- **Маркеры (цвет и знак) в сессии — по-разному.** Маркер программы снапшотится в строку сессии
  вместе с именем (`SessionProgramEntity.accent/glyph`): карточка истории обязана пережить
  переименование, перекраску и удаление программы. Маркер упражнения не снапшотится — приезжает
  живым из библиотеки тем же join'ом, что имя и описание, и меняется в истории вслед за библиотекой.
- Инвариант «не более одной активной сессии» держит транзакция DAO (`:core:core-db`).
- ISP: история инжектит `SessionHistoryRepository`, трекер — `WorkoutSessionRepository`.
- **Прогрессия плана.** `finishSession(id)` после завершения сессии подтягивает цели программы под
  факты: выполненный подход задаёт новую цель (и вверх, и вниз), пропущенный и невыполненный цель не
  меняют. Сессия — снапшот, поэтому упражнения и подходы сопоставляются по позициям и упражнение
  принимается только при совпадении id упражнения-библиотеки: правка программы во время сессии не
  испортит чужие цели. Пишется узким `WorkoutRepository.updateWorkoutTargets` — структура программы и
  её архивность не меняются. Программы уже нет — прогрессии просто нет, завершение не падает.

> При изменении исходников модуля проверь и при необходимости обнови этот README в том же коммите.
