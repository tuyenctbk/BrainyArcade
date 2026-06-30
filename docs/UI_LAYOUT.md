# Premium UI/UX Design Standards: BrainyArcade

This document defines the "Gold Standard" for visual fidelity, animations, and layout consistency across all 50 games.

## 1. Global Aesthetic (Cinematic Dark)
- **Palette**: Pitch Black (#050505) background to maximize contrast. 
- **Accent**: Neon Cyan (#00BCD4) for focus and active states.
- **Branding**: "Netflix Red" (#E50914) for high-impact titles and badges.
- **Typography**: Sans-serif-black for titles, sans-serif-medium for headers, and sans-serif-light for benefits.

## 2. The Discovery Hub (Homescreen)
Inspired by premium streaming services, the Hub uses a "Shelf" metaphor:
- **Hero Banners**: 1.75:1 landscape cards (280x160dp).
- **Glassmorphism**: Headers and overlays use semi-transparent gradients to create depth.
- **Micro-interactions**:
    - **Focus Scale**: Items scale to 1.15x with an `OvershootInterpolator`.
    - **Parallax Icons**: Icons within banners move slightly on focus.
    - **Active Row Elevation**: The focused row gains +8dp translationZ to overlap other rows.

## 3. Standardized Game Screen Architecture
Every game activity follows this "Pro" layout:
- **Header (Dynamic)**:
    - Left: Game Title (36sp) and Category (14sp Cyan).
    - Right: **Personal Best Record** (e.g., "Best: 1240" or "Fastest: 1:45").
- **Game Area**: 
    - 1:1 Aspect Ratio board centered vertically.
    - 4dp Neon border around the entire board.
    - Subtle board shadow (translationZ).
- **Footer**: 
    - Text-based hints with glow effects.
    - Secondary actions (Undo/Hint) styled as circular icons.

## 4. Animation & Visual Feedback
- **Win State**: Confetti particles and a scaling "Victory" badge.
- **Invalid Move**: Board "shakes" (200ms translationX loop).
- **Tile/Piece Movement**: Use `AccelerateDecelerateInterpolator` for all coordinate changes.
- **Score Pop**: When points are earned, the text scales up 1.2x and returns.

## 5. Record Tracking
Games like **2048**, **Sudoku (Time)**, and **Simon Says** will prominently display the "High Record" in the header to encourage replayability.
- **Gold Badge**: Appears next to the score if a new record is set.
