# Comprehensive Test Plan: BrainyArcade

This document outlines the rigorous testing standards for ensuring quality across all 50 logic games and the global infrastructure.

## 1. Core Logic & Puzzle Generators
Every logic game must undergo the following tests:
- **Solvability Proof**: 
    - Automated Unit Test: Run 1,000 iterations of each generator (e.g., `SudokuGenerator`).
    - Verification: Use a backtracking solver to confirm at least one valid solution exists for every generated board.
- **Randomness & Entropy**:
    - Ensure seeds for games like `Wordle` or `SlidingPuzzle` utilize `System.currentTimeMillis()` or `Random.nextLong()` to prevent session repetition.
    - Verify that no two consecutive sessions produce the same level pattern.
- **Boundary Conditions**:
    - Test games with minimum board sizes (e.g., 3x3 Sudoku) and maximum board sizes (e.g., 15x15 Gomoku).
    - Ensure no "IndexOutOfBounds" or "ArithmeticException" occurs during clue calculation.

## 2. Standardized Persistence (Room DB)
- **Score Logging**: Verify that `onGameWin(score)` triggers a successful insert into the `game_scores` table.
- **Recent Tracking**: Verify that launching a game updates the `recent_games` table and that `MainActivity` correctly re-ranks the Hub rows on `onResume()`.
- **Database Threading**: Ensure all DB operations are performed on `Dispatchers.IO` to prevent UI jank on low-end hardware.

## 3. UI/UX & Cross-Platform Compliance
- **10-Foot UI (Android TV)**:
    - **Focus Trap Test**: Every clickable element (Game Card, Menu Item, Grid Cell) must be reachable via DPAD and have a logical neighbor in 4 directions.
    - **Focus Visibility**: Every focused element must have the high-contrast **Cyan (#00BCD4)** 5dp border and 1.1x scaling animation.
- **Input Parity**:
    - Test all 50 games for simultaneous D-pad and Touch support.
    - Coordinate Translation: Ensure `onTouchEvent` correctly maps screen coordinates to grid indices across different aspect ratios (4:3, 16:9, 21:9).
- **Responsive Layouts**:
    - Verify Hub Row scaling: Focused rows scale to 1.02x.
    - Verify Game Canvas scaling: Games must use `Math.min(width, height)` to remain square and centered.

## 4. Engagement & Platform Logic
- **Onboarding Flow**:
    - Reset app data -> Verify welcome dialog appears.
    - Close app -> Open app -> Verify welcome dialog does NOT appear.
- **Review Trigger**:
    - Automated test: Mock launch count to 5, 15, and 30 to verify the Rating prompt appears.
- **Daily Challenge Seed**:
    - Verify that `DailyChallengeManager.getDailySeed()` returns the same value for all users on the same calendar day.

## 5. Performance & Resource Constraints
- **Stable FPS**: Monitor frame times during complex drawing (e.g., `FlowFree` or `Mahjong`). Target < 16ms per frame.
- **Memory Leaks**: Verify that `SoundManager` release is called and that no circular references exist in the Hub adapters.
- **APK Footprint**: Ensure assets (if any) are compressed. Standard target: < 10MB total size.
