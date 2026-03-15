# ActivityApp 🚀

A sleek, multi-functional Android productivity app designed to track daily health goals, organize notes for college assignments, and time focused coding or game dev sessions.

Built entirely with modern Android development practices, featuring a fluid, heavily animated Jetpack Compose UI.

## ✨ Features

* **🍎 Health & Activity Dashboard**
    * Apple Fitness-style animated progress rings for daily step and calorie goals.
    * Smart Water Tracker that automatically resets at midnight.
    * Custom Compose Canvas particle explosion animation triggered when the daily water goal (8 glasses) is reached.
* **📝 Keep-Style Notes & Folders**
    * Create, edit, and organize rich-text notes in a responsive staggered grid layout.
    * Custom project folders with emoji support to group notes logically (e.g., 🎓 College, 🎮 Game Dev, 💻 Python & Git).
    * Persistent local storage ensures data is safely saved between app sessions.
* **⏱️ Smart Timers**
    * Create quick-start countdown timers for specific tasks.
    * **Scheduled Start:** Set a timer to automatically start at an exact local time (e.g., auto-start a study block at 2:00 PM).
    * Interactive UI with quick-fill chips for common activities.
* **🎨 Dynamic UI/UX**
    * Custom glassmorphism floating bottom navigation bar with sliding pill and spring physics animations.
    * Seamless Light/Dark mode toggling tied to local preferences.

## 🛠️ Tech Stack

* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material Design 3)
* **Local Storage:** `SharedPreferences` with Gson serialization
* **Asynchrony:** Kotlin Coroutines (`LaunchedEffect`, `delay`)
* **Animations:** Compose `Animatable`, `tween`, spring physics, and custom `Canvas` drawing.

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone [https://github.com/yourusername/ActivityApp.git](https://github.com/yourusername/ActivityApp.git)