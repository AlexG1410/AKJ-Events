---
name: AKJ Events Design System
colors:
  surface: '#fff8f5'
  surface-dim: '#e1d8d4'
  surface-bright: '#fff8f5'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#fbf2ed'
  surface-container: '#f5ece7'
  surface-container-high: '#efe6e2'
  surface-container-highest: '#e9e1dc'
  on-surface: '#1e1b18'
  on-surface-variant: '#56423d'
  inverse-surface: '#34302c'
  inverse-on-surface: '#f8efea'
  outline: '#89726c'
  outline-variant: '#ddc0b9'
  surface-tint: '#a04028'
  primary: '#9c3e26'
  on-primary: '#ffffff'
  primary-container: '#bc553b'
  on-primary-container: '#fffbff'
  inverse-primary: '#ffb4a2'
  secondary: '#3d6752'
  on-secondary: '#ffffff'
  secondary-container: '#bcead0'
  on-secondary-container: '#416b57'
  tertiary: '#755b00'
  on-tertiary: '#ffffff'
  tertiary-container: '#c9a84b'
  on-tertiary-container: '#4f3d00'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdad2'
  primary-fixed-dim: '#ffb4a2'
  on-primary-fixed: '#3c0700'
  on-primary-fixed-variant: '#802913'
  secondary-fixed: '#bfedd3'
  secondary-fixed-dim: '#a3d1b8'
  on-secondary-fixed: '#002114'
  on-secondary-fixed-variant: '#244f3c'
  tertiary-fixed: '#ffe08f'
  tertiary-fixed-dim: '#e6c363'
  on-tertiary-fixed: '#241a00'
  on-tertiary-fixed-variant: '#584400'
  background: '#fff8f5'
  on-background: '#1e1b18'
  surface-variant: '#e9e1dc'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 57px
    fontWeight: '700'
    lineHeight: 64px
    letterSpacing: -0.25px
  display-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 45px
    fontWeight: '600'
    lineHeight: 52px
    letterSpacing: 0px
  display-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 36px
    fontWeight: '600'
    lineHeight: 44px
    letterSpacing: 0px
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: 0px
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: 0px
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: 0px
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: 0px
  title-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: 0px
  title-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: 0.15px
  title-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.1px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.5px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.25px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0.4px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.1px
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.5px
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.5px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  spacing-4: 0.25rem
  spacing-8: 0.5rem
  spacing-12: 0.75rem
  spacing-16: 1rem
  spacing-20: 1.25rem
  spacing-24: 1.5rem
  spacing-32: 2rem
  spacing-40: 2.5rem
  spacing-48: 3rem
  margin-mobile: 1rem
  margin-tablet: 1.5rem
  margin-desktop: 2rem
  gutter: 1rem
---

## Brand & Style

This design system delivers a tactile, community-focused experience for Android using Material Design 3 and Jetpack Compose foundations. Built for local discovery, communal gatherings, and civic engagement, the interface emphasizes human warmth, accessibility, and intuitive navigation.

The aesthetic blends Modern Material 3 principles with organic lifestyle cues:
- **Warm Editorial Presence:** Cream-toned canvases replace sterile cool whites, providing an inviting, sunlit foundation.
- **Approachable Tactility:** Soft squoval geometries, generous touch targets (minimum 48dp), and gentle surface shifts evoke physical paper, printed community boards, and welcoming event spaces.
- **Empathetic Feedback:** Dynamic color-tinted containers convey state changes, attendance confirmations, and verified host credentials without visual clutter or clinical severity.

## Colors

The palette grounds modern digital interactions in earth-inspired, human-scale hues:

- **Primary (`#D96B4F` Terracotta):** Anchors main actions, Floating Action Buttons (FAB), active navigation indicators, and high-priority callouts. Deepened to `#B54E35` on press states; paired with `#FBECE8` for relaxed pill containers.
- **Secondary (`#5F8A74` Sage Green):** Highlights environmental, outdoor, or wellness categories, verified community badges, and secondary affirmative states. Paired with `#E8F0EC` for passive chips.
- **Tertiary (`#F4D06F` Honey Pastel):** Celebrates achievements, ticket confirmations, spotlight badges, and special attendee milestones, supported by `#FDF7E7` for highlights.
- **Surfaces & Neutral Hierarchy:** 
  - Canvas / Screen Background: `#FAF7F2` (Warm Cream)
  - Surface Variant / Surface Container Low: `#F3EFEA` (Light Beige)
  - Card Surfaces / Surface Container: `#FFFFFF`
  - High Emphasis Text / Icons: `#2D2926` (Deep Charcoal)
  - Medium Emphasis Text / Meta details: `#6E6862` (Warm Slate)
  - Disabled / Divider Borders: `#9E9790` (Soft Muted Clay)

## Typography

The type system uses **Plus Jakarta Sans**, chosen for its friendly, contemporary curves and open apertures that remain readable at all scales on mobile hardware. 

