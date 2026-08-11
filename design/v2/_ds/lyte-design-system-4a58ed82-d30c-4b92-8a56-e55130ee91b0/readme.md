# Lyte — Design System

Lyte is a premium, offline-first strength-training tracker (Compose Multiplatform,
Android + iOS) with AI-assisted programming planned: fast logging mid-set,
autoregulation of load/reps, one-tap exercise substitutions. The product bet is a
beautiful, obvious interface — no tutorial videos needed — in the register of Apple
Fitness: calm, confident, one-handed.

**Current version: v2.** v1 was a 1:1 transfer of the Claude Design handoff
bundle, reconciled against the shipping Kotlin Multiplatform codebase for the
assets the handoff didn't carry (real fonts, real logo). It is frozen, viewable
and complete in `archive/v1/` — nothing there changes.

v2 answers one piece of feedback: the UX was logical but visually too busy —
"глаза разбегаются". Everything praised in v1 is untouched (the colour system,
the «Lyte.» wordmark, `BottomNav`). What changed is **information density**:

| | v1 | v2 |
|---|---|---|
| Card anchor | 22px/700 title | 52px colour `ExerciseMark`, title down to 16px/600 |
| Metadata | «5 упражнений · посл. сессия 2 июл» | one fact: «5 упражнений» |
| Planned sets | 4 numeric pills «10×60 кг» | `ProgressTrack` + «4 подхода» |
| Session result | «10 повт · 60 кг → 12 повт · 62,5 кг» | «12×62,5» + delta chip «+2 повт · +2,5 кг» |
| Session at a glance | «выполнено 15/16 подходов» | per-set bar chart: height = outcome vs. target |
| Hit target exactly | full row of numbers | no chip — nothing to report |
| «в прошлый раз» | header, competing with the set number | quiet caption under the steppers |
| Copy | telegraphic («Выбрать тренировку») | warm and short («С чего начнём?») |

The reference quality bar is Apple Fitness — generous whitespace, one hero
number per view, soft depth, colour as identity. **Not its patterns**: no rings,
no activity-graph vocabulary, no stacked-metric dashboards. Minimalism is the
differentiator; Apple's "everything at once" is the thing to avoid.

- Color system: Material 3 tonal-palette structure, so it drops directly onto a
  Compose Multiplatform `MaterialTheme` (`core-design/LyteTheme`).
- Visual reference: Apple Fitness crossed with contemporary AI-assisted apps
  (a distinct "smart" accent reserved for AI-touched surfaces).
- Components are M3-retargetable; custom components are reserved for moments that
  need more personality (session stopwatch, rest-timer overlay, target→actual diff
  rows, the set tracker).

## Sources

- **Claude Design handoff bundle** `Премиум фитнес приложение-handoff/` — the
  primary source. Contains `project/_ds/lyte-design-system-c40c504d…/`
  (styles.css, tokens/, readme.md and the compiled `_ds_bundle.js` of the previous
  design-system project) plus `project/ds-update/` — the audited component update
  (7 updated + 5 new components, 2 new color tokens) with its own README.
- **Handoff prototypes** — `Lyte - Прототип.html` (interactive click-through) and
  `Lyte - Экраны MVP.html` (21-screen canvas), read as the source of truth for
  component behaviour and copy, but not carried into this system — it ships
  foundations and components, not screens. `Lyte - Обновления ДС (превью).html`
  was the component-update preview.
- **Lyte codebase** (local mount, read-only) — Kotlin Multiplatform repo, module
  `core/core-design` (`LyteTheme`, `LyteIcons`, `LyteButton`…`LyteExerciseStrip`).
  Its README documents the same token and component inventory, implemented in
  Compose. Real font binaries and the app icon were copied from it.
- **Store screenshots** — `store/screenshots/01…08-*.png` in the codebase (active
  session, history, session detail, programs, program editor, preview, workout
  pick, tracker).
- Original product brief: Russian-language MVP UX spec, «УХ-спецификация MVP —
  трекер силовых тренировок v1.0» (glossary, navigation, screens 3.1–5.2).
  No Figma file exists.

