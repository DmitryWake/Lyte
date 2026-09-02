# core-app

Data-слой состояния приложения: персистентные флаги старта поверх `:core:core-db`. Данные общие
(пишет сплэш, пишет и читает обучение), поэтому живут в core, а не в фиче: `:feature:*:api` по
CLAUDE.md § «Целевая структура» data-контрактом для других фич быть не должен, а `:core:core-db` —
это Room, а не доменные репозитории.

## Публичный API

- Контракт (`domain.repository`): `AppLaunchStateRepository` — `hasCompletedFirstLaunch()` /
  `markFirstLaunchCompleted()` и `hasCompletedOnboarding()` / `markOnboardingCompleted()`.
  - **Флаги независимы, общей записи «сохрани состояние целиком» нет намеренно.** У них разные
    писатели: сид библиотеки (`WorkoutLibraryInitializer` в `:feature:splash:impl`) отмечает первый
    запуск на сплэше, выход из обучения — обучение. Запись строки целиком вторым писателем обнулила
    бы `hasCompletedFirstLaunch`, и сид на следующем запуске засеял бы библиотеку повторно,
    продублировав все упражнения и программы. Ниже по стеку это держит `AppLaunchStateDao`:
    точечные `UPDATE` в транзакции, без `@Upsert`.
  - Флаги переживают удаление любых доменных данных: пользователь мог очистить библиотеку
    упражнений, и это не делает запуск первым.
- Тестовый двойник (`testing`): `FakeAppLaunchStateRepository(hasCompletedFirstLaunch,
  hasCompletedOnboarding)` — **единственный** фейк этого контракта в проекте, его берут тесты всех
  потребителей.
  - Лежит в `commonMain`, а не в `commonTest`: KMP 2.3 поддерживает `java-test-fixtures` только для
    JVM-таргета, а тестовые source set'ы между модулями не публикует, потребители же компилируются
    и под iOS. Второй фейк в каждом модуле обошёлся бы дороже — копии одного контракта расходятся
    молча, а ошибка именно в этом флаге приводит к дублированию библиотеки.
- DI: `coreAppModule()` — регистрирует реализацию репозитория (DAO приходит из `coreDbModule()`).
  - `CoreAppModuleTest` проверяет, что связана именно реализация поверх БД. Фейк лежит в
    `commonMain` вынужденно, значит виден и продакшен-коду: подмена в модуле прошла бы мимо тестов
    потребителей — те и так работают с фейком, — а `hasCompletedFirstLaunch()` начал бы всегда
    возвращать `false`.

Реализация (`data.repository.AppLaunchStateRepositoryImpl`) — `internal`; наружу отдаются только
контракт и фейк.

## Подключение

```kotlin
implementation(projects.core.coreApp)
```

`coreAppModule()` добавляется в список модулей Koin в `:shared` (`KoinInit`) **после**
`coreDbModule()` — репозиторий получает `AppLaunchStateDao` оттуда. Дополнительных зависимостей
модуль не требует: контракт из `suspend`-функций, ни Compose, ни `Flow` в нём нет.

## Пример

```kotlin
class OnboardingViewModel(
    private val appLaunchStateRepository: AppLaunchStateRepository,
) : BaseViewModel<OnboardingUiState, OnboardingIntent>() {

    private fun finish() {
        launch {
            appLaunchStateRepository.markOnboardingCompleted()
            lyteNavigator.navigate(route = TrackerLandingRoute)
        }
    }
}
```

В тесте потребителя:

```kotlin
val repository = FakeAppLaunchStateRepository(hasCompletedOnboarding = false)
```

> При изменении исходников модуля проверь и при необходимости обнови этот README в том же коммите.
