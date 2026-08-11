/* @ds-bundle: {"format":4,"namespace":"LyteDesignSystem_4a58ed","components":[{"name":"ExerciseCard","sourcePath":"components/cards/ExerciseCard.jsx"},{"name":"ListRow","sourcePath":"components/cards/ListRow.jsx"},{"name":"ProgramCard","sourcePath":"components/cards/ProgramCard.jsx"},{"name":"SessionCard","sourcePath":"components/cards/SessionCard.jsx"},{"name":"Badge","sourcePath":"components/core/Badge.jsx"},{"name":"Button","sourcePath":"components/core/Button.jsx"},{"name":"Chip","sourcePath":"components/core/Chip.jsx"},{"name":"ExerciseIcon","sourcePath":"components/core/ExerciseIcon.jsx"},{"name":"ACCENTS","sourcePath":"components/core/ExerciseMark.jsx"},{"name":"ACCENT_NAMES","sourcePath":"components/core/ExerciseMark.jsx"},{"name":"ExerciseMark","sourcePath":"components/core/ExerciseMark.jsx"},{"name":"Icon","sourcePath":"components/core/Icon.jsx"},{"name":"IconButton","sourcePath":"components/core/IconButton.jsx"},{"name":"Overline","sourcePath":"components/core/Overline.jsx"},{"name":"ProgressTrack","sourcePath":"components/core/ProgressTrack.jsx"},{"name":"Switch","sourcePath":"components/core/Switch.jsx"},{"name":"TextField","sourcePath":"components/core/TextField.jsx"},{"name":"EXERCISE_ICONS","sourcePath":"components/core/exerciseIcons.js"},{"name":"EXERCISE_ICON_ORDER","sourcePath":"components/core/exerciseIcons.js"},{"name":"EXERCISES","sourcePath":"components/core/plural.js"},{"name":"SETS","sourcePath":"components/core/plural.js"},{"name":"SessionStopwatch","sourcePath":"components/data-display/SessionStopwatch.jsx"},{"name":"Dialog","sourcePath":"components/feedback/Dialog.jsx"},{"name":"DiffRow","sourcePath":"components/feedback/DiffRow.jsx"},{"name":"EmptyState","sourcePath":"components/feedback/EmptyState.jsx"},{"name":"AccentPicker","sourcePath":"components/forms/AccentPicker.jsx"},{"name":"ExerciseIconPicker","sourcePath":"components/forms/ExerciseIconPicker.jsx"},{"name":"BottomNav","sourcePath":"components/navigation/BottomNav.jsx"},{"name":"TopBar","sourcePath":"components/navigation/TopBar.jsx"},{"name":"BottomSheet","sourcePath":"components/overlays/BottomSheet.jsx"},{"name":"RestTimerOverlay","sourcePath":"components/overlays/RestTimerOverlay.jsx"},{"name":"ExerciseSetList","sourcePath":"components/session/ExerciseSetList.jsx"},{"name":"ExerciseStrip","sourcePath":"components/session/ExerciseStrip.jsx"},{"name":"SetDots","sourcePath":"components/session/SetDots.jsx"},{"name":"TrackSetRow","sourcePath":"components/session/TrackSetRow.jsx"},{"name":"Stepper","sourcePath":"components/stepper/Stepper.jsx"}],"sourceHashes":{"components/cards/ExerciseCard.jsx":"f83dd84e0417","components/cards/ListRow.jsx":"cbeb0ad6d2de","components/cards/ProgramCard.jsx":"3d016cefadda","components/cards/SessionCard.jsx":"4d2fe5e1e522","components/core/Badge.jsx":"83d70397bf7c","components/core/Button.jsx":"e6dffd629c1f","components/core/Chip.jsx":"b3a8f86ad7df","components/core/ExerciseIcon.jsx":"021b3568f4ee","components/core/ExerciseMark.jsx":"e8d9442bedfc","components/core/Icon.jsx":"fa72e08d5464","components/core/IconButton.jsx":"66cabf1c56c0","components/core/Overline.jsx":"044fca459903","components/core/ProgressTrack.jsx":"75fa8ae40cf9","components/core/Switch.jsx":"56c9cad4e215","components/core/TextField.jsx":"f658cf761f5a","components/core/exerciseIcons.js":"021e1d20662f","components/core/plural.js":"7b90ecad01d2","components/data-display/SessionStopwatch.jsx":"1a6676f4e9dd","components/feedback/Dialog.jsx":"00bcaea1b0eb","components/feedback/DiffRow.jsx":"fe589fb7997a","components/feedback/EmptyState.jsx":"9306e6cd53f2","components/forms/AccentPicker.jsx":"b3e81c374d21","components/forms/ExerciseIconPicker.jsx":"1d6d80fd3934","components/navigation/BottomNav.jsx":"5a28b915ebad","components/navigation/TopBar.jsx":"20291d50014b","components/overlays/BottomSheet.jsx":"5d9592a3a56a","components/overlays/RestTimerOverlay.jsx":"cb95885ea238","components/session/ExerciseSetList.jsx":"9af8c4247aeb","components/session/ExerciseStrip.jsx":"170a19df7582","components/session/SetDots.jsx":"34a5ea478aa7","components/session/TrackSetRow.jsx":"8a4407fd7747","components/stepper/Stepper.jsx":"964347cb26ee"},"inlinedExternals":[],"unexposedExports":[{"name":"plural","sourcePath":"components/core/plural.js"}]} */