### What changed during the transfer (nothing visual)

1. **Fonts are no longer a substitution.** The handoff loaded Space Grotesk /
   Inter Tight from the Google Fonts CDN and flagged them as guesses. They are in
   fact the brand's real, OFL-licensed faces: the binaries are bundled in the app
   (`core-design/…/composeResources/font/`), copied here to `assets/fonts/` and
   declared in `tokens/fonts.css`. Same families, same scale — no CDN dependency.
2. **The logo exists.** The handoff said "no logo file provided"; the codebase
   ships the real wordmark «Lyte.» (ink + lime dot) as the app icon. Copied to
   `assets/`, shown in the Brand cards. Nothing was drawn or approximated.
3. Everything else — every color value, type step, radius, shadow, motion token
   and component implementation — is transferred verbatim.

## Fonts

- **Space Grotesk** (400/500/600/700) — all UI text *and* all numerals. Live
  numbers (stopwatch, steppers, countdown) use it with
  `font-variant-numeric: tabular-nums` and weight 600–700, never a dev-tool mono.
- **Inter Tight 700** — the «Lyte.» wordmark only (`--font-wordmark`).

---

## Content fundamentals

Copy is in **Russian**, written for a solo lifter mid-set — short, literal,
zero flourish. Patterns observed/derived from the spec:

- **Warm, short, and never bossy** (v2). Headlines may ask a friendly question
  — «С чего начнём?», «На сегодня хватит?» — and completion speaks like a
  training partner: «Готово. Отдыхай». Buttons stay bare verbs, as short as
  they can be: «Начать», «Готово», «Пропустить», «Сохранить тренировку».
  Prefer one warm word over a correct-but-cold phrase: «Начать» beats «Начать
  тренировку» when the program name is already on screen.
- **No «Вы» or «Ты» in labels.** The UI names the action, not the person. The
  exception is deliberate warmth in a headline («Отдыхай»), never in a control.
- **Relative dates in lists**: «вчера», «3 дня назад» — absolute dates
  («2 июля · начало 18:24») only in a detail view.
- **One number, not a table** (v2 correction). v1's numeric-shorthand voice was
  right but overused: «выполнено 14/15 подходов» next to «52 мин» next to a row
  of «10×60 кг» pills gave every card four numbers. Keep the compact notation
  («12×62,5», «Упражнение 2 из 5»), but **one per element** — the rest becomes
  a `ProgressTrack` or moves one tap deeper. Ratios like «14/15» are now a
  track, not text.
- **No exclamation points, no congratulatory copy, no emoji.** A completed
  set doesn't say «Отлично!» — it just shows the diff. The tone is a quiet,
  competent training partner, not a hype coach.
- **Confirmations are plain questions**, not warnings dressed up: destructive
  actions (delete program, end session early) get a direct confirm, e.g.
  «Удалить программу «Push Day»?» — name the object, ask once.
  («удалить» not «уничтожить»; never scare-case or all-caps.) The object being
  acted on is named, not "this item".
- **Empty states are one line + one action**, no illustration copy needed
  beyond a simple mark: «Создайте первую программу» / «Здесь появятся ваши
  тренировки» — states the absence, then immediately offers the fix.
  No jokes, no "nothing here yet 👀" tone.
- Labels favor **domain nouns over generic UI nouns**: «Подход» not «Строка»,
  «Целевые» / «Фактические» not «План» / «Результат». Keep the glossary
  (упражнение / программа / сессия / подход) consistent everywhere — never
  swap in a casual synonym.
- Units are always spelled the app's way: «кг», «повт», «мин», «сек»; decimals
  use a comma («62,5 кг»).

## Visual foundations

