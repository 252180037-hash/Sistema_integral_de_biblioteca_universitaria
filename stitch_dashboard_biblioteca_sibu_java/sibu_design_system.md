---
name: "SIBU — Sistema Integrado de Bibliotecas"
description: "Editorial-technical design system for SIBU, combining university library operations (RF1–RF8) with ISO/IEC 25010:2023 software quality telemetry."
---

## Overview

SIBU is a desktop-first academic library management and software quality dashboard built for a Java-based five-layer architecture. The interface pairs scholarly authority with engineering precision: calm, high-contrast light surfaces, disciplined tabular data, and clear role-based navigation for **Administrador**, **Bibliotecario**, and **Lector**.

The visual style is **Corporate / Modern** with tonal surface layering, crisp 1px structural outlines, and restrained semantic color coding reserved strictly for operational states, circulation alerts, and ISO/IEC 25010:2023 compliance thresholds.

## Colors

The palette is anchored in **Evergreen** (`#243119`), **Sage Green** (`#629460`), and **Tea Green** (`#C9F2C7`), paired with **Light Green** (`#ACECA1`) and **Muted Olive** (`#96BE8C`) across crisp botanical surfaces.

- **Primary:** Deep sage (`#376637`) for high-contrast primary actions, transitioning to `--sage-green` (`#629460`) on hover/container states.
- **Secondary:** Muted olive (`#43673D`) and `--tea-green` (`#C9F2C7`) for active navigation states and supporting containers.
- **Text & Ink:** `--evergreen` (`#243119`) anchors primary typography and dark-on-light contrast.
- **Status tokens:** `success`, `warning`, and `error` carry operational and ISO/IEC 25010 compliance meaning only — never decorative.

```yaml
colors:
  tea-green: "#C9F2C7"
  light-green: "#ACECA1"
  muted-olive: "#96BE8C"
  sage-green: "#629460"
  evergreen: "#243119"
  success: "#146C2E"
  on-success: "#FFFFFF"
  success-container: "#C9F2C7"
  on-success-container: "#243119"
  warning: "#8C5000"
  on-warning: "#FFFFFF"
  warning-container: "#FFDCBE"
  on-warning-container: "#2C1600"
  light:
    primary: "#376637"
    on-primary: "#FFFFFF"
    primary-container: "#629460"
    on-primary-container: "#212121"
    secondary: "#43673D"
    on-secondary: "#FFFFFF"
    secondary-container: "#C9F2C7"
    on-secondary-container: "#243119"
    tertiary: "#89475F"
    on-tertiary: "#FFFFFF"
    tertiary-container: "#A65F77"
    on-tertiary-container: "#FFFFFF"
    error: "#BA1A1A"
    on-error: "#FFFFFF"
    error-container: "#FFDAD6"
    on-error-container: "#690005"
    background: "#F4FBF2"
    on-background: "#243119"
    surface: "#F4FBF2"
    surface-dim: "#D5E4D1"
    surface-bright: "#FFFFFF"
    surface-container-lowest: "#FFFFFF"
    surface-container-low: "#EBF5E8"
    surface-container: "#E2F0DE"
    surface-container-high: "#DAE9D6"
    surface-container-highest: "#D1E2CC"
    on-surface: "#243119"
    surface-variant: "#DDE5D8"
    on-surface-variant: "#3D4B31"
    outline: "#72796E"
    outline-variant: "#96BE8C"
    inverse-surface: "#243119"
    inverse-on-surface: "#E6F6D2"
    inverse-primary: "#ACECA1"
  dark:
    primary: "#ACECA1"
    on-primary: "#243119"
    primary-container: "#629460"
    on-primary-container: "#212121"
    secondary: "#96BE8C"
    on-secondary: "#243119"
    secondary-container: "#2C4F27"
    on-secondary-container: "#C9F2C7"
    tertiary: "#FFB0C9"
    on-tertiary: "#541C33"
    tertiary-container: "#C67A94"
    on-tertiary-container: "#410C23"
    error: "#FFB4AB"
    on-error: "#690005"
    error-container: "#93000A"
    on-error-container: "#FFDAD6"
    background: "#0B1603"
    on-background: "#D8E8C4"
    surface: "#0B1603"
    surface-dim: "#0B1603"
    surface-bright: "#303D24"
    surface-container-lowest: "#061101"
    surface-container-low: "#131F09"
    surface-container: "#17230C"
    surface-container-high: "#212E16"
    surface-container-highest: "#2B3920"
    on-surface: "#D8E8C4"
    surface-variant: "#41493F"
    on-surface-variant: "#C1C9BC"
    outline: "#8B9388"
    outline-variant: "#41493F"
    inverse-surface: "#D8E8C4"
    inverse-on-surface: "#243119"
    inverse-primary: "#376637"
```

## Typography

**Red Hat Text** anchors headings and KPI figures with geometric clarity. **Instrument Sans** handles all body copy, table cells, and navigation labels for clean legibility at compact dashboard densities. **Spline Sans Mono** is reserved for ISBNs, requirement codes (`RF1`–`RF8`), ISO/IEC 25010:2023 latency/RPO/RTO metrics, and audit log timestamps.