(() => {

const __ds_ns = (window.LyteDesignSystem_4a58ed = window.LyteDesignSystem_4a58ed || {});

const __ds_scope = {};

(__ds_ns.__errors = __ds_ns.__errors || []);

// components/core/Badge.jsx
try { (() => {
const tones = {
  neutral: {
    bg: "var(--md-sys-color-surface-container-highest)",
    fg: "var(--md-sys-color-on-surface-variant)"
  },
  primary: {
    bg: "var(--md-sys-color-primary-container)",
    fg: "var(--md-sys-color-on-primary-container)"
  },
  success: {
    bg: "var(--diff-positive-bg)",
    fg: "var(--diff-positive)"
  },
  ai: {
    bg: "var(--ai-accent-container)",
    fg: "var(--ai-accent)"
  }
};
const sizes = {
  // Compact inline pill — set/exercise counts sitting next to a title.
  small: {
    h: 22,
    pad: "0 8px",
    font: "var(--type-label-small-size)",
    weight: 500
  },
  // Stat pill — the larger tabular metadata pill used in session detail
  // ("52 мин", "15/16"). Promoted from inline one-offs across the screens.
  medium: {
    h: 30,
    pad: "0 14px",
    font: "13px",
    weight: 600
  }
};

/**
 * Badge — small pill for compact metadata (set count, exercise count,
 * duration, completion fraction). Not a notification-dot badge.
 * `size="medium"` is the tabular "stat pill" (e.g. duration / sets summary).
 */
function Badge({
  children,
  tone = "neutral",
  size = "small",
  style
}) {
  const t = tones[tone];
  const s = sizes[size] || sizes.small;
  return /*#__PURE__*/React.createElement("span", {
    style: {
      display: "inline-flex",
      alignItems: "center",
      height: s.h,
      padding: s.pad,
      borderRadius: "var(--shape-full)",
      background: t.bg,
      color: t.fg,
      fontFamily: "var(--font-numeric)",
      fontVariantNumeric: "tabular-nums",
      fontSize: s.font,
      fontWeight: s.weight,
      ...style
    }
  }, children);
}
Object.assign(__ds_scope, { Badge });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Badge.jsx", error: String((e && e.message) || e) }); }

// components/core/Button.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const sizes = {
  // Large / hero CTA — the bottom-anchored primary action on session and
  // form screens ("Начать тренировку", "Готово", "Завершить тренировку").
  // Promoted into the component after every screen was hand-overriding
  // `style={{ height: 64, fontSize: 17 }}` on the medium button.
  large: {
    h: 64,
    pad: "0 40px",
    font: "17px",
    radius: "var(--shape-full)"
  },
  medium: {
    h: 56,
    pad: "0 28px",
    font: "16px",
    radius: "var(--shape-full)"
  },
  small: {
    h: 44,
    pad: "0 20px",
    font: "var(--type-label-large-size)",
    radius: "var(--shape-full)"
  }
};
const variants = {
  filled: accent => ({
    background: `var(--md-sys-color-${accent})`,
    color: `var(--md-sys-color-on-${accent})`,
    border: "none",
    boxShadow: "var(--elevation-2)"
  }),
  tonal: accent => ({
    background: `var(--md-sys-color-${accent}-container)`,
    color: `var(--md-sys-color-on-${accent}-container)`,
    border: "none"
  }),
  outlined: () => ({
    background: "transparent",
    color: "var(--md-sys-color-on-surface)",
    border: "1.5px solid var(--md-sys-color-outline-variant)"
  }),
  text: () => ({
    background: "transparent",
    color: "var(--md-sys-color-primary)",
    border: "none"
  })
};

/**
 * Button — M3-style action button (filled / tonal / outlined / text).
 * `accent` picks which color role drives filled/tonal variants (primary by
 * default; use "secondary" or "error" for destructive/alternate actions).
 * `size="large"` is the 64px bottom-anchored hero CTA used across the app.
 */
function Button({
  children,
  variant = "filled",
  accent = "primary",
  size = "medium",
  icon,
  disabled = false,
  fullWidth = false,
  onClick,
  style,
  ...rest
}) {
  const s = sizes[size] || sizes.medium;
  const v = variants[variant](accent);
  const [press, setPress] = React.useState(false);
  return /*#__PURE__*/React.createElement("button", _extends({
    onClick: disabled ? undefined : onClick,
    disabled: disabled,
    onMouseDown: () => setPress(true),
    onMouseUp: () => setPress(false),
    onMouseLeave: () => setPress(false),
    style: {
      height: s.h,
      padding: s.pad,
      borderRadius: s.radius,
      fontFamily: "var(--font-brand)",
      fontSize: s.font,
      fontWeight: 600,
      letterSpacing: "0.1px",
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      gap: 8,
      width: fullWidth ? "100%" : undefined,
      cursor: disabled ? "default" : "pointer",
      opacity: disabled ? 0.38 : 1,
      transform: press && !disabled ? "scale(0.97)" : "scale(1)",
      transition: "background-color var(--motion-duration-short) var(--motion-easing-standard), transform var(--motion-duration-short) var(--motion-easing-standard)",
      ...v,
      ...style
    }
  }, rest), icon, children);
}
Object.assign(__ds_scope, { Button });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Button.jsx", error: String((e && e.message) || e) }); }

// components/core/Chip.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * Chip — M3 filter/assist chip. Used for quick-tag set notes ("тяжело",
 * "легко", "боль") and filter affordances.
 */
function Chip({
  children,
  selected = false,
  onClick,
  icon,
  style,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("button", _extends({
    onClick: onClick,
    style: {
      height: 38,
      padding: "0 16px",
      borderRadius: "var(--shape-full)",
      border: "none",
      background: selected ? "var(--md-sys-color-secondary-container)" : "var(--md-sys-color-surface-container)",
      color: selected ? "var(--md-sys-color-on-secondary-container)" : "var(--md-sys-color-on-surface-variant)",
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-label-large-size)",
      fontWeight: 600,
      display: "inline-flex",
      alignItems: "center",
      gap: 6,
      cursor: "pointer",
      transition: "background-color var(--motion-duration-short) var(--motion-easing-standard)",
      ...style
    }
  }, rest), icon, children);
}
Object.assign(__ds_scope, { Chip });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Chip.jsx", error: String((e && e.message) || e) }); }

// components/core/Icon.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const SETS = {
  lucide: n => `https://unpkg.com/lucide-static@latest/icons/${n}.svg`,
  tabler: n => `https://unpkg.com/@tabler/icons@latest/icons/outline/${n}.svg`,
  mdi: n => `https://unpkg.com/@mdi/svg@latest/svg/${n}.svg`
};

/**
 * Icon — the single icon primitive. Renders one glyph by name from any of the
 * three open-source sets the brand uses:
 *   - "lucide" (default) — the UI vocabulary: chevrons, actions, status.
 *     24×24, 2px stroke, round caps.
 *   - "tabler" — same grid and weight as Lucide; used where its glyph reads
 *     better than the Lucide equivalent.
 *   - "mdi" (Material Design Icons, Apache 2.0) — SOLID silhouettes, used only
 *     for the exercise-type marks. Deliberately a different weight class: at
 *     22–28px inside a filled circle a solid figure reads instantly where a
 *     2px outline turns to mush, and it keeps "what exercise" visually
 *     separate from "what action".
 *
 * Uses a CSS mask (not <img>) so the glyph is a true silhouette tinted by
 * `color` — required for the flat black source SVGs to stay visible against
 * dark and coloured backgrounds.
 */
function Icon({
  name,
  set = "lucide",
  size = 24,
  color = "currentColor",
  style,
  ...rest
}) {
  const url = (SETS[set] || SETS.lucide)(name);
  return /*#__PURE__*/React.createElement("span", _extends({
    role: "img",
    "aria-hidden": "true",
    style: {
      display: "inline-block",
      width: size,
      height: size,
      backgroundColor: color,
      WebkitMaskImage: `url("${url}")`,
      maskImage: `url("${url}")`,
      WebkitMaskRepeat: "no-repeat",
      maskRepeat: "no-repeat",
      WebkitMaskPosition: "center",
      maskPosition: "center",
      WebkitMaskSize: "contain",
      maskSize: "contain",
      flexShrink: 0,
      ...style
    }
  }, rest));
}
Object.assign(__ds_scope, { Icon });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Icon.jsx", error: String((e && e.message) || e) }); }

// components/core/IconButton.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * IconButton — circular icon-only tap target, M3 state-layer hover/press.
 */
function IconButton({
  icon,
  label,
  size = 40,
  active = false,
  onClick,
  style,
  ...rest
}) {
  const [hover, setHover] = React.useState(false);
  const [press, setPress] = React.useState(false);
  return /*#__PURE__*/React.createElement("button", _extends({
    "aria-label": label,
    onClick: onClick,
    onMouseEnter: () => setHover(true),
    onMouseLeave: () => {
      setHover(false);
      setPress(false);
    },
    onMouseDown: () => setPress(true),
    onMouseUp: () => setPress(false),
    style: {
      width: size,
      height: size,
      borderRadius: "50%",
      border: "none",
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      background: press ? "color-mix(in srgb, var(--md-sys-color-on-surface) 12%, transparent)" : hover ? "color-mix(in srgb, var(--md-sys-color-on-surface) 8%, transparent)" : active ? "var(--md-sys-color-secondary-container)" : "transparent",
      color: active ? "var(--md-sys-color-on-secondary-container)" : "var(--md-sys-color-on-surface-variant)",
      cursor: "pointer",
      transition: "background-color var(--motion-duration-short) var(--motion-easing-standard)",
      ...style
    }
  }, rest), typeof icon === "string" ? /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: icon,
    size: size * 0.5
  }) : icon);
}
Object.assign(__ds_scope, { IconButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/IconButton.jsx", error: String((e && e.message) || e) }); }

// components/core/Overline.jsx
try { (() => {
/**
 * Overline — small-caps micro-label used pervasively across the app: section
 * headers in lists ("Июль", "Упражнения"), stepper field captions ("Повт",
 * "Кг", "Вес"), and progress context ("Упражнение 2 из 5"). Sits above the
 * thing it labels; never a standalone paragraph. Space Grotesk, 11px, 600,
 * wide tracking, uppercase, on-surface-variant.
 */
function Overline({
  children,
  style
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: 11,
      fontWeight: 600,
      letterSpacing: "1.4px",
      textTransform: "uppercase",
      color: "var(--md-sys-color-on-surface-variant)",
      ...style
    }
  }, children);
}
Object.assign(__ds_scope, { Overline });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Overline.jsx", error: String((e && e.message) || e) }); }

// components/core/ProgressTrack.jsx
try { (() => {
const TONE = {
  positive: {
    h: 14,
    bg: "var(--diff-positive)"
  },
  met: {
    h: 9,
    bg: "var(--md-sys-color-primary)"
  },
  negative: {
    h: 5,
    bg: "var(--diff-negative)"
  },
  skipped: {
    h: 9,
    bg: "transparent",
    ring: "var(--diff-skipped)"
  },
  todo: {
    h: 4,
    bg: "color-mix(in srgb, var(--md-sys-color-outline) 28%, transparent)"
  }
};

/**
 * ProgressTrack — set progress as segments instead of numbers. Replaces v1's
 * wrapping «10×60 кг» pills: at a glance you read how it went, and the exact
 * numbers stay one tap away.
 *
 * Three modes:
 *  - `tones` (a per-set array of "positive"|"met"|"negative"|"skipped"|"todo")
 *    draws a **mini bar chart against the target**: height is the signal, not
 *    just hue — exceeded spikes to full height, met sits at the mid line,
 *    below-target dips short, skipped is a hollow outline. Colour alone at
 *    5px could not separate five states, and two greens (met vs. exceeded)
 *    are indistinguishable; height reads instantly with no legend.
 *  - "plan" — nothing has happened yet: every segment renders as an opaque
 *    62% mix of the group colour over `surface-container-high`, so the track
 *    reads "4 planned" and stays visible even for the lightest group hues.
 *  - "progress" (default) — `done` segments filled solid, the rest empty;
 *    for an exercise mid-session where only the count matters.
 */
function ProgressTrack({
  total,
  done = 0,
  missed = [],
  tones,
  color = "var(--md-sys-color-primary)",
  height = 5,
  mode = "progress",
  style
}) {
  if (tones && tones.length) {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        gap: 4,
        alignItems: "flex-end",
        height: 14,
        ...style
      }
    }, tones.map((t, i) => {
      const s = TONE[t] || TONE.todo;
      return /*#__PURE__*/React.createElement("span", {
        key: i,
        style: {
          flex: 1,
          height: s.h,
          borderRadius: "var(--shape-full)",
          background: s.bg,
          boxShadow: s.ring ? `inset 0 0 0 1.5px ${s.ring}` : undefined,
          transition: "height var(--motion-duration-medium) var(--motion-easing-standard)"
        }
      });
    }));
  }
  const plan = mode === "plan";
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      gap: 4,
      alignItems: "center",
      ...style
    }
  }, Array.from({
    length: total
  }).map((_, i) => {
    const isMissed = missed.includes(i);
    return /*#__PURE__*/React.createElement("span", {
      key: i,
      style: {
        flex: 1,
        height,
        borderRadius: "var(--shape-full)",
        background: isMissed ? "transparent" : plan ? `color-mix(in srgb, ${color} 62%, var(--md-sys-color-surface-container-high))` : i < done ? color : "color-mix(in srgb, var(--md-sys-color-outline) 28%, transparent)",
        boxShadow: isMissed ? "inset 0 0 0 1.5px var(--diff-negative)" : undefined,
        transition: "background-color var(--motion-duration-medium) var(--motion-easing-standard)"
      }
    });
  }));
}
Object.assign(__ds_scope, { ProgressTrack });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/ProgressTrack.jsx", error: String((e && e.message) || e) }); }

// components/core/Switch.jsx
try { (() => {
/**
 * Switch — M3 toggle switch.
 */
function Switch({
  checked,
  onChange,
  label
}) {
  return /*#__PURE__*/React.createElement("label", {
    style: {
      display: "inline-flex",
      alignItems: "center",
      gap: 12,
      cursor: "pointer"
    }
  }, /*#__PURE__*/React.createElement("span", {
    onClick: () => onChange && onChange(!checked),
    style: {
      width: 52,
      height: 32,
      borderRadius: "var(--shape-full)",
      background: checked ? "var(--md-sys-color-primary)" : "var(--md-sys-color-surface-container-highest)",
      border: checked ? "none" : "2px solid var(--md-sys-color-outline)",
      position: "relative",
      transition: "background-color var(--motion-duration-medium) var(--motion-easing-standard)",
      flexShrink: 0,
      boxSizing: "border-box"
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      top: "50%",
      left: checked ? 22 : 4,
      transform: "translateY(-50%)",
      width: checked ? 24 : 16,
      height: checked ? 24 : 16,
      borderRadius: "50%",
      background: checked ? "var(--md-sys-color-on-primary)" : "var(--md-sys-color-outline)",
      transition: "left var(--motion-duration-medium) var(--motion-easing-standard), width var(--motion-duration-short), height var(--motion-duration-short)"
    }
  })), label && /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-body-large-size)",
      color: "var(--md-sys-color-on-surface)"
    }
  }, label));
}
Object.assign(__ds_scope, { Switch });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Switch.jsx", error: String((e && e.message) || e) }); }

// components/core/TextField.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * TextField — M3 outlined text input. Used for program/exercise names,
 * descriptions, and free-text set notes — the only places the spec calls
 * for keyboard input.
 */
