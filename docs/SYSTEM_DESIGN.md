# System Design: BrainyArcade

## 1. High-Level Architecture
The app follows a **Registry-Driven Activity pattern**. The `MainActivity` (Hub) acts as a router, while each game is an isolated `Activity` inheriting from `BaseGameActivity`.

### Component Diagram
```
[MainActivity] <--> [GameRegistry]
      |
      +--> [BaseGameActivity] <-- [Game-Specific Activity]
                 |                       |
          [SoundManager]           [GameView (Canvas)]
          [OnboardingManager]      [Generator/Logic Class]
          [Room Database]
```

## 2. Shared Infrastructure
- **`SoundManager`**: Pool of short SFX (Move, Select, Win, Error). No background music to minimize APK size and battery drain.
- **`ThemeEngine`**: Dynamic accent colors based on game category (Logic: Cyan, Math: Orange, etc.). Standard Dark background (#0A0A0A).
- **`Persistence`**: Room DB stores `GameScore` (Top 10 per game) and `RecentGame` (Play history).
- **`DiscoveryEngine`**: Logic for the Netflix-style Hub. Categorizes games and prioritizes "Continue Playing" and "Daily Puzzles".

## 3. Game Logic Pattern
Each game module should contain:
1. **Activity**: Handles lifecycle and UI layout.
2. **View**: Handles rendering and raw input translation.
3. **Generator**: Pure Kotlin class responsible for producing valid, solvable board states.
4. **Solver/Validator**: Logic to verify win conditions.

## 4. Input Abstraction & Multi-Device Support
The `GameView` interface contract ensures the app runs smoothly across all form factors:
- **Android TV**: Managed via `onKeyDown` for D-pad navigation and the `leanback` library for Hub rows.
- **Tablets/Mobile**: Handled via `onTouchEvent` with high-precision coordinate mapping. 
- **Android Auto**: Integration with rotary controllers via standard focus traversal and key events. 
- **Responsive Canvas**: All games use relative scaling (`min(width, height) / size`) within the Canvas `onDraw`, ensuring the puzzles look perfect on everything from a 5-inch phone to an 85-inch TV.

## 5. State Management
Games must support **Configuration Changes** (rotation) by saving the current board state in `onSaveInstanceState` or using a `ViewModel` for larger state sets.
