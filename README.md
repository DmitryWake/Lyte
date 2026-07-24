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
- Android (релизный APK, формат для RuStore): `./gradlew :androidApp:assembleRelease` — требует ключа
  подписи (см. «Подпись релиза»).
- Android (релизный AAB, формат для Google Play): `./gradlew :androidApp:bundleRelease`.
- iOS: открыть `iosApp/iosApp.xcodeproj` в Xcode и запустить.

### Версия

Единственный источник — `version.properties` в корне (`lyte.versionName`, `lyte.versionCode`), оттуда её
читает `androidApp/build.gradle.kts`. Правила бампа — в [CLAUDE.md](CLAUDE.md#версионирование).

### Подпись релиза (Android)

Релизная сборка подписывается ключом из `keystore.properties` в корне проекта — файл в `.gitignore`,
ни он, ни keystore в репозиторий не коммитятся. Без него релизные задачи **падают с ошибкой**:
молчаливого отката на debug-ключ нет намеренно, чтобы неподходящая для публикации сборка не уехала в
стор незамеченной.

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
          :core:core-workout:testAndroidHostTest \
          :core:core-session:testAndroidHostTest \
          :feature:tracker:impl:testAndroidHostTest \
          :feature:workout:impl:testAndroidHostTest \
          :feature:history:impl:testAndroidHostTest \
          :feature:splash:impl:testAndroidHostTest
```

Те же наборы на iOS-симуляторе — `:iosSimulatorArm64Test` у соответствующих модулей.

Оба набора плюс сборки Android и iOS гоняются в CI на каждом pull request (`.github/workflows/ci.yml`).

## Лицензии

Исходный код — проприетарный, см. [LICENSE](LICENSE).

Шрифты **Space Grotesk** и **Inter Tight** распространяются под **SIL Open Font License 1.1** —
[OFL.txt](core/core-design/src/commonMain/composeResources/files/OFL.txt).

## Конфиденциальность

Приложение не собирает и не передаёт данные — [docs/PRIVACY.md](docs/PRIVACY.md).

Опубликованная версия (её адрес указывается в карточке приложения в сторах) поднимается GitHub Pages из
папки `docs/` ветки `master`.