function TextField({
  label,
  value,
  onChange,
  placeholder,
  multiline = false,
  style,
  ...rest
}) {
  const [focused, setFocused] = React.useState(false);
  const Tag = multiline ? "textarea" : "input";
  return /*#__PURE__*/React.createElement("label", {
    style: {
      display: "block",
      ...style
    }
  }, label && /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-label-medium-size)",
      fontWeight: 600,
      color: "var(--md-sys-color-on-surface-variant)",
      marginBottom: 6
    }
  }, label), /*#__PURE__*/React.createElement(Tag, _extends({
    value: value,
    placeholder: placeholder,
    onChange: e => onChange && onChange(e.target.value),
    onFocus: () => setFocused(true),
    onBlur: () => setFocused(false),
    rows: multiline ? 3 : undefined,
    style: {
      width: "100%",
      boxSizing: "border-box",
      padding: "14px 18px",
      borderRadius: "var(--shape-large)",
      border: `1.5px solid ${focused ? "var(--md-sys-color-primary)" : "transparent"}`,
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-body-large-size)",
      color: "var(--md-sys-color-on-surface)",
      background: "var(--md-sys-color-surface-container)",
      outline: "none",
      resize: multiline ? "vertical" : undefined,
      transition: "border-color var(--motion-duration-short) var(--motion-easing-standard)"
    }
  }, rest)));
}
Object.assign(__ds_scope, { TextField });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/TextField.jsx", error: String((e && e.message) || e) }); }

// components/core/exerciseIcons.js
try { (() => {
/**
 * Lyte exercise pictograms — the set the product ships with.
 *
 * Source: Flaticon, "Special Flat" линейный стиль, автор Icongeek26.
 * Free licence — **attribution is required wherever these ship**; see
 * ATTRIBUTION.md at the project root and keep that credit in any build that
 * uses them.
 *
 * Ten movements. Deliberately not padded out: an approximated glyph is worse
 * than an honest gap, so overhead press, dips and lunges have no pictogram yet
 * and reuse the nearest correct one until real files exist.
 *
 * The files are 512×512 PNG, black line art on transparent. They are rendered
 * through a CSS mask rather than <img> so a glyph tints to any accent colour —
 * an <img> would bake in black and vanish on a dark surface.
 */
const EXERCISE_ICONS = {
  "squat": {
    label: "Присед",
    file: "squat.png"
  },
  "deadlift": {
    label: "Становая",
    file: "deadlift.png"
  },
  "bench-press": {
    label: "Жим лёжа",
    file: "bench-press.png"
  },
  "pull-up": {
    label: "Подтягивания",
    file: "pull-up.png"
  },
  "dumbbell-press": {
    label: "Жим гантелей",
    file: "dumbbell-press.png"
  },
  "curl": {
    label: "Сгибания",
    file: "curl.png"
  },
  "crunch": {
    label: "Пресс",
    file: "crunch.png"
  },
  "rack": {
    label: "Рама",
    file: "rack.png"
  },
  "machine": {
    label: "Тренажёр",
    file: "machine.png"
  },
  "stretch": {
    label: "Растяжка",
    file: "stretch.png"
  }
};

/** Order the pickers and specimens use: barbell work, bodyweight, dumbbell, core, stations. */
const EXERCISE_ICON_ORDER = ["squat", "deadlift", "bench-press", "pull-up", "dumbbell-press", "curl", "crunch", "stretch", "rack", "machine"];
Object.assign(__ds_scope, { EXERCISE_ICONS, EXERCISE_ICON_ORDER });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/exerciseIcons.js", error: String((e && e.message) || e) }); }

// components/core/ExerciseIcon.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * Where the icon files live, resolved from the design-system bundle's own
 * <script src> so a consuming page never has to configure a path: whatever
 * relative prefix reaches _ds_bundle.js also reaches assets/.
 */
const ASSET_ROOT = (() => {
  if (typeof document === "undefined") return "assets/icons/exercises/";
  const tag = document.querySelector('script[src*="_ds_bundle.js"]');
  const base = tag ? (tag.getAttribute("src") || "").replace(/_ds_bundle\.js.*$/, "") : "";
  return base + "assets/icons/exercises/";
})();

/**
 * ExerciseIcon — one exercise pictogram from the shipped set.
 *
 * Drawn through a CSS mask, not an <img>: the source files are black line art,
 * and the mask turns each one into a silhouette tinted by `color`, so the same
 * glyph works on a light card and inside a saturated accent circle. An <img>
 * would bake in black and disappear.
 *
 * Unknown names render nothing rather than a broken box — a mistyped key
 * should be invisible, not load-bearing.
 */
function ExerciseIcon({
  name,
  size = 24,
  color = "currentColor",
  style,
  ...rest
}) {
  const icon = __ds_scope.EXERCISE_ICONS[name];
  if (!icon) return null;
  const url = ASSET_ROOT + icon.file;
  return /*#__PURE__*/React.createElement("span", _extends({
    role: "img",
    "aria-label": icon.label,
    style: {
      display: "inline-block",
      width: size,
      height: size,
      backgroundColor: color,
      WebkitMaskImage: `url("${url}")`,
      maskImage: `url("${url}")`,
      WebkitMaskRepeat: "no-repeat",
      maskRepeat: "no-repeat",
      WebkitMaskPosition: "center",
      maskPosition: "center",
      WebkitMaskSize: "contain",
      maskSize: "contain",
      flexShrink: 0,
      ...style
    }
  }, rest));
}
Object.assign(__ds_scope, { ExerciseIcon });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/ExerciseIcon.jsx", error: String((e && e.message) || e) }); }

// components/core/ExerciseMark.jsx
try { (() => {
/**
 * The six colours an exercise can carry. NOT muscle groups — the app has no
 * muscle-group data and none is planned. Seed exercises ship pre-coloured;
 * anything the user creates, the user colours.
 */
const ACCENTS = {
  coral: {
    label: "Коралловый",
    fg: "var(--accent-coral)",
    bg: "var(--accent-coral-container)"
  },
  indigo: {
    label: "Индиго",
    fg: "var(--accent-indigo)",
    bg: "var(--accent-indigo-container)"
  },
  lime: {
    label: "Лайм",
    fg: "var(--accent-lime)",
    bg: "var(--accent-lime-container)"
  },
  amber: {
    label: "Янтарный",
    fg: "var(--accent-amber)",
    bg: "var(--accent-amber-container)"
  },
  teal: {
    label: "Бирюзовый",
    fg: "var(--accent-teal)",
    bg: "var(--accent-teal-container)"
  },
  slate: {
    label: "Серый",
    fg: "var(--accent-slate)",
    bg: "var(--accent-slate-container)"
  }
};
const ACCENT_NAMES = ["coral", "indigo", "lime", "amber", "teal", "slate"];

/**
 * ExerciseMark — the visual anchor of every card and row: a tinted circle
 * carrying two signals at once.
 *
 *   colour = the exercise's own accent, picked by the user
 *   glyph  = the movement (squat · deadlift · pull-up · curl · …)
 *
 * Neither is derived from anything: an exercise stores a colour and a
 * pictogram as plain properties. That is the whole model — no taxonomy to
 * maintain, nothing to infer, and a list the user coloured themselves is the
 * one they can scan fastest.
 *
 * `slate` is the default, so an exercise created without a choice still looks
 * deliberate. Legible at every size: 36px picker rows get the same treatment
 * as 52px cards.
 */
function ExerciseMark({
  color = "slate",
  exercise = "squat",
  size = 44,
  image,
  style
}) {
  const a = ACCENTS[color] || ACCENTS.slate;
  return /*#__PURE__*/React.createElement("span", {
    "aria-hidden": "true",
    style: {
      width: size,
      height: size,
      borderRadius: "50%",
      background: a.bg,
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      flexShrink: 0,
      overflow: "hidden",
      ...style
    }
  }, image && size >= 48 ? /*#__PURE__*/React.createElement("img", {
    src: image,
    alt: "",
    style: {
      width: "100%",
      height: "100%",
      objectFit: "cover"
    }
  }) : /*#__PURE__*/React.createElement(__ds_scope.ExerciseIcon, {
    name: exercise,
    size: Math.round(size * 0.58),
    color: a.fg
  }));
}
Object.assign(__ds_scope, { ACCENTS, ACCENT_NAMES, ExerciseMark });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/ExerciseMark.jsx", error: String((e && e.message) || e) }); }

// components/cards/ListRow.jsx
try { (() => {
/**
 * ListRow (v2) — generic tap-through row: exercise-library picker, settings,
 * any plain list item. Quieter than v1 (15.5px/500 title, 12px subtitle) and
 * takes a `color` so library rows carry the same mark as the cards.
 */
function ListRow({
  title,
  subtitle,
  color,
  exercise,
  leadingIcon,
  trailing,
  onClick,
  showChevron = true
}) {
  return /*#__PURE__*/React.createElement("div", {
    onClick: onClick,
    style: {
      display: "flex",
      alignItems: "center",
      gap: 12,
      padding: "10px 14px",
      borderBottom: "1px solid var(--md-sys-color-outline-variant)",
      cursor: onClick ? "pointer" : "default"
    }
  }, color ? /*#__PURE__*/React.createElement(__ds_scope.ExerciseMark, {
    color: color,
    exercise: exercise,
    size: 36
  }) : leadingIcon ? /*#__PURE__*/React.createElement("span", {
    style: {
      color: "var(--md-sys-color-on-surface-variant)",
      display: "flex",
      flexShrink: 0
    }
  }, /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: leadingIcon,
    size: 20
  })) : null, /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "15.5px",
      fontWeight: 500,
      letterSpacing: "-0.1px",
      color: "var(--md-sys-color-on-surface)",
      display: "-webkit-box",
      WebkitLineClamp: 2,
      WebkitBoxOrient: "vertical",
      overflow: "hidden"
    }
  }, title), subtitle && /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-body-small-size)",
      color: "var(--md-sys-color-on-surface-variant)",
      marginTop: 1
    }
  }, subtitle)), trailing, showChevron && !trailing && /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: "chevron-right",
    size: 17,
    color: "var(--md-sys-color-outline)"
  }));
}
Object.assign(__ds_scope, { ListRow });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/cards/ListRow.jsx", error: String((e && e.message) || e) }); }

