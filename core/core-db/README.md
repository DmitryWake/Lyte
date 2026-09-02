# core-db

Room KMP: единая локальная база приложения, DAO текущих доменных сущностей и Koin-модуль для их регистрации.

## Что внутри

- `LyteDatabase` — `@Database(entities = [...], version = 2, exportSchema = true)`, `@ConstructedBy(LyteDatabaseConstructor::class)`.
- `LyteDatabaseConstructor` — `expect object : RoomDatabaseConstructor<LyteDatabase>`; `actual`-реализация генерируется Room-компилятором (KSP) отдельно на каждой платформе.
- Схема тренировок (`db/workout/`, `@Entity`-классы — с суффиксом `*DatabaseEntity`):
  - `WorkoutDatabaseEntity` (`workout`), `ExerciseDatabaseEntity` (`exercise`) — тренировка и упражнение-библиотека (`id: String`, `name`, `name_normalized`, `description?`).
  - `accent`/`glyph` (у `workout` и `exercise`) — маркер: цвет и знак движения. Хранятся строковыми ключами доменных `ExerciseAccent`/`ExerciseGlyph` из `:core:core-workout` (не `ordinal`: набор значений будет расширяться, и порядок не должен быть частью формата хранения). Колонки `NOT NULL DEFAULT 'slate'`/`'squat'`; неизвестный ключ читающая сторона заменяет дефолтом, поэтому расширение набора не требует миграции. У программы маркер **свой**, а не выведенный из первого упражнения.
  - `WorkoutExerciseCrossRefDatabaseEntity` (`workout_exercise`) — упорядоченная связка «тренировка ↔ упражнение» (FK→`workout`/`exercise`, `ON DELETE CASCADE`, `position`).
  - `WorkoutSetDatabaseEntity` (`workout_set`) — подход внутри связки (FK→`workout_exercise` CASCADE, `position`, `count`, `weight?`).
  - `is_archived` (у `workout` и `exercise`) — soft delete: сущность, на которую ссылаются сессии (и, для упражнения, программы), не удаляется физически, а прячется из списков/библиотеки (иначе повисли бы `workout_session.program_id` / `session_exercise.exercise_id`). Списки/поиск фильтруют `WHERE is_archived = 0`, чтение по id — нет.
  - `WorkoutWithExercises` / `WorkoutExerciseWithSets` — `@Relation`-POJО для чтения полного графа; порядок под-списков потребитель восстанавливает сортировкой по `position`.
  - `WorkoutItemWithExerciseCount` — плоский POJO для списка: `id`/`name`/`description` тренировки + агрегированное `exerciseCount` (без загрузки графа упражнений/подходов).
  - `WorkoutSetTargetUpdate` — плоский POJO точечного обновления цели подхода: подход адресуется позициями (`exercisePosition`/`setPosition`), а не id строки.
  - `WorkoutDao` (`abstract class`) — `getItems` (агрегирующий `@Query` с `LEFT JOIN workout_exercise` + `COUNT(...)` + `WHERE is_archived = 0`, возвращает `WorkoutItemWithExerciseCount`), `getWithExercises` (`@Transaction`, архивные тоже отдаёт), гранулярные upsert/insert/delete, `@Transaction saveWorkoutGraph(...)` (единый путь create/edit; для `workout`/`exercise` — `@Upsert`, а не `@Insert(REPLACE)`, иначе `INSERT OR REPLACE` снёс бы детей каскадом), `@Transaction updateSetTargets(workoutId, targets)` и `@Transaction deleteOrArchiveWorkout(id)` (если есть ссылающиеся сессии — `archiveWorkout`, иначе жёсткий `deleteWorkout`).
  - `updateSetTargets(...)` — путь прогрессии по итогам сессии: правит только `workout_set.count`/`weight` по позициям. Отдельно от `saveWorkoutGraph` намеренно — тот пересоздаёт связки и апсертит строки `workout`/`exercise` целиком, сбрасывая им `is_archived` (то есть воскресил бы удалённую программу). Позиции, которых в программе нет (её отредактировали после старта сессии), обновляют ноль строк — это не ошибка.
  - `ExerciseDao` (`abstract class`) — CRUD по упражнениям (`search`, `getById`, `@Upsert upsert`, `deleteById`) + soft delete `@Transaction deleteOrArchiveExercise(id)` (если есть ссылающиеся программы/сессии — `archiveExercise`, иначе жёсткий `deleteById`). `search(normalizedQuery)` фильтрует `is_archived = 0` + по подстроке названия и сортирует по нему же; пустой запрос отдаёт всю неархивную библиотеку. `getById` архивные тоже отдаёт (нужно программам/сессиям резолвить упражнение по id). Фильтрация и сортировка — в SQL, потребитель списки в памяти не перебирает.
