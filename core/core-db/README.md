# core-db

Room KMP: единая локальная база приложения, DAO текущих доменных сущностей и Koin-модуль для их регистрации.

## Что внутри

- `LyteDatabase` — `@Database(entities = [...], version = 3, exportSchema = true)`, `@ConstructedBy(LyteDatabaseConstructor::class)`.
- `LyteDatabaseConstructor` — `expect object : RoomDatabaseConstructor<LyteDatabase>`; `actual`-реализация генерируется Room-компилятором (KSP) отдельно на каждой платформе.
- Схема тренировок (`db/workout/`, `@Entity`-классы — с суффиксом `*DatabaseEntity`):
  - `WorkoutDatabaseEntity` (`workout`), `ExerciseDatabaseEntity` (`exercise`) — тренировка и упражнение-библиотека (`id: String`, `name`, `description?`).
  - `WorkoutExerciseCrossRefDatabaseEntity` (`workout_exercise`) — упорядоченная связка «тренировка ↔ упражнение» (FK→`workout`/`exercise`, `ON DELETE CASCADE`, `position`).
  - `WorkoutSetDatabaseEntity` (`workout_set`) — подход внутри связки (FK→`workout_exercise` CASCADE, `position`, `count`, `weight?`).
  - `WorkoutWithExercises` / `WorkoutExerciseWithSets` — `@Relation`-POJО для чтения полного графа; порядок под-списков потребитель восстанавливает сортировкой по `position`.
  - `WorkoutDao` (`abstract class`) — `getItems`, `getWithExercises` (`@Transaction`), гранулярные upsert/insert/delete и `@Transaction saveWorkoutGraph(...)` (единый путь create/edit; для `workout`/`exercise` — `@Upsert`, а не `@Insert(REPLACE)`, иначе `INSERT OR REPLACE` снёс бы детей каскадом).
  - `ExerciseDao` — CRUD по упражнениям (`getAll`, `getById`, `@Upsert upsert`, `deleteById`).
- Состояние приложения (`db/app/`):
  - `AppLaunchStateEntity` (`app_launch_state`) — singleton-строка (`id = AppLaunchStateEntity.SINGLETON_ROW_ID`, всегда `0`), `hasCompletedFirstLaunch: Boolean`. Переживает удаление любых доменных данных (например, если пользователь очистит библиотеку упражнений) — используется как независимый от содержимого других таблиц маркер «первый запуск уже прошёл», а не эвристика вида «таблица X пуста».
  - `AppLaunchStateDao` — `get()` (singleton-строка или `null`, если ещё не создана), `@Upsert upsert(state)`.
- `applyLyteDefaults()` — расширение `RoomDatabase.Builder<T>`: `BundledSQLiteDriver`, `setQueryCoroutineContext(Dispatchers.IO)` и `fallbackToDestructiveMigration(dropAllTables = true)` (на стадии каркаса миграций нет — при смене схемы БД пересоздаётся, данные теряются).
- `coreDbModule()` — Koin-модуль: `single<LyteDatabase> { ... }` + `single<WorkoutDao>` + `single<ExerciseDao>` + `single<AppLaunchStateDao>`.
- `lyteDatabaseBuilder()` (`internal`, expect/actual) — платформенный билдер: на Android контекст берётся через `Koin.GlobalContext` (`androidDatabaseContext()`), на iOS путь — `NSDocumentDirectory` (`iosDatabaseFilePath()`).

## Использование

Подключение DAO в фиче — только через DI, без создания инстансов БД напрямую:

```kotlin
val featureWorkoutModule = module {
    // WorkoutDao / ExerciseDao приходят из coreDbModule()
    single<WorkoutRepository> { WorkoutRepositoryImpl(workoutDao = get()) }
    single<WorkoutExerciseRepository> { WorkoutExerciseRepositoryImpl(exerciseDao = get()) }
}
```

`coreDbModule()` подключается в общей точке инициализации Koin (`initKoinShared` в `:shared`):

```kotlin
modules(
    coreDbModule(),
    // ...
)
```

## Подключение

```kotlin
plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidxRoom)
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
```

```kotlin
implementation(projects.core.coreDb)
```

Модуль использует только `implementation(...)`-зависимости, ничего транзитивно не экспортирует. Потребитель **обязан** подключить сам:

- `libs.androidx.room.runtime` — типы Room (`@Entity`, `@Dao`, `@Query`, `RoomDatabase` и т.п.), если фича добавляет свои сущности/DAO в `LyteDatabase`.
- `libs.kotlinx.coroutines.core` — `Flow`-возвраты DAO, `suspend`-функции.
- `libs.koin.core` — доступ к `LyteDatabase`/DAO через `get()` в Koin-модуле фичи.

## Нюансы

- **KSP-компилятор Room подключается только per-target** (`add("kspAndroid"/"kspIosArm64"/"kspIosSimulatorArm64", ...)`) — общего `ksp(...)` для всех таргетов нет.
- `expect object LyteDatabaseConstructor` помечен `@Suppress("NO_ACTUAL_FOR_EXPECT")` — `actual` генерируется KSP и не виден IDE до первой сборки; это ожидаемо, не баг.
- `room { schemaDirectory(...) }` обязателен при `exportSchema = true` — без него KSP-таска падает. Схемы (`core/core-db/schemas/`) коммитятся в репозиторий, при любом изменении `version` или сущностей — новый файл схемы, старые не трогаем/не удаляем.
- Новые сущности/DAO кладём в `core/core-db/src/commonMain/.../db/<domain>/` рядом с существующими (`workout/`), регистрируем в `@Database(entities = [...])` и добавляем DAO-провайдер в `coreDbModule()`.
- Драйвер — только `BundledSQLiteDriver` (без нативных SQLite-биндингов), запросы выполняются на `Dispatchers.IO` по умолчанию (`applyLyteDefaults()`) — не переопределяй контекст выполнения запросов на билдере вручную без причины.

> При изменении исходников модуля проверь и при необходимости обнови этот README в том же коммите.