// components/cards/SessionCard.jsx
try { (() => {
/**
 * SessionCard (v2) — a finished session in history. v1 packed name, date,
 * duration and "15/16 подходов" into two text lines of equal weight. v2 keeps
 * one hero number (duration), turns the sets ratio into a ProgressTrack, and
 * leads with the exercise mark.
 */
function SessionCard({
  programName,
  date,
  duration,
  setsSummary,
  done,
  total,
  missed = [],
  tones,
  color = "slate",
  exercise,
  image,
  onClick
}) {
  return /*#__PURE__*/React.createElement("div", {
    onClick: onClick,
    style: {
      background: "var(--md-sys-color-surface-container-lowest)",
      borderRadius: "var(--shape-extra-large)",
      padding: "14px 16px",
      boxShadow: "var(--elevation-1)",
      cursor: onClick ? "pointer" : "default"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 14
    }
  }, /*#__PURE__*/React.createElement(__ds_scope.ExerciseMark, {
    color: color,
    exercise: exercise,
    image: image,
    size: 52
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-title-medium-size)",
      fontWeight: 600,
      letterSpacing: "-0.2px",
      color: "var(--md-sys-color-on-surface)",
      whiteSpace: "nowrap",
      overflow: "hidden",
      textOverflow: "ellipsis"
    }
  }, programName), /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-body-small-size)",
      color: "var(--md-sys-color-on-surface-variant)",
      marginTop: 1
    }
  }, date)), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-numeric)",
      fontVariantNumeric: "tabular-nums",
      fontSize: "19px",
      fontWeight: 700,
      letterSpacing: "-0.4px",
      color: "var(--md-sys-color-on-surface)",
      flexShrink: 0
    }
  }, duration)), tones && tones.length ? /*#__PURE__*/React.createElement(__ds_scope.ProgressTrack, {
    tones: tones,
    style: {
      marginTop: 12
    }
  }) : total ? /*#__PURE__*/React.createElement(__ds_scope.ProgressTrack, {
    total: total,
    done: done ?? total,
    missed: missed,
    style: {
      marginTop: 12
    }
  }) : setsSummary ? /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-body-small-size)",
      color: "var(--md-sys-color-on-surface-variant)",
      marginTop: 8
    }
  }, setsSummary) : null);
}
Object.assign(__ds_scope, { SessionCard });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/cards/SessionCard.jsx", error: String((e && e.message) || e) }); }

// components/core/plural.js
try { (() => {
/**
 * plural — Russian plural forms. `plural(3, ["упражнение", "упражнения", "упражнений"])`
 * → "упражнения". Every count in the UI goes through this: a wrong plural on a
 * list screen is exactly the kind of detail that makes an interface feel unfinished.
 */
function plural(n, forms) {
  const a = Math.abs(n) % 100;
  const b = a % 10;
  if (a > 10 && a < 20) return forms[2];
  if (b === 1) return forms[0];
  if (b >= 2 && b <= 4) return forms[1];
  return forms[2];
}
const EXERCISES = ["упражнение", "упражнения", "упражнений"];
const SETS = ["подход", "подхода", "подходов"];
Object.assign(__ds_scope, { plural, EXERCISES, SETS });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/plural.js", error: String((e && e.message) || e) }); }

// components/cards/ExerciseCard.jsx
try { (() => {
/**
 * ExerciseCard (v2) — an exercise inside a program editor. v1 rendered every
 * planned set as a numeric pill («10×60 кг» ×4), which turned one card into
 * five competing text blocks. v2 shows the same plan as a ProgressTrack plus
 * a single count line; exact numbers live one tap deeper, in set editing.
 *
 * The track here is a PLAN, so every segment is the same colour — it answers
 * "how many sets", not "how many done". Progress belongs to the session
 * screen, not the program editor; a half-filled track in a list of exercises
 * reads as an error.
 *
 * Pass `plan` (array of [reps, weight]) — its length drives the track — or a
 * plain `summary` string for the compact form.
 */
function ExerciseCard({
  name,
  summary,
  plan,
  color = "slate",
  exercise,
  image,
  onClick,
  onEdit,
  onRemove,
  draggable = true
}) {
  const a = __ds_scope.ACCENTS[color] || __ds_scope.ACCENTS.slate;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      background: "var(--md-sys-color-surface-container-lowest)",
      borderRadius: "var(--shape-large-increased)",
      padding: "12px 12px 12px 8px",
      boxShadow: "var(--elevation-1)",
      display: "flex",
      alignItems: "center",
      gap: 10
    }
  }, draggable && /*#__PURE__*/React.createElement("span", {
    style: {
      color: "var(--md-sys-color-outline-variant)",
      display: "flex",
      cursor: "grab",
      flexShrink: 0
    }
  }, /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: "grip-vertical",
    size: 18
  })), /*#__PURE__*/React.createElement(__ds_scope.ExerciseMark, {
    color: color,
    exercise: exercise,
    size: 38
  }), /*#__PURE__*/React.createElement("div", {
    onClick: onClick,
    style: {
      flex: 1,
      minWidth: 0,
      cursor: onClick ? "pointer" : "default"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "15.5px",
      lineHeight: "20px",
      fontWeight: 600,
      letterSpacing: "-0.1px",
      color: "var(--md-sys-color-on-surface)",
      display: "-webkit-box",
      WebkitLineClamp: 2,
      WebkitBoxOrient: "vertical",
      overflow: "hidden"
    }
  }, name), plan && plan.length > 0 ? /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 8,
      marginTop: 6,
      flexWrap: "nowrap"
    }
  }, /*#__PURE__*/React.createElement(__ds_scope.ProgressTrack, {
    total: plan.length,
    mode: "plan",
    color: a.fg,
    style: {
      width: 56,
      flexShrink: 0
    }
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-body-small-size)",
      color: "var(--md-sys-color-on-surface-variant)",
      whiteSpace: "nowrap"
    }
  }, plan.length, " ", __ds_scope.plural(plan.length, __ds_scope.SETS))) : summary ? /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-body-small-size)",
      color: "var(--md-sys-color-on-surface-variant)",
      marginTop: 1
    }
  }, summary) : null), onEdit && /*#__PURE__*/React.createElement(__ds_scope.IconButton, {
    icon: "pencil-line",
    label: "\u0420\u0435\u0434\u0430\u043A\u0442\u0438\u0440\u043E\u0432\u0430\u0442\u044C \u043F\u043E\u0434\u0445\u043E\u0434\u044B",
    size: 36,
    onClick: onEdit
  }), onRemove && /*#__PURE__*/React.createElement(__ds_scope.IconButton, {
    icon: "trash-2",
    label: "\u0423\u0431\u0440\u0430\u0442\u044C \u0438\u0437 \u043F\u0440\u043E\u0433\u0440\u0430\u043C\u043C\u044B",
    size: 36,
    onClick: onRemove
  }));
}
Object.assign(__ds_scope, { ExerciseCard });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/cards/ExerciseCard.jsx", error: String((e && e.message) || e) }); }

// components/cards/ProgramCard.jsx
try { (() => {
/**
 * ProgramCard (v2) — one program in the list. The v1 card led with a bold
 * 22px title and a "5 упражнений · посл. сессия 2 июл" metadata line; three
 * of them in a row gave the eye nothing to land on. v2 leads with a colour
 * mark, drops the title to 16px, and keeps exactly one metadata fact —
 * the last-session date moved into the program detail.
 */
function ProgramCard({
  name,
  exerciseCount,
  lastSession,
  color = "slate",
  exercise,
  image,
  onClick,
  onMenuClick,
  trailing
}) {
  return /*#__PURE__*/React.createElement("div", {
    onClick: onClick,
    style: {
      background: "var(--md-sys-color-surface-container-lowest)",
      borderRadius: "var(--shape-extra-large)",
      padding: "14px 16px",
      boxShadow: "var(--elevation-1)",
      display: "flex",
      alignItems: "center",
      gap: 14,
      cursor: onClick ? "pointer" : "default"
    }
  }, /*#__PURE__*/React.createElement(__ds_scope.ExerciseMark, {
    color: color,
    exercise: exercise,
    image: image,
    size: 52
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-title-medium-size)",
      fontWeight: 600,
      letterSpacing: "-0.2px",
      color: "var(--md-sys-color-on-surface)",
      whiteSpace: "nowrap",
      overflow: "hidden",
      textOverflow: "ellipsis"
    }
  }, name), /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-body-small-size)",
      color: "var(--md-sys-color-on-surface-variant)",
      marginTop: 1
    }
  }, exerciseCount, " ", __ds_scope.plural(exerciseCount, __ds_scope.EXERCISES))), trailing ? /*#__PURE__*/React.createElement("div", {
    style: {
      flexShrink: 0
    },
    onClick: e => e.stopPropagation()
  }, trailing) : onMenuClick ? /*#__PURE__*/React.createElement("button", {
    onClick: e => {
      e.stopPropagation();
      onMenuClick();
    },
    "aria-label": "\u041C\u0435\u043D\u044E",
    style: {
      background: "none",
      border: "none",
      padding: 8,
      cursor: "pointer",
      color: "var(--md-sys-color-outline)"
    }
  }, /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: "more-vertical",
    size: 18
  })) : onClick ? /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: "chevron-right",
    size: 18,
    color: "var(--md-sys-color-outline)"
  }) : null);
}
Object.assign(__ds_scope, { ProgramCard });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/cards/ProgramCard.jsx", error: String((e && e.message) || e) }); }

// components/data-display/SessionStopwatch.jsx
try { (() => {
/**
 * SessionStopwatch — hero numeric readout for total elapsed session time
 * (spec 4.3, element 1). Always tabular-numeric, never the display face.
 */
function SessionStopwatch({
  seconds,
  size = "hero"
}) {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor(seconds % 3600 / 60);
  const s = seconds % 60;
  const parts = h > 0 ? [h, m, s] : [m, s];
  const text = parts.map(p => String(p).padStart(2, "0")).join(":");
  const font = size === "hero" ? "var(--type-numeric-hero-size)" : "var(--type-numeric-large-size)";
  const line = size === "hero" ? "var(--type-numeric-hero-line)" : "var(--type-numeric-large-line)";
  return /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-numeric)",
      fontVariantNumeric: "tabular-nums",
      fontSize: font,
      lineHeight: line,
      fontWeight: 500,
      color: "var(--md-sys-color-on-surface)",
      textAlign: "center"
    }
  }, text);
}
Object.assign(__ds_scope, { SessionStopwatch });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/data-display/SessionStopwatch.jsx", error: String((e && e.message) || e) }); }