- Схема сессий трекинга (`db/session/`) — фактический прогресс тренировки, снапшот программы на момент старта:
  - `WorkoutSessionDatabaseEntity` (`workout_session`) — сессия: `program_id`/`program_name`/`program_accent`/`program_glyph` (снапшот, FK на `workout` **нет** — сессия переживает удаление/архив программы), `started_at`/`finished_at` (epoch millis; `finished_at IS NULL` — активная), `current_exercise_id?`. Индексы на `program_id`, `finished_at`. Маркер упражнения, в отличие от маркера программы, НЕ снапшотится — читается живым из `exercise` вместе с именем.
  - `SessionExerciseDatabaseEntity` (`session_exercise`) — упражнение сессии (FK→`workout_session` CASCADE, `position`). Имя/описание НЕ снапшотятся: читаются живыми из `exercise` по `exercise_id` (join в `SessionExerciseWithSets`) — переименование упражнения отражается и в истории. FK→`exercise` (без каскада) гарантирует, что строка упражнения жива, пока на неё ссылается сессия (удаление при ссылках запрещено — упражнение архивируется). В отличие от программы (`program_id` без FK, имя снапшотится): у упражнения выбрано живое имя, поэтому нужна ссылочная целостность.
  - `SessionSetDatabaseEntity` (`session_set`) — подход (FK→`session_exercise` CASCADE, `position`): план (`target_count`/`target_weight?`) и факт (`result_status?` — `null`=pending / `COMPLETED` / `SKIPPED` через companion-константы, без `TypeConverter`; `result_count?`/`result_weight?`) + `note`.
  - `SessionWithExercises` / `SessionExerciseWithSets` — `@Relation`-POJО полного графа; `FinishedSessionSetRow` — плоская проекция подхода завершённой сессии (`session_id` + `@Embedded` строка подхода) для трека истории; `ProgramSetHistoryRow` — то же плюс координаты упражнения (`exercise_id`, `exercise_position`), под ориентир «в прошлый раз».
  - `WorkoutSessionDao` (`abstract class`) — чтения (`getActiveSession`/`getSession`/`getFinishedSessions`/`getFinishedSessionSets`/`getProgramSetHistory`/`countActiveSessions` + `Flow`-версии двух чтений истории), гранулярные `@Query`-UPDATE прогресса, `deleteSession(id)` (упражнения и подходы уходят каскадом; программу и библиотеку не задевает — FK на `workout` у сессии нет), `@Transaction insertSessionGraph(...)` — **держит инвариант «не более одной активной сессии»** (`check(countActiveSessions() == 0)` внутри транзакции, `@throws IllegalStateException`), `@Transaction finishSession(id, finishedAt)` (pending-подходы → `SKIPPED`, затем `finished_at`). Список истории читается **двумя** запросами (сессии + подходы всех сессий разом), а не запросом на карточку: исход подхода считает домен (`:core:core-session`), а не SQL.
  - `getProgramSetHistory(programId)` — подходы всех завершённых сессий **одной** программы, свежие сессии первыми (`ORDER BY finished_at DESC, session_exercise.position, session_set.position`), фильтр по двум индексированным колонкам (`program_id`, `finished_at`). Отдаёт строки как есть, включая пропущенные подходы: сопоставление «тот же подход» (упражнение + номер вхождения + позиция подхода) считает `:core:core-session` в Kotlin — без CTE и оконных функций, иначе правило проверялось бы только в `androidHostTest` этого модуля.
- Состояние приложения (`db/app/`):
  - `AppLaunchStateEntity` (`app_launch_state`) — singleton-строка (`id = AppLaunchStateEntity.SINGLETON_ROW_ID`, всегда `0`), `hasCompletedFirstLaunch: Boolean`. Переживает удаление любых доменных данных (например, если пользователь очистит библиотеку упражнений) — используется как независимый от содержимого других таблиц маркер «первый запуск уже прошёл», а не эвристика вида «таблица X пуста».
  - `AppLaunchStateDao` — `get()` (singleton-строка или `null`, если ещё не создана), `@Upsert upsert(state)`.