- **Colour is identity** (v2). Six accents — `--accent-coral` · `indigo` ·
  `lime` · `amber` · `teal` · `slate` — carried by the `ExerciseMark` circle
  on every card, row and picker item, so a list is scanned by colour before a
  word is read.
  **These are not muscle groups.** The app stores no muscle-group data and
  none is planned: colour and pictogram are plain properties of an exercise.
  Seed exercises ship pre-coloured; anything the user creates, the user
  colours, picking from these six. `slate` is the default, so an exercise
  created without a choice still looks deliberate. Six is the whole palette —
  enough to tell a list apart, few enough to stay a considered choice rather
  than a colour picker. Amber and teal are the only two colours in the system
  not present in the handoff bundle; they are interpolated in oklch between
  the existing hues (see `tokens/accents.css`).
- **Colour.** Material 3 tonal-palette architecture end to end: a primary
  (energetic lime — "spotting" you through a set), secondary (warm coral —
  effort/heart-rate, echoing Apple Fitness' Exercise ring without copying its
  ring system), tertiary (cool indigo, reserved *only* for AI-touched
  surfaces — auto-progression suggestions, smart substitutions — so users
  learn to associate that hue with "the AI decided this"), and a semantic
  success green layered on top for target/actual diffs. Everything else is a
  desaturated warm-neutral surface stack (`surface` → `surface-container-highest`)
  so color reads as signal, not decoration. No gradients anywhere — flat
  fills only, per Material 3 and to avoid generic "AI slop" gradient washes.
- **Diff tones.** Five, and only five, result tones: `positive` (exceeded —
  saturated green), `met` (exactly on target — soft green tint), `negative`
  (below), `neutral` (plan/target), `skipped` (grey, renders «—»).
- **One set outcome, one look, everywhere** (v2). The five result states —
  `hit` (ровно в цель) · `exceed` (больше) · `miss` (меньше) · `skip` ·
  `todo` — share one tone vocabulary across `TrackSetRow`, `DiffRow` and
  `ProgressTrack`. In row form the icon carries the direction: a tick for
  exactly-on-target, an **up/down arrow** for above/below, a minus for skipped.
  Never a cross — below target is a direction, not a failure, and an ✗ beside a
  set the lifter actually completed reads as punishment.
- **The tracking screen is `ExerciseSetList`** (v2): every set in order, the
  current one an elevated focus card with steppers, the rest compact 36px rows
  carrying a bare index digit. The card is **sticky to the bottom of the scroll
  area** — a focus card is ~200px and past six or seven sets a plain list
  outgrows the space between header and dock, and in a plain scroller the card
  (the only element the lifter touches) scrolls off screen. Sticky keeps it in
  the thumb zone at any set count: completed sets scroll behind it, upcoming
  sets stay below in true order, and on a short list sticky is inert.
  A horizontal carousel of set plaques (`SetOverview`) was the alternative and
  was removed: two components for one piece of information left consumers
  guessing, it asks a sideways swipe of someone standing under a bar, and it
  cannot show a result and a target in the same glance.
- **Outcome is encoded by HEIGHT, not just hue** (v2). In `ProgressTrack`'s
  `tones` mode each set is a bar against the target: exceeded spikes to full
  height, met sits at the mid line, below-target dips short, skipped is a
  hollow outline. Five states cannot be told apart by colour alone at 5px —
  two greens (met vs. exceeded) are indistinguishable — and a legend would
  defeat a glanceable summary. Reuse this encoding for any future
  target-vs-actual visual.
- **Type.** One family: **Space Grotesk** for everything — headlines, body,
  labels, and the live numeric reads (stopwatch, steppers, countdown), the
  latter always with `font-variant-numeric: tabular-nums` and weight 600–700
  so digits are bold, expressive, and don't reflow as they tick. The wordmark
  alone keeps its Inter Tight cut (`--font-wordmark`).
- **Spacing.** Strict 4px base grid (`--space-1` = 4px … `--space-24` =
  96px), matching Compose `Dp` conventions directly.
- **Marks, not imagery** (v2). `ExerciseMark` is the one visual anchor, and it
  carries **two signals: colour = the exercise's accent, glyph = the movement**.
  Legible at every size, so a 36px picker row gets the same treatment as a
  52px card. No photography, no hero imagery, no full-bleed, no illustration
  anywhere in the product.
  Graded exercise photos were built and then retired in favour of icons — they
  live in `archive/photos/` with their grading recipe, and `ExerciseMark` still
  accepts `image` at size >= 48, so bringing them back is a data change.