// components/feedback/Dialog.jsx
try { (() => {
/**
 * Dialog — confirmation modal for destructive actions (delete program, end
 * session early). Always requires an explicit confirm; never auto-dismiss.
 * Action buttons are compact (small) so the footer stays proportionate to
 * the dialog's padding — no caller-side height override needed.
 */
function Dialog({
  open,
  title,
  description,
  confirmLabel = "Удалить",
  cancelLabel = "Отмена",
  destructive = true,
  onConfirm,
  onCancel
}) {
  if (!open) return null;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      position: "fixed",
      inset: 0,
      background: "color-mix(in srgb, var(--md-sys-color-scrim) 32%, transparent)",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      zIndex: 1000
    },
    onClick: onCancel
  }, /*#__PURE__*/React.createElement("div", {
    onClick: e => e.stopPropagation(),
    style: {
      background: "var(--md-sys-color-surface-container-lowest)",
      borderRadius: "var(--shape-extra-large)",
      padding: 24,
      width: 320,
      boxSizing: "border-box",
      boxShadow: "var(--elevation-4)"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-title-large-size)",
      lineHeight: "28px",
      fontWeight: 700,
      letterSpacing: "-0.3px",
      color: "var(--md-sys-color-on-surface)",
      marginBottom: 8,
      textWrap: "balance",
      overflowWrap: "break-word"
    }
  }, title), description && /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-body-medium-size)",
      color: "var(--md-sys-color-on-surface-variant)"
    }
  }, description), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      justifyContent: "flex-end",
      gap: 4,
      marginTop: 20
    }
  }, /*#__PURE__*/React.createElement(__ds_scope.Button, {
    variant: "text",
    size: "small",
    onClick: onCancel
  }, cancelLabel), /*#__PURE__*/React.createElement(__ds_scope.Button, {
    variant: "text",
    size: "small",
    accent: destructive ? "error" : "primary",
    onClick: onConfirm
  }, confirmLabel))));
}
Object.assign(__ds_scope, { Dialog });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/Dialog.jsx", error: String((e && e.message) || e) }); }

// components/feedback/DiffRow.jsx
try { (() => {
const tones = {
  positive: {
    bg: "var(--diff-positive-bg)",
    fg: "var(--diff-positive)"
  },
  met: {
    bg: "var(--diff-met-bg)",
    fg: "var(--diff-met)"
  },
  negative: {
    bg: "var(--diff-negative-bg)",
    fg: "var(--diff-negative)"
  },
  neutral: {
    bg: "var(--diff-neutral-bg)",
    fg: "var(--diff-neutral)"
  },
  skipped: {
    bg: "var(--diff-skipped-bg)",
    fg: "var(--diff-skipped)"
  }
};
const parse = s => {
  const m = /^([\d.,]+)\s*[×x]\s*([\d.,]+)$/.exec(s || "");
  return m ? [parseFloat(m[1].replace(",", ".")), parseFloat(m[2].replace(",", "."))] : null;
};
const num = n => String(n).replace(".", ",");

/**
 * DiffRow (v2) — one set's result in session detail. v1 spelled out both
 * sides («10 повт · 60 кг → 12 повт · 62,5 кг»), six elements per row and
 * fifteen rows per screen. v2 shows the fact once, large, and expresses the
 * comparison as a signed delta chip: the target is implied by the tone.
 * Rows that hit the target exactly show no chip at all — nothing to report.
 *
 * Notes are free text a user typed mid-set, so length is unbounded — and they
 * ALWAYS sit on their own line under the numbers, aligned past the set index,
 * however short they are. One shape for every row: a note never competes with
 * the delta chip for the same horizontal space, rows in a list stay visually
 * parallel, and nothing is ever truncated. A note the user bothered to write
 * is worth reading in full.
 */
function DiffRow({
  index,
  target,
  actual,
  tone = "neutral",
  note
}) {
  const t = tones[tone] || tones.neutral;
  const a = parse(actual);
  const p = parse(target);
  const dReps = a && p ? a[0] - p[0] : 0;
  const dKg = a && p ? a[1] - p[1] : 0;
  const delta = [dReps ? `${dReps > 0 ? "+" : "−"}${num(Math.abs(dReps))} повт` : null, dKg ? `${dKg > 0 ? "+" : "−"}${num(Math.abs(dKg))} кг` : null].filter(Boolean).join(" · ");
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 7,
      padding: "11px 14px",
      borderRadius: "var(--shape-large)",
      background: t.bg
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 12
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-numeric)",
      fontVariantNumeric: "tabular-nums",
      fontSize: "12px",
      fontWeight: 600,
      color: t.fg,
      opacity: 0.6,
      width: 14,
      flexShrink: 0
    }
  }, index), tone === "skipped" ? /*#__PURE__*/React.createElement("span", {
    style: {
      flex: 1,
      fontFamily: "var(--font-brand)",
      fontSize: "14px",
      fontWeight: 500,
      color: t.fg
    }
  }, "\u043F\u0440\u043E\u043F\u0443\u0449\u0435\u043D\u043E") : /*#__PURE__*/React.createElement("span", {
    style: {
      flex: 1,
      display: "flex",
      alignItems: "baseline",
      gap: 6
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-numeric)",
      fontVariantNumeric: "tabular-nums",
      fontSize: "17px",
      fontWeight: 700,
      letterSpacing: "-0.3px",
      color: t.fg
    }
  }, a ? `${num(a[0])}×${num(a[1])}` : actual), a && /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "11px",
      fontWeight: 500,
      color: t.fg,
      opacity: 0.7
    }
  }, "\u043A\u0433")), delta && tone !== "skipped" && /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-brand)",
      fontVariantNumeric: "tabular-nums",
      fontSize: "12.5px",
      fontWeight: 600,
      color: t.fg,
      background: "color-mix(in srgb, currentColor 12%, transparent)",
      borderRadius: "var(--shape-full)",
      padding: "3px 9px",
      flexShrink: 0
    }
  }, delta)), note && /*#__PURE__*/React.createElement("div", {
    style: {
      paddingLeft: 26,
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-body-small-size)",
      lineHeight: "var(--type-body-small-line)",
      color: t.fg,
      opacity: 0.8,
      textWrap: "pretty"
    }
  }, note));
}
Object.assign(__ds_scope, { DiffRow });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/DiffRow.jsx", error: String((e && e.message) || e) }); }

// components/feedback/EmptyState.jsx
try { (() => {
/**
 * EmptyState — full-screen-worthy empty message: large icon mark, headline,
 * supporting line, one proportionate action. Used for empty program list
 * (3.1) and empty history (5.1).
 */
function EmptyState({
  icon = "dumbbell",
  message,
  hint,
  actionLabel,
  onAction
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      justifyContent: "center",
      gap: 0,
      padding: "64px 32px",
      textAlign: "center",
      minHeight: 320,
      boxSizing: "border-box"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 112,
      height: 112,
      borderRadius: "50%",
      background: "var(--md-sys-color-primary-container)",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      marginBottom: 28
    }
  }, /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: icon,
    size: 48,
    color: "var(--md-sys-color-on-primary-container)"
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-headline-small-size)",
      lineHeight: "var(--type-headline-small-line)",
      fontWeight: 700,
      letterSpacing: "-0.3px",
      color: "var(--md-sys-color-on-surface)",
      textWrap: "balance",
      maxWidth: 280
    }
  }, message), hint && /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-body-medium-size)",
      lineHeight: "var(--type-body-medium-line)",
      color: "var(--md-sys-color-on-surface-variant)",
      marginTop: 8,
      maxWidth: 260
    }
  }, hint), actionLabel && /*#__PURE__*/React.createElement("div", {
    style: {
      marginTop: 28
    }
  }, /*#__PURE__*/React.createElement(__ds_scope.Button, {
    size: "small",
    onClick: onAction
  }, actionLabel)));
}
Object.assign(__ds_scope, { EmptyState });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/EmptyState.jsx", error: String((e && e.message) || e) }); }

// components/forms/AccentPicker.jsx
try { (() => {
/**
 * AccentPicker — choose an exercise's colour from the six accents.
 *
 * Six swatches on one row, no scroll and no overflow: the whole palette is
 * visible at once, which is the point of keeping it at six. Each swatch is a
 * 44px circle filled with the accent's *container* tone — the same fill the
 * mark will use — so the choice previews the result rather than showing a
 * saturated dot the user will never see again.
 *
 * Selection is a ring drawn outside the swatch with a surface-coloured gap,
 * never a checkmark: a tick inside a colour swatch obscures the colour being
 * chosen. Hit area is 44px even though the visible circle can be smaller.
 */
function AccentPicker({
  value = "slate",
  onChange,
  label = "Цвет"
}) {
  return /*#__PURE__*/React.createElement("div", null, label && /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-label-medium-size)",
      fontWeight: 600,
      color: "var(--md-sys-color-on-surface-variant)",
      marginBottom: 8
    }
  }, label), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      gap: 10,
      justifyContent: "space-between"
    }
  }, __ds_scope.ACCENT_NAMES.map(name => {
    const a = __ds_scope.ACCENTS[name];
    const selected = name === value;
    return /*#__PURE__*/React.createElement("button", {
      key: name,
      type: "button",
      "aria-label": a.label,
      "aria-pressed": selected,
      onClick: () => onChange && onChange(name),
      style: {
        width: 44,
        height: 44,
        flex: "0 0 auto",
        borderRadius: "50%",
        border: "none",
        padding: 0,
        cursor: "pointer",
        background: a.bg,
        boxShadow: selected ? `0 0 0 2.5px var(--md-sys-color-surface), 0 0 0 5px ${a.fg}` : "none",
        transition: "box-shadow var(--motion-duration-short) var(--motion-easing-standard)"
      }
    });
  })));
}
Object.assign(__ds_scope, { AccentPicker });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/AccentPicker.jsx", error: String((e && e.message) || e) }); }

// components/forms/ExerciseIconPicker.jsx
try { (() => {
/**
 * ExerciseIconPicker — choose the pictogram for an exercise.
 *
 * Ten circular tiles on two rows — each tile IS the mark it produces, at
 * the size it will appear in a list, so the grid needs no labels and no
 * preview to explain itself. The movement name still ships as the tile's
 * `aria-label`, so it is available to screen readers and on long-press.
 *
 * The grid renders in the currently chosen accent, so the two pickers read as
 * one decision — pick a colour, the whole grid recolours, pick a shape, the
 * mark is done. The selected tile fills with the accent's container tone,
 * which is exactly what the finished mark looks like; unselected tiles sit on
 * `surface-container`.
 */
function ExerciseIconPicker({
  value = "squat",
  onChange,
  color = "slate",
  label = "Знак"
}) {
  const a = __ds_scope.ACCENTS[color] || __ds_scope.ACCENTS.slate;
  return /*#__PURE__*/React.createElement("div", null, label && /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-label-medium-size)",
      fontWeight: 600,
      color: "var(--md-sys-color-on-surface-variant)",
      marginBottom: 8
    }
  }, label), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "grid",
      gridTemplateColumns: "repeat(5, 1fr)",
      gap: 8,
      justifyItems: "center"
    }
  }, __ds_scope.EXERCISE_ICON_ORDER.map(name => {
    const selected = name === value;
    return /*#__PURE__*/React.createElement("button", {
      key: name,
      type: "button",
      "aria-label": __ds_scope.EXERCISE_ICONS[name].label,
      "aria-pressed": selected,
      onClick: () => onChange && onChange(name),
      style: {
        width: 46,
        height: 46,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: 0,
        borderRadius: "50%",
        border: "none",
        cursor: "pointer",
        background: selected ? a.bg : "var(--md-sys-color-surface-container)",
        transition: "background-color var(--motion-duration-short) var(--motion-easing-standard)"
      }
    }, /*#__PURE__*/React.createElement(__ds_scope.ExerciseIcon, {
      name: name,
      size: 26,
      color: selected ? a.fg : "var(--md-sys-color-on-surface-variant)"
    }));
  })));
}
Object.assign(__ds_scope, { ExerciseIconPicker });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/ExerciseIconPicker.jsx", error: String((e && e.message) || e) }); }