```yaml
typography:
  display-md:
    fontFamily: "Red Hat Text"
    fontSize: 36px
    fontWeight: 700
    lineHeight: 44px
    letterSpacing: -0.5px
  display-md-mobile:
    fontFamily: "Red Hat Text"
    fontSize: 28px
    fontWeight: 700
    lineHeight: 34px
    letterSpacing: -0.25px
  headline-lg:
    fontFamily: "Red Hat Text"
    fontSize: 24px
    fontWeight: 600
    lineHeight: 32px
    letterSpacing: -0.25px
  headline-md:
    fontFamily: "Red Hat Text"
    fontSize: 18px
    fontWeight: 600
    lineHeight: 24px
    letterSpacing: 0px
  title-md:
    fontFamily: "Instrument Sans"
    fontSize: 16px
    fontWeight: 600
    lineHeight: 22px
    letterSpacing: 0px
  body-md:
    fontFamily: "Instrument Sans"
    fontSize: 14px
    fontWeight: 400
    lineHeight: 20px
    letterSpacing: 0px
  body-sm:
    fontFamily: "Instrument Sans"
    fontSize: 12px
    fontWeight: 400
    lineHeight: 16px
    letterSpacing: 0px
  label-lg:
    fontFamily: "Instrument Sans"
    fontSize: 14px
    fontWeight: 500
    lineHeight: 20px
    letterSpacing: 0px
  label-sm:
    fontFamily: "Instrument Sans"
    fontSize: 12px
    fontWeight: 500
    lineHeight: 16px
    letterSpacing: 0.25px
  mono-sm:
    fontFamily: "Spline Sans Mono"
    fontSize: 12px
    fontWeight: 500
    lineHeight: 16px
    letterSpacing: 0px
```

## Layout & Spacing

Desktop-first **12-column Fluid Grid** paired with a persistent `260px` left navigation rail on `lg:` viewports and above, collapsing cleanly to a single-column stack on smaller screens. Built on a strict `4px` / `8px` spacing rhythm.

```yaml
spacing:
  gutter-xs: 4px
  gutter-sm: 8px
  gutter-md: 16px
  gutter-lg: 24px
  gutter-xl: 32px
  section: 48px
  page-margin: 24px
```

## Elevation & Depth

Hierarchy relies on **tonal surface layering** (`surface` → `surface-container-low` → `surface-container-lowest`) combined with crisp `1px` `outline-variant` borders. Drop shadows are omitted on resting cards and reserved solely for elevated overlays or dropdowns.

## Shapes

Structured, technical geometry with restrained corner rounding: `8px` default for cards and inputs, `6px` / `4px` for compact badges and table chips, and `9999px` pill geometry for status indicators.

```yaml
rounded:
  sm: 4px
  DEFAULT: 8px
  md: 12px
  lg: 16px
  full: 9999px
```

## Motion & Micro-interaction

Motion is crisp and functional. Interactive controls respond in `120ms` (`state-change`), while tab switches and panel transitions settle in `220ms` (`surface-enter`). No decorative loops; `prefers-reduced-motion` is respected.

```yaml
motion:
  state-change:
    duration: 120ms
    delay: 0ms
    timingFunction: ease-out
  surface-enter:
    duration: 220ms
    delay: 0ms
    timingFunction: cubic-bezier(0.2, 0, 0, 1)
  surface-exit:
    duration: 150ms
    delay: 0ms
    timingFunction: cubic-bezier(0.3, 0, 1, 1)
```

## Voice & Content Strategy

- **Tone of voice:** Clear, direct, and technical in Spanish (es-ES / es-MX neutral).
- **Capitalisation:** Sentence case for headings, buttons, and table headers (`Préstamos activos`, `Exportar CSV`).
- **Terminology:** Use exact SIBU domain terms: `Ejemplar`, `Circulación`, `Bitácora`, `Sanción`, `Respaldo`, `RF1`–`RF8`, `ISO/IEC 25010:2023`.
- **Action labels:** Verb + object (`Exportar CSV`, `Registrar préstamo`, `Ejecutar respaldo`).
- **Numbers & units:** Use `tabular-nums` for all counts, latencies (`p95 < 2 s`), percentages (`99.4%`), and ISO timestamps.

## Components

Component implementations live as individual HTML sticker sheet files in
`components/`. Each file contains the canonical, styled HTML components with
`<!-- COMPONENT: name -->` comment markers. Read each file before use.

- **Badge** → `@components/badge.html`: `badge-numeric`, `badge-dot`
- **Button** → `@components/button.html`: `button-primary`, `button-icon`, `button-with-icon`
- **Card** → `@components/card.html`: `card-elevated`, `card-action`, `card-interactive`, `card-interactive-2`
- **Table** → `@components/table.html`: `table-standard`, `table-interactive`, `table-selectable`
- **Tabs** → `@components/tabs.html`: `tabs-horizontal`, `tabs-with-icon`