- **Backgrounds.** Flat surface fills only — no imagery, no full-bleed
  photography, no hand-drawn illustration, no repeating pattern/texture. This
  is a utility tool used mid-set, one-handed; the background must never
  compete with the current number on screen.
- **Corner radii.** Generous and modern: pills (`full`) for buttons, chips,
  and the floating nav dock; 20–28px (`large-increased`–`extra-large`) for
  cards; 32px (`extra-large-increased`) for sheets. Bigger container =
  bigger radius.
- **Shadows / elevation.** Soft, diffuse, low-alpha shadows
  (`--elevation-1`…`5`, large blur radii) replace hairline borders as the
  primary separation device — cards are borderless fills on a subtly tinted
  background. Elevation rises for overlays (sheets, dialogs, the floating
  nav). No inner shadows, no neumorphism.
- **Borders.** Rare. Cards are borderless; a 1.5px `outline-variant` stroke
  appears only on outlined buttons and focused text fields; a 1px
  `outline-variant` hairline separates `ListRow`s.
- **Animation.** M3 motion tokens (`standard` / `emphasized` easing,
  150/250/400ms durations). Transitions are quick, purposeful, and never
  bouncy or springy — this is a training tool, not a playful consumer app.
  The rest-timer countdown is the one deliberately "alive" element (a
  continuously-updating ring/number); everything else fades or slides at
  `--motion-duration-short`–`medium`.
- **Hover states** (desktop/tablet use): surface lightens toward
  `surface-container-high`; text/icon-only controls gain a faint circular
  hover backdrop, per M3 state-layer convention (8% on-surface overlay; 12%
  on press).
- **Press states:** buttons and steppers scale down slightly (0.94–0.97)
  with an M3 state-layer color shift — a tactile, contemporary press signal.
  Steppers additionally flash a filled/inverted `primary` state so a
  one-handed, eyes-elsewhere tap gets a confidence signal.
- **Transparency / blur:** the floating bottom nav dock is translucent
  (`surface-container-lowest` at 82%) with `backdrop-filter: blur(20px)`;
  sheet scrims use `scrim` at 48%, dialog scrims at 32%. No frosted glass
  anywhere else. (The Compose build approximates the dock blur with a
  semi-transparent fill — noted in `core-design`.)
- **Imagery:** none in MVP scope (no photography, no onboarding illustration)
  per the UX spec's explicit "вне скоупа" list. Empty states use a simple
  icon mark in a 112px `primary-container` circle, not a stock illustration.
- **Cards** (v2): borderless `surface-container-lowest` fills, soft
  `--elevation-1`, 20–28px radius, 12–16px padding, 16px/600 titles with
  `-0.2px` tracking. Anatomy is fixed: colour mark → title → **one** quiet
  fact (12px `on-surface-variant`) → optional hero number on the trailing
  edge → optional full-width track. Nothing else goes in a card.
- **Hit targets:** never below 48px (`--hit-target-min`); stepper buttons are
  56px (`--stepper-target`), and stacked steppers share one vertical tap axis
  via a constant `--stepper-width`.

## Iconography

**Two open-source sets, one grid.** Both are 24×24, 2px stroke, round caps, so
they mix without a visible seam. `Icon` takes `set="lucide" | "tabler"` and
renders either through a CSS mask, so a glyph tints to any token colour (an
`<img>` would bake in black and disappear on dark surfaces).
Sizes in use: 17 / 18 / 20 / 22 / 24 / 48 px.

**Lucide — the UI vocabulary.** Confirmed by the codebase: `core-design` depends
on `com.composables:icons-lucide-cmp` and exposes a deliberately narrow, curated
set as `LyteIcons`. Chevrons, actions, status, empty-state marks.