// components/navigation/BottomNav.jsx
try { (() => {
const items = [{
  key: "workout",
  label: "Тренировка",
  icon: "dumbbell"
}, {
  key: "programs",
  label: "Программы",
  icon: "clipboard-list"
}, {
  key: "history",
  label: "История",
  icon: "history"
}];

/**
 * BottomNav — 3-tab bottom navigation bar (spec section 2): Тренировка /
 * Программы / История.
 */
function BottomNav({
  active,
  onChange
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      margin: "0 16px 12px",
      padding: "6px",
      borderRadius: "var(--shape-full)",
      background: "color-mix(in srgb, var(--md-sys-color-surface-container-lowest) 82%, transparent)",
      backdropFilter: "blur(20px)",
      WebkitBackdropFilter: "blur(20px)",
      boxShadow: "var(--elevation-3)"
    }
  }, items.map(it => {
    const isActive = it.key === active;
    return /*#__PURE__*/React.createElement("button", {
      key: it.key,
      onClick: () => onChange && onChange(it.key),
      style: {
        flex: 1,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        gap: 3,
        padding: "8px 0",
        background: isActive ? "var(--md-sys-color-primary-container)" : "none",
        borderRadius: "var(--shape-full)",
        border: "none",
        cursor: "pointer",
        transition: "background-color var(--motion-duration-medium) var(--motion-easing-standard)",
        color: isActive ? "var(--md-sys-color-on-primary-container)" : "var(--md-sys-color-on-surface-variant)"
      }
    }, /*#__PURE__*/React.createElement(__ds_scope.Icon, {
      name: it.icon,
      size: 22,
      color: isActive ? "var(--md-sys-color-on-primary-container)" : "var(--md-sys-color-on-surface-variant)"
    }), /*#__PURE__*/React.createElement("span", {
      style: {
        fontFamily: "var(--font-brand)",
        fontSize: "var(--type-label-medium-size)",
        fontWeight: 600
      }
    }, it.label));
  }));
}
Object.assign(__ds_scope, { BottomNav });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/navigation/BottomNav.jsx", error: String((e && e.message) || e) }); }

// components/navigation/TopBar.jsx
try { (() => {
/**
 * TopBar — screen header. Two sizes, both kept:
 *   size="small"  — compact inline bar (back + title on one row, title-large).
 *                   Good for sheets/secondary bars.
 *   size="large"  — the app's default screen header: iOS-style large title
 *                   (headline-large), with the back button and any trailing
 *                   action on their own row above the title. Optional
 *                   `subtitle`, plus `children` for anything under the title
 *                   (metadata line, filter row).
 */
function TopBar({
  title,
  onBack,
  trailing,
  size = "small",
  subtitle,
  children
}) {
  if (size === "large") {
    const hasActionRow = onBack || trailing;
    return /*#__PURE__*/React.createElement("div", {
      style: {
        padding: "6px 24px 10px"
      }
    }, hasActionRow && /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        margin: "0 -12px 2px"
      }
    }, onBack ? /*#__PURE__*/React.createElement(__ds_scope.IconButton, {
      icon: "chevron-right",
      label: "\u041D\u0430\u0437\u0430\u0434",
      size: 44,
      onClick: onBack,
      style: {
        transform: "scaleX(-1)"
      }
    }) : /*#__PURE__*/React.createElement("span", null), trailing || /*#__PURE__*/React.createElement("span", null)), /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: "var(--font-brand)",
        fontSize: "var(--type-headline-large-size)",
        lineHeight: "var(--type-headline-large-line)",
        fontWeight: 700,
        letterSpacing: "-0.6px",
        color: "var(--md-sys-color-on-surface)",
        marginTop: hasActionRow ? 0 : 10
      }
    }, title), subtitle && /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: "var(--font-brand)",
        fontSize: "var(--type-body-medium-size)",
        color: "var(--md-sys-color-on-surface-variant)",
        marginTop: 4
      }
    }, subtitle), children);
  }

  // size === "small" — compact inline bar (original behavior).
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 4,
      padding: "8px 8px 8px 4px",
      background: "var(--md-sys-color-surface)"
    }
  }, onBack && /*#__PURE__*/React.createElement(__ds_scope.IconButton, {
    icon: "chevron-right",
    label: "\u041D\u0430\u0437\u0430\u0434",
    onClick: onBack,
    style: {
      transform: "scaleX(-1)"
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-title-large-size)",
      fontWeight: "var(--type-title-large-weight)",
      color: "var(--md-sys-color-on-surface)",
      paddingLeft: onBack ? 4 : 16
    }
  }, title), trailing);
}
Object.assign(__ds_scope, { TopBar });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/navigation/TopBar.jsx", error: String((e && e.message) || e) }); }

// components/overlays/BottomSheet.jsx
try { (() => {
/**
 * BottomSheet — the "puller" pattern the spec uses for exercise pick/edit
 * (3.3, 3.4) and the session's exercise-switcher list. Dismiss by tapping
 * the scrim or dragging the handle — no close button.
 */
function BottomSheet({
  open,
  title,
  onClose,
  children,
  height = "auto"
}) {
  if (!open) return null;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      position: "fixed",
      inset: 0,
      background: "color-mix(in srgb, var(--md-sys-color-scrim) 48%, transparent)",
      display: "flex",
      alignItems: "flex-end",
      justifyContent: "center",
      zIndex: 900
    },
    onClick: onClose
  }, /*#__PURE__*/React.createElement("div", {
    onClick: e => e.stopPropagation(),
    style: {
      background: "var(--md-sys-color-surface-container-lowest)",
      borderRadius: "var(--shape-extra-large-increased) var(--shape-extra-large-increased) 0 0",
      width: "100%",
      maxWidth: 480,
      maxHeight: "85vh",
      height,
      display: "flex",
      flexDirection: "column",
      boxShadow: "var(--elevation-4)"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      justifyContent: "center",
      padding: "12px 0 6px"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 40,
      height: 5,
      borderRadius: "var(--shape-full)",
      background: "var(--md-sys-color-outline)",
      cursor: "pointer"
    },
    onClick: onClose
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      padding: "4px 20px 12px"
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-title-large-size)",
      fontWeight: 700,
      letterSpacing: "-0.3px",
      color: "var(--md-sys-color-on-surface)"
    }
  }, title)), /*#__PURE__*/React.createElement("div", {
    style: {
      overflowY: "auto",
      padding: "0 20px 20px"
    }
  }, children)));
}
Object.assign(__ds_scope, { BottomSheet });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/overlays/BottomSheet.jsx", error: String((e && e.message) || e) }); }

// components/overlays/RestTimerOverlay.jsx
try { (() => {
/**
 * RestTimerOverlay — full-screen rest countdown (spec 4.3). The one
 * deliberately "alive" element in the product per the motion guidelines.
 */
function RestTimerOverlay({
  open,
  secondsLeft,
  totalSeconds,
  onSkip,
  onAdd30
}) {
  if (!open) return null;
  const pct = Math.max(0, Math.min(1, secondsLeft / totalSeconds));
  const mm = String(Math.floor(secondsLeft / 60)).padStart(2, "0");
  const ss = String(secondsLeft % 60).padStart(2, "0");
  return /*#__PURE__*/React.createElement("div", {
    style: {
      position: "fixed",
      inset: 0,
      background: "var(--md-sys-color-surface)",
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      justifyContent: "center",
      gap: 32,
      zIndex: 1100
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: "var(--type-label-large-size)",
      fontWeight: 600,
      letterSpacing: "1px",
      textTransform: "uppercase",
      color: "var(--md-sys-color-on-surface-variant)"
    }
  }, "\u041E\u0442\u0434\u044B\u0445"), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "relative",
      width: 240,
      height: 240
    }
  }, /*#__PURE__*/React.createElement("svg", {
    width: "240",
    height: "240",
    style: {
      transform: "rotate(-90deg)"
    }
  }, /*#__PURE__*/React.createElement("circle", {
    cx: "120",
    cy: "120",
    r: "104",
    stroke: "var(--md-sys-color-surface-container-high)",
    strokeWidth: "16",
    fill: "none"
  }), /*#__PURE__*/React.createElement("circle", {
    cx: "120",
    cy: "120",
    r: "104",
    stroke: "var(--md-sys-color-primary)",
    strokeWidth: "16",
    fill: "none",
    strokeLinecap: "round",
    strokeDasharray: 2 * Math.PI * 104,
    strokeDashoffset: 2 * Math.PI * 104 * (1 - pct),
    style: {
      transition: "stroke-dashoffset var(--motion-duration-short) linear"
    }
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      inset: 0,
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      fontFamily: "var(--font-numeric)",
      fontVariantNumeric: "tabular-nums",
      fontSize: "var(--type-numeric-hero-size)",
      fontWeight: "var(--type-numeric-hero-weight)",
      color: "var(--md-sys-color-on-surface)"
    }
  }, mm, ":", ss)), /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      gap: 12
    }
  }, /*#__PURE__*/React.createElement(__ds_scope.Button, {
    variant: "outlined",
    onClick: onAdd30
  }, "+30 \u0441\u0435\u043A"), /*#__PURE__*/React.createElement(__ds_scope.Button, {
    variant: "tonal",
    onClick: onSkip
  }, "\u041F\u0440\u043E\u043F\u0443\u0441\u0442\u0438\u0442\u044C")));
}
Object.assign(__ds_scope, { RestTimerOverlay });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/overlays/RestTimerOverlay.jsx", error: String((e && e.message) || e) }); }

