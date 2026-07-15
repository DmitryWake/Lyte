# Lyte

Lyte — фитнес-трекер на **Kotlin Multiplatform** (Android + iOS) с общим UI на **Compose Multiplatform**.
Приложение **offline-first**: все данные (программы, упражнения, сессии тренировок, история) хранятся
локально на устройстве, без сети, аккаунтов и аналитики. UI — русскоязычный.

Корневой пакет: `com.nikolaevskii.lyte`.

## Возможности

- **Трекер** — старт тренировки по программе, пошаговое выполнение подходов (готово/пропустить),
  секундомер сессии, досрочное завершение; активная сессия переживает перезапуск процесса.
- **Программы** — список программ, редактор (упражнения, порядок, подходы: повторения × вес),
  библиотека упражнений с поиском и созданием новых.
- **История** — завершённые сессии с группировкой по месяцам и экран деталей сессии (план против факта).
- **Splash** — первый запуск сидит библиотеку упражнений и стартовые программы.

## Архитектура

- **Multi-Module по фичам, api/impl-split**: каждая фича — пара модулей `feature/<name>/{api,impl}`.
  `:api` — pure KMP (`@Serializable` роуты и доменные контракты), `:impl` — Compose UI + ViewModel-и.
- **MVI** (`:core:core-mvi`): `UiState`/`UiIntent`, `BaseViewModel`, единый `onIntent`.
- **Навигация** (`:core:core-navigation`): типобезопасные роуты (Jetpack Navigation), команды через
  `LyteNavigator`; `NavController` живёт только в шелле (`:shared`).
- **DI** — Koin (`:core:core-di` + Koin-модуль на каждый модуль).
- **БД** — Room KMP (`:core:core-db`), `BundledSQLiteDriver`, offline-first.
- **Дизайн-система** — `:core:core-design` (`LyteTheme` + компонент-кит `Lyte*`).

Полное описание архитектуры, кодстайла и правил — в [CLAUDE.md](CLAUDE.md); у каждого `:core:*`-модуля
есть свой `README.md`.

## Сборка и запуск

**Требования:** JDK 11+, Android SDK (`compileSdk 36`), для iOS — macOS + Xcode.

- Android (debug APK): `./gradlew :androidApp:assembleDebug`
- Android (релиз AAB): `./gradlew :androidApp:bundleRelease` — требует ключа подписи
  (см. «Подпись релиза»).
- iOS: открыть `iosApp/iosApp.xcodeproj` в Xcode и запустить.

### Подпись релиза (Android)

Релизная сборка подписывается ключом из `keystore.properties` (в `.gitignore`) или из переменных
окружения (`LYTE_KEYSTORE_FILE`, `LYTE_KEYSTORE_PASSWORD`, `LYTE_KEY_ALIAS`, `LYTE_KEY_PASSWORD`).
Без них релиз собирается debug-ключом (годится для проверки, не для публикации). `keystore.properties`:

```
storeFile=/absolute/path/to/lyte-release.jks
storePassword=…
keyAlias=…
keyPassword=…
```

## Тесты

Unit-тесты (host/JVM):

```
./gradlew :core:core-mvi:testAndroidHostTest \
          :feature:tracker:impl:testAndroidHostTest \
          :feature:workout:impl:testAndroidHostTest \
          :feature:history:impl:testAndroidHostTest \
          :feature:splash:impl:testAndroidHostTest
```

Те же наборы на iOS-симуляторе — `:iosSimulatorArm64Test` у соответствующих модулей.

## Лицензии

Исходный код — проприетарный, см. [LICENSE](LICENSE).

Шрифты **Space Grotesk** и **Inter Tight** распространяются под **SIL Open Font License 1.1** —
[OFL.txt](core/core-design/src/commonMain/composeResources/files/OFL.txt).

## Конфиденциальность

Приложение не собирает и не передаёт данные — [docs/PRIVACY.md](docs/PRIVACY.md).
