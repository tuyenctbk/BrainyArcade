# BrainyArcade 🧠🎮

**BrainyArcade** is a premium, logic-focused game hub designed for **Android TV, Mobile, Tablet, and Android Auto**. It features a curated collection of classic and modern logic puzzles with a unified input system supporting D-pad, Touch, and Rotary controllers.

---

## 🎮 Features & Included Games

Explore a collection of classic and modern logic puzzles designed to train memory, spatial reasoning, and deduction skills:

- **Classic Logic**: Lights Out, Mastermind, Reversi, and Peg Solitaire.
- **Math & Numbers**: Sudoku, 2048, and Kakuro.
- **Spatial Reasoning**: Nonograms, Bridges, and Sliding Puzzles.
- **Strategy & Deduction**: Minesweeper, Battleship, and Chess.

### 🕹️ Platform & Input Support
- **Android TV**: Native D-pad navigation, overscan-safe layout, and immersive TV UI.
- **Mobile & Tablet**: Precise touch controls, gestures, and adaptive landscape layout.
- **Android Auto**: Parked-mode UI optimized for high-contrast viewing, with rotary controller and D-pad event mapping.

---

## 🛠️ Architecture Overview

The project is built with Kotlin using a performant **View-based Canvas** model rather than heavy Compose layouts to ensure high framerates on low-end Android TV and automotive hardware.

- **`BaseGameActivity`**: Abstract base class providing common infrastructure (help dialogs, SoundManager, ScoreManager, Firebase Analytics integration).
- **`GameView`**: Custom canvas rendering interface.
- **`UnifiedInputManager`**: Translates platform-specific events (Rotary, D-pad, Touch gestures) into abstract game commands (`UP`, `DOWN`, `LEFT`, `RIGHT`, `SELECT`).
- **`AppDatabase`**: Local Room database for persisting high scores, user stats, and unlocked achievements.

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Koala / Ladybug or newer
- JDK 17+
- Android SDK 34 (Compile & Target)
- Minimum SDK: 23 (Android 6.0)

### Setup & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/tuyenctbk/BrainyArcade.git
   cd BrainyArcade
   ```
2. Open the project in Android Studio.
3. Add a `google-services.json` file inside the `app/` directory (obtain this from your Firebase console if you want analytics/ads to function).
4. Run the app on an Emulator or a connected Android TV/Mobile device:
   ```bash
   ./gradlew installDebug
   ```

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