// components/session/ExerciseStrip.jsx
try { (() => {
/**
 * ExerciseStrip — horizontal, scrollable strip of the session's exercises,
 * each showing done/total sets and its status (done · current · todo). The
 * "context" view of an active session; tap a card to switch exercises.
 * Pass already-shortened names — the strip does not truncate.
 */
function ExerciseStrip({
  items,
  onSelect
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      gap: 8,
      overflowX: "auto",
      padding: "2px 20px 6px",
      scrollbarWidth: "none"
    }
  }, items.map((it, i) => {
    const active = it.status === "current";
    const complete = it.status === "done";
    return /*#__PURE__*/React.createElement("div", {
      key: i,
      onClick: () => onSelect && onSelect(i),
      style: {
        flex: "0 0 auto",
        minWidth: 96,
        padding: "10px 14px",
        borderRadius: "var(--shape-large)",
        background: active ? "var(--md-sys-color-primary-container)" : "var(--md-sys-color-surface-container-low)",
        boxShadow: active ? "var(--elevation-2)" : "none",
        display: "flex",
        flexDirection: "column",
        gap: 7,
        cursor: onSelect ? "pointer" : "default"
      }
    }, /*#__PURE__*/React.createElement("span", {
      style: {
        fontFamily: "var(--font-brand)",
        fontSize: 13,
        fontWeight: 600,
        whiteSpace: "nowrap",
        color: active ? "var(--md-sys-color-on-primary-container)" : complete ? "var(--md-sys-color-on-surface-variant)" : "var(--md-sys-color-on-surface)"
      }
    }, it.name), /*#__PURE__*/React.createElement("span", {
      style: {
        display: "flex",
        alignItems: "center",
        gap: 6
      }
    }, complete ? /*#__PURE__*/React.createElement(__ds_scope.Icon, {
      name: "check",
      size: 14,
      color: "var(--md-sys-color-primary)"
    }) : null, /*#__PURE__*/React.createElement("span", {
      style: {
        fontFamily: "var(--font-numeric)",
        fontVariantNumeric: "tabular-nums",
        fontSize: 12,
        fontWeight: 600,
        color: active ? "var(--md-sys-color-on-primary-container)" : "var(--md-sys-color-on-surface-variant)",
        opacity: 0.85
      }
    }, it.done, "/", it.total)));
  }));
}
Object.assign(__ds_scope, { ExerciseStrip });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/session/ExerciseStrip.jsx", error: String((e && e.message) || e) }); }

// components/session/SetDots.jsx
try { (() => {
const dotColors = {
  hit: "var(--md-sys-color-primary)",
  // target met/exceeded
  miss: "var(--diff-negative)",
  // below target
  skipped: "var(--diff-skipped)",
  // set skipped
  current: "var(--md-sys-color-on-surface)",
  // the active set (elongated)
  todo: "var(--md-sys-color-surface-container-highest)" // not yet done
};

/**
 * SetDots — compact set-progress indicator: one dot per set, the current set
 * shown as an elongated pill. A glanceable summary of how a session's sets are
 * going without the full per-set breakdown.
 */
function SetDots({
  states
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      gap: 8,
      justifyContent: "center",
      alignItems: "center"
    }
  }, states.map((st, i) => /*#__PURE__*/React.createElement("span", {
    key: i,
    style: {
      width: st === "current" ? 26 : 8,
      height: 8,
      borderRadius: 999,
      background: dotColors[st] || dotColors.todo
    }
  })));
}
Object.assign(__ds_scope, { SetDots });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/session/SetDots.jsx", error: String((e && e.message) || e) }); }

// components/stepper/Stepper.jsx
try { (() => {
/**
 * Stepper — large ± control for numeric input, with the number itself as a
 * tap-to-edit field so a precise value can be typed on the rare occasion
 * the keyboard is already up. The single most-used control in the product:
 * rep count (step 1) and weight (step 1kg) during an active session.
 *
 * The control has a constant overall width (`--stepper-width`), with the
 * ± buttons pinned to its edges — stacked steppers (reps over weight) keep
 * their buttons on exactly the same vertical axis, so one-handed taps land
 * on muscle memory regardless of how wide the value is ("8" vs "60,5 кг").
 * The value flexes in the middle and is tap-to-edit for exact keyboard
 * entry.
 */
function Stepper({
  value,
  onChange,
  step = 1,
  min = 0,
  max = Infinity,
  unit,
  size = "large"
}) {
  const dims = size === "large" ? {
    btn: "var(--stepper-target)",
    font: "var(--type-numeric-large-size)",
    width: "var(--stepper-width, 248px)"
  } : {
    btn: "var(--hit-target-min)",
    font: "var(--type-numeric-medium-size)",
    width: "var(--stepper-width, 196px)"
  };
  const [editing, setEditing] = React.useState(false);
  const [draft, setDraft] = React.useState(String(value));
  const inputRef = React.useRef(null);
  React.useEffect(() => {
    if (!editing) setDraft(String(value));
  }, [value, editing]);
  const clamp = n => Math.min(max, Math.max(min, n));
  /* Decimals display with a comma, per the brand numeral rule («62,5 кг»);
     keyboard entry already accepts both separators. */
  const display = String(value).replace(".", ",");
  const dec = () => onChange && onChange(+clamp(value - step).toFixed(2));
  const inc = () => onChange && onChange(+clamp(value + step).toFixed(2));
  function startEdit() {
    setDraft(String(value));
    setEditing(true);
    requestAnimationFrame(() => inputRef.current && inputRef.current.select());
  }
  function commit() {
    const parsed = parseFloat(draft.replace(",", "."));
    if (!Number.isNaN(parsed)) onChange && onChange(+clamp(parsed).toFixed(2));
    setEditing(false);
  }
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      justifyContent: "space-between",
      gap: 8,
      width: dims.width
    }
  }, /*#__PURE__*/React.createElement(StepBtn, {
    dim: dims.btn,
    onClick: dec,
    disabled: value <= min,
    icon: "minus"
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0,
      display: "flex",
      justifyContent: "center",
      whiteSpace: "nowrap"
    }
  }, editing ? /*#__PURE__*/React.createElement("input", {
    ref: inputRef,
    value: draft,
    onChange: e => setDraft(e.target.value),
    onBlur: commit,
    onKeyDown: e => {
      if (e.key === "Enter") {
        e.currentTarget.blur();
      }
      if (e.key === "Escape") {
        setDraft(String(value));
        setEditing(false);
      }
    },
    inputMode: "decimal",
    autoFocus: true,
    style: {
      width: "100%",
      textAlign: "center",
      fontFamily: "var(--font-numeric)",
      fontVariantNumeric: "tabular-nums",
      fontSize: dims.font,
      fontWeight: 700,
      letterSpacing: "-0.5px",
      color: "var(--md-sys-color-on-surface)",
      background: "transparent",
      border: "none",
      borderBottom: "1.5px solid var(--md-sys-color-primary)",
      outline: "none",
      padding: 0
    }
  }) : /*#__PURE__*/React.createElement("button", {
    onClick: startEdit,
    "aria-label": "\u0412\u0432\u0435\u0441\u0442\u0438 \u0437\u043D\u0430\u0447\u0435\u043D\u0438\u0435",
    style: {
      width: "100%",
      textAlign: "center",
      fontFamily: "var(--font-numeric)",
      fontVariantNumeric: "tabular-nums",
      fontSize: dims.font,
      fontWeight: 700,
      letterSpacing: "-0.5px",
      color: "var(--md-sys-color-on-surface)",
      background: "none",
      border: "none",
      padding: 0,
      cursor: "text"
    }
  }, display, unit && /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: "0.5em",
      marginLeft: 4,
      color: "var(--md-sys-color-on-surface-variant)"
    }
  }, unit))), /*#__PURE__*/React.createElement(StepBtn, {
    dim: dims.btn,
    onClick: inc,
    disabled: value >= max,
    icon: "plus"
  }));
}
function StepBtn({
  dim,
  onClick,
  disabled,
  icon
}) {
  const [press, setPress] = React.useState(false);
  return /*#__PURE__*/React.createElement("button", {
    onClick: disabled ? undefined : onClick,
    onMouseDown: () => setPress(true),
    onMouseUp: () => setPress(false),
    onMouseLeave: () => setPress(false),
    disabled: disabled,
    style: {
      width: dim,
      height: dim,
      borderRadius: "50%",
      border: "none",
      background: press ? "var(--md-sys-color-primary)" : "var(--md-sys-color-surface-container-high)",
      color: press ? "var(--md-sys-color-on-primary)" : "var(--md-sys-color-on-surface)",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      cursor: disabled ? "default" : "pointer",
      opacity: disabled ? 0.38 : 1,
      transform: press && !disabled ? "scale(0.94)" : "scale(1)",
      transition: "background-color var(--motion-duration-short) var(--motion-easing-standard), transform var(--motion-duration-short) var(--motion-easing-standard)",
      flexShrink: 0
    }
  }, /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: icon,
    size: 24
  }));
}
Object.assign(__ds_scope, { Stepper });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/stepper/Stepper.jsx", error: String((e && e.message) || e) }); }