**Exercise pictograms — 10 movements, Flaticon.** Line art in Flaticon's
"Special Flat" style (author Icongeek26), 512×512 PNG, in
`assets/icons/exercises/` and rendered by `ExerciseIcon`.

| | | |
|---|---|---|
| `squat` Присед | `deadlift` Становая | `bench-press` Жим лёжа |
| `pull-up` Подтягивания | `dumbbell-press` Жим гантелей | `curl` Сгибания |
| `crunch` Пресс | `stretch` Растяжка | `rack` Рама |
| `machine` Тренажёр | | |

**Attribution is required** — the Flaticon free licence needs a visible credit
wherever the icons ship (an About / Licences screen). Exact wording and the
source link are in `ATTRIBUTION.md`; keep them in any build. A paid Flaticon
plan removes the requirement, and ten glyphs is a small enough set to
commission outright if that is ever preferable.

Drawn through a **CSS mask, not `<img>`**: the sources are black line art, and
the mask makes each a silhouette tinted by `color`, so one file works on a
light card and inside a saturated accent circle alike. Paths resolve from the
bundle's own `<script src>`, so a consuming page configures nothing.

The tradeoff, accepted knowingly: **CSS masks don't survive DOM-rasterising
export** — a PNG or PPTX export of a card renders these glyphs as solid
squares. Tinting is non-negotiable (black line art vanishes on dark-theme
surfaces, where the accent containers are dark), and this is a mobile-app
system where exporting a card into a slide is not a real workflow. Screenshot
the live page if a deck ever needs these marks.

**Gaps, left honest:** there is no glyph for overhead press, dips or lunges.
Those reuse the nearest correct pictogram rather than one showing
the wrong movement — an approximated glyph is worse than a shared one. Add the
files and the keys become available with no code change.

Two earlier attempts are worth not repeating: borrowed sets (Lucide, Tabler,
Iconoir, Phosphor, MDI) have no figures holding equipment, and their
near-misses read wrong — a pull-up bar that renders as a playground slide, a
rowing figure standing in for a cable machine; and hand-drawn pictograms did
not reach shippable quality.

No icon font or sprite sheet is shipped. **No emoji, and no unicode
glyphs-as-icons, anywhere in the product** — the "quiet training partner" tone
rules them out. Prefer a curated icon over an invented one: if a needed glyph
isn't in the list above, add it to the curated set deliberately rather than
reaching for a decorative alternative.

---

## Index

- `styles.css` — root stylesheet, import list only.
- `tokens/fonts.css` — `@font-face` for the bundled Space Grotesk / Inter Tight.
- `tokens/colors.css` — M3 reference + system color roles, light + dark, plus the
  diff/AI semantic aliases.
- `tokens/typography.css` — M3 type-role scale + numeric/tabular scale, font stacks.
- `tokens/spacing.css` — 4px spacing grid, shape (radius) scale, elevation,
  motion tokens, hit targets.
- `guidelines/` — foundation specimen cards (Colors, Type, Spacing, Brand groups
  in the Design System tab): `colors-brand`, `colors-semantic`, `colors-surfaces`,
  `type-display`, `type-body`, `type-numeric`, `spacing-scale`, `spacing-shape`,
  `spacing-elevation`, `colors-accents`, `brand-wordmark`, `brand-app-icon`,
  `brand-exercise-icons`, `brand-ai-accent`.
- `assets/` — real app icon (`app-icon-1024[-dark|-tinted].png`,
  `ic_launcher_foreground/monochrome/round.png`), `assets/fonts/` (5 ttf), and
  `assets/icons/exercises/` (10 Flaticon PNGs — **attribution required**).
- `tokens/accents.css` — the six exercise accent colours.
- `archive/v1/` — frozen v1 foundations: tokens, entry stylesheet and the v1
  component sources as `.jsx.txt`. Nothing there compiles into the bundle.
