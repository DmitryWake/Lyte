# core-workout

Data-слой библиотеки упражнений и программ тренировок: доменные модели, репозитории и их реализации
поверх `:core:core-db`. Общие данные приложения (пишет редактор программ, читают трекер и сид сплэша),
поэтому живут в core, а не в фиче.

## Публичный API

- Модели (`domain.model`): `WorkoutEntity`, `WorkoutItemEntity`, `WorkoutExerciseEntity`,
  `WorkoutExerciseWithRepsEntity`, `WorkoutRepEntity`.
- Контракты (`domain.repository`): `WorkoutRepository` (программы), `WorkoutExerciseRepository`
  (библиотека упражнений).
- DI: `coreWorkoutModule()` — регистрирует реализации репозиториев (DAO приходят из `coreDbModule()`).

Реализации (`data.*`) — `internal`; наружу отдаются только интерфейсы и доменные модели.

## Подключение

```kotlin
implementation(projects.core.coreWorkout)
```

`coreWorkoutModule()` добавляется в список модулей Koin в `:shared` (`KoinInit`). Модуль зависит от
`:core:core-db` (DAO) и `:core:core-di`.

## Нюансы

- Удаление программы/упражнения — **soft-delete** (архивирование), если на них ссылаются другие записи;
  подробности инвариантов — в `:core:core-db` README.

> При изменении исходников модуля проверь и при необходимости обнови этот README в том же коммите.