- Миграции (`db/migration/`):
  - `MIGRATION_1_2` — маркеры: `ALTER TABLE ADD COLUMN` для `exercise`/`workout`/`workout_session`, затем `UPDATE` строк стартовой библиотеки и стартовых программ по стабильным `seed-*` id (иначе у пользователя v1 вся библиотека осталась бы серой) и заполнение снапшота сессии из программы, на которую она ссылается (`COALESCE` до дефолта, если программу уже удалили). Id-шники сидов дублируют `:feature:splash:impl` — core-модуль БД не может зависеть от фичи; расхождение делает `UPDATE` пустым, но не ломает миграцию.
  - Тест — `Migration1To2Test` в `androidHostTest`: БД v1 собирается по DDL из закоммиченной `schemas/…/1.json`, наполняется и мигрируется на **настоящей** SQLite. Драйвер в тесте — `AndroidSQLiteDriver` под Robolectric, а не `BundledSQLiteDriver`: JNI бандла собран под Android и на host-JVM падает с `UnsatisfiedLinkError`.
- `applyLyteDefaults()` — расширение `RoomDatabase.Builder<T>`: `BundledSQLiteDriver`, `setQueryCoroutineContext(Dispatchers.IO)` и `addMigrations(*LYTE_MIGRATIONS)`. Деструктивного пересоздания БД (`fallbackToDestructiveMigration`) **нет** — оно стирало бы историю тренировок пользователя при бампе схемы.
- `coreDbModule()` — Koin-модуль: `single<LyteDatabase> { ... }` + `single<WorkoutDao>` + `single<ExerciseDao>` + `single<WorkoutSessionDao>` + `single<AppLaunchStateDao>`.
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

Host-тесты модуля (`androidHostTest`) дополнительно тянут `libs.androidx.sqlite.framework`, `libs.robolectric` и `libs.junit` — ради тестов на настоящей SQLite: миграция (`Migration1To2Test`) и SQL нетривиальных запросов DAO (`WorkoutSessionDaoProgramHistoryTest` — предикаты и порядок `getProgramSetHistory`, которые фейк DAO в `:core:core-session` повторяет вручную и потому доказать не может).

## Нюансы

- **KSP-компилятор Room подключается только per-target** (`add("kspAndroid"/"kspIosArm64"/"kspIosSimulatorArm64", ...)`) — общего `ksp(...)` для всех таргетов нет.
- `expect object LyteDatabaseConstructor` помечен `@Suppress("NO_ACTUAL_FOR_EXPECT")` — `actual` генерируется KSP и не виден IDE до первой сборки; это ожидаемо, не баг.
- `room { schemaDirectory(...) }` обязателен при `exportSchema = true` — без него KSP-таска падает. Схемы (`core/core-db/schemas/`) коммитятся в репозиторий.
- **Текущая версия схемы — `2`.** История до первого релиза была схлопнута до `1.json`, дальше версии только растут. Любое изменение схемы обязано: (1) поднять `version`, (2) добавить `Migration`-объект в `db/migration/` и в `LYTE_MIGRATIONS` (`RoomBuilderDefaults.kt`), (3) закоммитить новый файл схемы (старые не трогаем), (4) добавить тест миграции по закоммиченным схемам — эталон `Migration1To2Test`. Деструктивный сброс в релизе запрещён.
- **Дефолт колонки объявляется в двух местах и обязан совпадать**: `@ColumnInfo(defaultValue = ...)` у сущности и `DEFAULT` в DDL миграции, иначе Room не примет схему при открытии БД. Общие значения лежат в `MarkerDefaults.kt` в SQL-виде (вместе с кавычками) и подставляются в оба места.
- **Регистр в SQLite работает только для ASCII.** `LIKE`, `lower()` и коллация `NOCASE` не знают ничего про кириллицу: «жим» не найдёт «Жим лёжа», а строчное название уедет в конец `ORDER BY`. Поэтому под поиск/сортировку заводим служебную колонку с заранее нормализованным (`lowercase`) значением — как `exercise.name_normalized`, — а регистр запроса приводим на стороне Kotlin. Строки, которые пользователь вводит и которые попадают в `LIKE`, обязаны экранироваться (`%`, `_`, `\`) — в запросе для этого есть `ESCAPE '\'`.
- Новые сущности/DAO кладём в `core/core-db/src/commonMain/.../db/<domain>/` рядом с существующими (`workout/`), регистрируем в `@Database(entities = [...])` и добавляем DAO-провайдер в `coreDbModule()`.
- Драйвер — только `BundledSQLiteDriver` (без нативных SQLite-биндингов), запросы выполняются на `Dispatchers.IO` по умолчанию (`applyLyteDefaults()`) — не переопределяй контекст выполнения запросов на билдере вручную без причины.

> При изменении исходников модуля проверь и при необходимости обнови этот README в том же коммите.