// components/session/TrackSetRow.jsx
try { (() => {
/**
 * TrackSetRow — one set inside the active-session tracker, and the backbone of
 * the tracking screen: a vertical list of these IS the exercise.
 *
 * Two shapes:
 *  - `current` is an elevated focus card whose two steppers are STACKED, label
 *    to the left. Side by side they get ~139px each at phone width, which is
 *    less than a 48+48 stepper plus a value carrying its unit — and stacking is
 *    what Stepper was built for: constant width with the ± pinned to the edges,
 *    so both rows share one vertical tap axis and one-handed taps land on
 *    muscle memory. Everything
 *    the lifter needs to decide a number sits in one quiet reference line
 *    under the set number («цель 10×26 · в прошлый раз 10×24») instead of
 *    competing with it — an earlier version put those in a header and a
 *    separate footer band, which gave the card three competing text blocks and
 *    an ad-hoc vertical rhythm.
 *  - every other set is a compact 36px row, tinted by how it went. It carries
 *    a bare index digit rather than «Подход N»: inside a list the position
 *    already says which set it is, and dropping the word is what makes seven
 *    resting sets fit on screen beside the focus card.
 *
 * Five resting states, and the tone vocabulary is shared with DiffRow and
 * ProgressTrack so the same outcome always looks the same anywhere in the
 * product:
 *
 *   hit     ровно в цель      soft green   circle-check
 *   exceed  больше цели       saturated    circle-arrow-up
 *   miss    меньше цели       red          circle-arrow-down
 *   skip    пропущен          grey         circle-minus
 *   todo    ещё не выполнен   neutral      circle       (shows «цель …»)
 *
 * Arrows rather than a tick/cross for exceed and miss: below target is not a
 * failure, it is a direction, and an ✗ next to a set the lifter completed
 * reads as punishment.
 */
const num = v => String(v).replace(".", ",");
const fmtSet = (reps, weight) => weight > 0 ? `${num(reps)}×${num(weight)} кг` : `${num(reps)} повт`;
const TONES = {
  hit: {
    fg: "var(--diff-met)",
    bg: "var(--diff-met-bg)",
    icon: "circle-check"
  },
  exceed: {
    fg: "var(--diff-positive)",
    bg: "var(--diff-positive-bg)",
    icon: "circle-arrow-up"
  },
  miss: {
    fg: "var(--diff-negative)",
    bg: "var(--diff-negative-bg)",
    icon: "circle-arrow-down"
  },
  skip: {
    fg: "var(--diff-skipped)",
    bg: "var(--diff-skipped-bg)",
    icon: "circle-minus"
  },
  todo: {
    fg: "var(--md-sys-color-outline)",
    bg: "var(--md-sys-color-surface-container-low)",
    icon: "circle"
  }
};
function TrackSetRow({
  n,
  state = "todo",
  reps,
  weight,
  target,
  last,
  onRepsChange,
  onWeightChange,
  repsStep = 1,
  weightStep = 2.5,
  children
}) {
  if (state === "current") {
    const reference = [target && `цель ${target}`, last && `в прошлый раз ${last}`].filter(Boolean).join(" · ");
    return /*#__PURE__*/React.createElement("div", {
      style: {
        background: "var(--md-sys-color-surface-container-lowest)",
        borderRadius: "var(--shape-extra-large)",
        /* Accent ring as an INSET shadow at an integer width. Two earlier
           attempts rendered unevenly: `outline: 1.5px` rounds differently
           per side on a rounded corner, and an outward `0 0 0 1.5px` spread
           both lands on half device pixels and gets clipped by the scroll
           container once the card is stuck to its bottom edge. Inset is
           painted inside the border box, so it follows the radius exactly
           and no ancestor can crop it. */
        boxShadow: "inset 0 0 0 2px var(--md-sys-color-primary), var(--elevation-2)",
        padding: 18
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        alignItems: "center",
        gap: 8
      }
    }, /*#__PURE__*/React.createElement(__ds_scope.Icon, {
      name: "circle-dot",
      size: 17,
      color: "var(--md-sys-color-primary)"
    }), /*#__PURE__*/React.createElement("span", {
      style: {
        fontFamily: "var(--font-brand)",
        fontSize: 14.5,
        fontWeight: 600,
        letterSpacing: "-0.1px",
        color: "var(--md-sys-color-on-surface)"
      }
    }, "\u041F\u043E\u0434\u0445\u043E\u0434 ", n)), reference && /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: "var(--font-brand)",
        fontVariantNumeric: "tabular-nums",
        fontSize: 12,
        color: "var(--md-sys-color-on-surface-variant)",
        marginTop: 4,
        paddingLeft: 25
      }
    }, reference), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        flexDirection: "column",
        gap: 10,
        marginTop: 14
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        alignItems: "center",
        gap: 12
      }
    }, /*#__PURE__*/React.createElement("span", {
      style: {
        width: 40,
        flexShrink: 0
      }
    }, /*#__PURE__*/React.createElement(__ds_scope.Overline, null, "\u041F\u043E\u0432\u0442")), /*#__PURE__*/React.createElement("div", {
      style: {
        flex: 1,
        minWidth: 0,
        "--stepper-width": "100%"
      }
    }, /*#__PURE__*/React.createElement(__ds_scope.Stepper, {
      size: "medium",
      value: reps,
      step: repsStep,
      onChange: onRepsChange
    }))), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        alignItems: "center",
        gap: 12
      }
    }, /*#__PURE__*/React.createElement("span", {
      style: {
        width: 40,
        flexShrink: 0
      }
    }, /*#__PURE__*/React.createElement(__ds_scope.Overline, null, "\u0412\u0435\u0441")), /*#__PURE__*/React.createElement("div", {
      style: {
        flex: 1,
        minWidth: 0,
        "--stepper-width": "100%"
      }
    }, /*#__PURE__*/React.createElement(__ds_scope.Stepper, {
      size: "medium",
      value: weight,
      step: weightStep,
      unit: "\u043A\u0433",
      onChange: onWeightChange
    })))), children && /*#__PURE__*/React.createElement("div", {
      style: {
        marginTop: 16
      }
    }, children));
  }
  const t = TONES[state] || TONES.todo;
  const isTodo = state === "todo";
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "center",
      gap: 10,
      padding: "9px 16px",
      borderRadius: "var(--shape-large)",
      background: t.bg
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-numeric)",
      fontVariantNumeric: "tabular-nums",
      fontSize: 12,
      fontWeight: 600,
      color: t.fg,
      width: 12,
      flexShrink: 0
    }
  }, n), /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: t.icon,
    size: 17,
    color: t.fg
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      flex: 1
    }
  }), state === "skip" ? /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-brand)",
      fontSize: 13,
      fontWeight: 500,
      color: t.fg
    }
  }, "\u043F\u0440\u043E\u043F\u0443\u0449\u0435\u043D") : isTodo ? /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-numeric)",
      fontVariantNumeric: "tabular-nums",
      fontSize: 13.5,
      fontWeight: 600,
      color: "var(--md-sys-color-on-surface-variant)"
    }
  }, "\u0446\u0435\u043B\u044C ", target) : /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-numeric)",
      fontVariantNumeric: "tabular-nums",
      fontSize: 14.5,
      fontWeight: 700,
      letterSpacing: "-0.2px",
      color: t.fg
    }
  }, fmtSet(reps, weight)));
}
Object.assign(__ds_scope, { TrackSetRow });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/session/TrackSetRow.jsx", error: String((e && e.message) || e) }); }

// components/session/ExerciseSetList.jsx
try { (() => {
/**
 * ExerciseSetList — the composition of the tracking screen: every set of the
 * active exercise in order, with the current one as a focus card that is
 * **sticky to the bottom of the scroll area**.
 *
 * This solves the problem a plain list has. A focus card is ~200px and a
 * resting row is 36px, so past six or seven sets the list is taller than the
 * space between the header and the dock. In a plain scroller the card — the
 * single element the lifter actually touches — scrolls off screen, which is
 * the one thing that must never happen mid-set.
 *
 * `position: sticky; bottom: 0` fixes it without splitting the list in two:
 *  - completed sets scroll up behind the card, so history is one flick away;
 *  - upcoming sets sit below in true order and are reachable by scrolling;
 *  - the card itself never leaves the thumb zone, at any set count;
 *  - when the list is short enough to fit, sticky is inert and everything
 *    simply sits in normal flow.
 *
 * A horizontal carousel of set plaques was the alternative and is deliberately
 * not used: it asks for a sideways swipe from someone standing under a bar,
 * and it cannot show a result and a target in the same glance.
 */
function ExerciseSetList({
  sets = [],
  onRepsChange,
  onWeightChange,
  repsStep = 1,
  weightStep = 2.5,
  currentExtra,
  maxHeight = "100%",
  style
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      flexDirection: "column",
      gap: 8,
      overflowY: "auto",
      maxHeight,
      padding: "2px 0 2px",
      ...style
    }
  }, sets.map((s, i) => {
    const isCurrent = s.state === "current";
    const row = /*#__PURE__*/React.createElement(__ds_scope.TrackSetRow, {
      n: i + 1,
      state: s.state,
      reps: s.reps,
      weight: s.weight,
      target: s.target,
      last: s.last,
      repsStep: repsStep,
      weightStep: weightStep,
      onRepsChange: onRepsChange,
      onWeightChange: onWeightChange
    }, isCurrent ? currentExtra : null);
    if (!isCurrent) return /*#__PURE__*/React.createElement("div", {
      key: i
    }, row);
    return /*#__PURE__*/React.createElement("div", {
      key: i,
      style: {
        position: "sticky",
        bottom: 0,
        zIndex: 2,
        /* room for the card's elevation shadow, which would otherwise be
           cropped by the scroller once the card is stuck to its bottom */
        paddingBottom: 4
      }
    }, row);
  }));
}
Object.assign(__ds_scope, { ExerciseSetList });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/session/ExerciseSetList.jsx", error: String((e && e.message) || e) }); }

__ds_ns.ExerciseCard = __ds_scope.ExerciseCard;

__ds_ns.ListRow = __ds_scope.ListRow;

__ds_ns.ProgramCard = __ds_scope.ProgramCard;

__ds_ns.SessionCard = __ds_scope.SessionCard;

__ds_ns.Badge = __ds_scope.Badge;

__ds_ns.Button = __ds_scope.Button;

__ds_ns.Chip = __ds_scope.Chip;

__ds_ns.ExerciseIcon = __ds_scope.ExerciseIcon;

__ds_ns.ACCENTS = __ds_scope.ACCENTS;

__ds_ns.ACCENT_NAMES = __ds_scope.ACCENT_NAMES;

__ds_ns.ExerciseMark = __ds_scope.ExerciseMark;

__ds_ns.Icon = __ds_scope.Icon;

__ds_ns.IconButton = __ds_scope.IconButton;

__ds_ns.Overline = __ds_scope.Overline;

__ds_ns.ProgressTrack = __ds_scope.ProgressTrack;

__ds_ns.Switch = __ds_scope.Switch;

__ds_ns.TextField = __ds_scope.TextField;

__ds_ns.EXERCISE_ICONS = __ds_scope.EXERCISE_ICONS;

__ds_ns.EXERCISE_ICON_ORDER = __ds_scope.EXERCISE_ICON_ORDER;

__ds_ns.EXERCISES = __ds_scope.EXERCISES;

__ds_ns.SETS = __ds_scope.SETS;

__ds_ns.SessionStopwatch = __ds_scope.SessionStopwatch;

__ds_ns.Dialog = __ds_scope.Dialog;

__ds_ns.DiffRow = __ds_scope.DiffRow;

__ds_ns.EmptyState = __ds_scope.EmptyState;

__ds_ns.AccentPicker = __ds_scope.AccentPicker;

__ds_ns.ExerciseIconPicker = __ds_scope.ExerciseIconPicker;

__ds_ns.BottomNav = __ds_scope.BottomNav;

__ds_ns.TopBar = __ds_scope.TopBar;

__ds_ns.BottomSheet = __ds_scope.BottomSheet;

__ds_ns.RestTimerOverlay = __ds_scope.RestTimerOverlay;

__ds_ns.ExerciseSetList = __ds_scope.ExerciseSetList;

__ds_ns.ExerciseStrip = __ds_scope.ExerciseStrip;

__ds_ns.SetDots = __ds_scope.SetDots;

__ds_ns.TrackSetRow = __ds_scope.TrackSetRow;

__ds_ns.Stepper = __ds_scope.Stepper;

})();