- `archive/photos/` — the retired graded exercise photos + grading recipe.
- `components/` — 27 reusable primitives, grouped by concern:
  - `core/` — Button, IconButton, Icon, ExerciseIcon, ExerciseMark, ProgressTrack, Chip, Badge, Switch, TextField, Overline (+ `exerciseIcons.js`, `plural.js` helpers)
  - `forms/` — AccentPicker, ExerciseIconPicker (colour + pictogram for an exercise)
  - `stepper/` — Stepper (rep/weight ± control)
  - `cards/` — ProgramCard, ExerciseCard, SessionCard, ListRow
  - `feedback/` — DiffRow (target→actual), Dialog, EmptyState
  - `navigation/` — BottomNav, TopBar
  - `overlays/` — BottomSheet (puller), RestTimerOverlay
  - `data-display/` — SessionStopwatch (numeric hero display)
  - `session/` — ExerciseSetList (tracking-screen composition), TrackSetRow, ExerciseStrip, SetDots
- `ATTRIBUTION.md` — third-party licences; the Flaticon credit is mandatory.
- `SKILL.md` — Agent-Skills front matter for using this system in Claude Code.

Each component directory carries `<Name>.jsx`, `<Name>.d.ts`, `<Name>.prompt.md`
and one `@dsCard` HTML showing its variants.

## Logo / brand mark

The real wordmark is **«Lyte.»** — Inter Tight 700, near-black ink with a lime
(`primary`) full stop, on the cream `surface` (#FBFDF4). Files in `assets/`
(app icon in light/dark/tinted, plus the transparent adaptive foreground and the
themed monochrome). The dot is always lime; nothing else is drawn.

## Intentional additions

Components not literally named in the UX spec, added because the flows require
them (all present in both the handoff update and the shipping codebase):

- **Icon** — wrapper over the Lucide glyph set, so every icon in the system is
  sized and tinted one way.
- **IconButton** — spec calls for icon-only taps (note, overflow, close) with no
  described visual; standard M3 icon button.
- **Switch / TextField** — generic form primitives needed by exercise creation
  (3.3) and note input (4.3).
- **EmptyState** — two empty-state copy strings (3.1, 5.1) with no described
  visual; one reusable pattern for both.
- **Overline** — the pervasive small-caps micro-label (list section headers,
  stepper captions, «Упражнение 2 из 5»). Its label names the field, not the
  unit — «ВЕС» over the stepper, «кг» beside the number.
- **session/** — ExerciseSetList, TrackSetRow, ExerciseStrip, SetDots: the active
  session (4.3) needed these as real primitives instead of screen-local code.
  `ExerciseSetList` additionally encodes the screen's layout decision (sticky
  focus card) so every consumer gets it right by default.

---

## Caveats & ask

1. **`RestTimerOverlay` is built but shown nowhere.** It's in the UX spec (4.3)
   and implemented in both this system and the Compose codebase, but it was
   taken off the overlays specimen card, so nothing in the Design System tab
   displays it. Say the word and it's deleted outright — or it stays as a
   spec'd component waiting for the rest-timer flow.
2. **The `met` tone lives in `DiffRow` and `TrackSetRow` only.** Should `Badge`
   and the diff icons get it too?
3. **Codebase-side components with no web counterpart:** `LyteSetEditRow` (plan
   one set in the program editor) and the `LyteExerciseCard` `Preview(index)`
   variant. They exist in `core-design` but not in the handoff bundle, so they
   were not invented here. Say the word and they'll be added AS IS from Compose.
4. **No screens ship here, by design.** The system's job is the foundations,
   components and rules a prototype is assembled *from* — not the prototype.
   The interactive flow and the 21-screen canvas that used to live in
   `ui_kits/` were removed for that reason. If a reusable starting scaffold is
   ever wanted, the right shape is a `templates/<slug>/` entry, not a UI kit.
5. **The exercise model needs two new fields:** `color` (one of six accents)
   and `exercise` (one of the ten pictograms). Seed exercises ship with
   both filled in; the create/edit exercise screen needs a picker for each,
   which the UX spec does not cover yet — worth designing before build.
   `ListRow`'s subtitle carries the exercise's **description**, since there is
   no category data to show there.
6. **No Figma file exists** — if one is ever made, reconcile it against this.
