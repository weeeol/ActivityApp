# ActivityApp 🚀

A modern, feature-rich Android productivity and lifestyle tracking application built entirely with **Kotlin** and **Jetpack Compose**. ActivityApp combines developer-focused note-taking, project organization, time management, and health tracking into a single, beautifully animated interface.

## ✨ Key Features

### 📝 Developer-Focused Notes
* **Code Mode:** Toggle a specialized editing mode that adds line numbers and a monospace font.
* **Syntax Highlighting:** Live Regex-powered multi-color syntax highlighting for keywords, strings, numbers, and comments.
* **Active Word Tracking:** Tapping any variable or word instantly highlights all matching instances across the entire note.
* **Staggered Grid:** A fluid, Google Keep-style staggered grid layout for easy viewing.

### 📁 Smart Organization
* **Custom Folders:** Group notes into specific project folders.
* **Emoji Support:** Personalize folders with custom emojis featuring a live-updating visual preview during creation.
* **Safe Deletion:** Cascading delete protections ensure orphaned notes are cleaned up when a folder is removed.

### ⏱️ Advanced Timers
* **Quick Select:** Rapidly create timers using predefined duration chips and activity tags.
* **Scheduled Starts:** Set a timer to automatically trigger at a specific time of day using the device clock.
* **Background Tracking:** Timers calculate remaining time intelligently, ensuring accuracy even when navigating away.

### ❤️ Health & Activity Dashboard
* **Activity Rings:** Custom Canvas-drawn, Apple-style animated progress rings for Sleep, Steps, and Water intake.
* **Gamification:** Reaching the daily water goal triggers a custom 60-particle 2D physics explosion effect.
* **Smart Reset:** Automatically detects overnight rollovers to reset daily water tracking.

### 🎨 Premium UI/UX
* **Floating Navigation:** A custom-built, highly optimized drag-to-select navigation pill with a soft-glowing blur mask filter.
* **Material Design 3:** Fully integrated MD3 components with seamless Light/Dark mode system toggling.
* **Adaptive Pop-ups:** Clean, distraction-free `AlertDialog` windows for creating new folders, notes, and timers.

## 🛠️ Tech Stack

* **Language:** [Kotlin](https://kotlinlang.org/)
* **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
* **Design System:** Material Design 3 (MD3)
* **Local Storage:** `SharedPreferences` & `Gson` (JSON Serialization for State Persistence)
* **Architecture:** State-driven Compose architecture with localized Data Managers.

## 📱 Screenshots
 ![Screenshot](img/s1.png)
## 🚀 Getting Started

### Prerequisites
* Android Studio (Latest stable release recommended)
* Minimum SDK: API 24 (Android 7.0) or higher

### Installation
1. Clone the repository:
   ```bash
   git clone [https://github.com/yourusername/ActivityApp.git](https://github.com/yourusername/ActivityApp.git)