- **Display & Headline:** Used for major screen anchors, event hero titles, and conversational banners. Letterforms lean on medium and semi-bold weights rather than ultra-bold, maintaining warmth without aggressive visual weight.
- **Title:** Directs card labels, group headings, and modal titles with balanced density.
- **Body:** Calibrated line-heights prevent text fatigue during longer descriptions, itinerary breakdowns, and host profiles.
- **Label:** Reserved for actionable elements, interactive category chips, tab titles, and system meta timestamps.

## Layout & Spacing

Layouts adhere to an 8dp baseline grid (with a secondary 4dp alignment rule for tight micro-copy and icon pairing), standard for Jetpack Compose:

- **Mobile (<600dp):** Single-column layout with 16dp horizontal margins. Content cards stretch full width within margins or bleed into horizontal paging carousels with 16dp snap offsets.
- **Tablet (600dp - 839dp):** 8-column layout with 24dp margins and 16dp gutters. Navigation translates from bottom bar to a left-anchored Navigation Rail. Event feeds adapt into a 2-column masonry or balanced dual-card grid.
- **Desktop / Foldable Expanded (840dp+):** 12-column adaptive layout with 32dp margins, incorporating permanent split-view layouts (e.g., interactive map anchored on the right, filtered list stream on the left).

## Elevation & Depth

Visual hierarchy uses Material 3 tonal elevation and warm-tinted ambient drop shadows:

- **Level 0 (Flat):** `#FAF7F2` canvas. Base lists, full-screen map underlays, and inline category rails sit directly on the background.
- **Level 1 (Card Default):** `#FFFFFF` surfaces with a tinted ambient shadow (`box-shadow: 0px 2px 8px rgba(45, 41, 38, 0.05)`). Used for standard event feed items and profile info containers.
- **Level 2 (Active/Pressed Cards, Chips, App Bars):** Elevated surface with `box-shadow: 0px 4px 12px rgba(45, 41, 38, 0.08)`. Applied to top app bars during scroll and resting action chips.
- **Level 3 (Floating Action Button & Bottom Navigation):** Raised above scrolling content with `box-shadow: 0px 6px 16px rgba(217, 107, 79, 0.16)`. The primary FAB inherits terracotta-tinted glow values.
- **Level 4 (Modals, Bottom Sheets):** `#FFFFFF` elevated container with `box-shadow: 0px 12px 24px rgba(45, 41, 38, 0.12)`, anchored over a 40% `#2D2926` scrim.

## Shapes

The design system incorporates intentional squoval corner radiuses to soften the overall interface:

- **Shape Large (20dp):** Applied to primary event cards, modal bottom sheets, map detail pop-ups, and hero media containers.
- **Shape Medium (14dp):** Applied to standard text fields, filter dialogs, elevated secondary banners, and action buttons.
- **Shape Small (8dp):** Applied to verified creator badges, compact category chips, attendance status pills, and thumbnail images.
- **Full / Pill (999dp):** Applied to active navigation selection pills, action buttons, search bars, and the central Floating Action Button.

## Components

### Bottom Navigation Bar
- **Structure:** 5 destinations: *Inicio* (Home), *Mapa* (Map), *Crear* (Create / FAB), *Notificaciones* (Alerts), *Perfil* (Profile).
- **Styling:** Grounded in a `#FFFFFF` container with a subtle `#F3EFEA` border on top. Active destination features an animated terracotta pill indicator (`#FBECE8`) with deep terracotta iconography (`#B54E35`) and bold label styling. Inactive destinations utilize `#6E6862`.

### Buttons & FAB
- **Primary FAB (Crear):** Centered or bottom-right floating, rendered in solid Terracotta (`#D96B4F`) with white iconography (`#FFFFFF`), utilizing a 16dp rounded squoval or full pill shape.
- **Filled Button:** Solid `#D96B4F` with `#FFFFFF` text for primary confirmations (e.g., "Inscribirse"). Height: 48dp, corner radius: 14dp.
- **Tonal Button:** `#FBECE8` container with `#B54E35` text for secondary decisions (e.g., "Guardar en calendario").

### Cards
- **Event Card:** Elevated white card (`#FFFFFF`) with 20dp rounded corners. Features a 16:9 aspect ratio image header, date badge pinned to the top left in warm terracotta container (`#FBECE8`), event title in `title-md`, venue subtitle in `body-sm` (`#6E6862`), and attendee progress indicators.

### Chips & Badges
- **Category Chips:** Inactive in `#F3EFEA` with `#2D2926` text; active selected state transitions to Sage Green (`#5F8A74`) container with pure white typography.
- **Verified Host Badge:** 8dp pill badge in `#E8F0EC` (Light Sage) with `#5F8A74` text and check icon, indicating validated organizers.

### Progress Bars
- **Event Capacity Bar:** 6dp height, rounded track with `#F3EFEA` background. Active indicator fills with Terracotta (`#D96B4F`), shifting to Sage Green (`#5F8A74`) when spots are confirmed or Honey (`#F4D06F`) when nearly sold out.

### Input Fields
- **Search & Text Inputs:** 14dp rounded corners with `#FAF7F2` filled container. Focus states highlight a 2dp border in `#D96B4F` with floating label transitions and immediate clear-icon affordances.