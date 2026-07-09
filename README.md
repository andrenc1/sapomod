<div align="center">
  <h1>🐸 Sapo Mod </h1>
  <p><i>A utility mod for Fabric focused on quality of life, chat alerts, and now featuring a powerful <b>Automatic Solver</b> for Akari (Light Up) puzzles!</i></p>
  
  ![Fabric](https://img.shields.io/badge/Fabric-1.21-dbb87d?style=for-the-badge&logo=fabric)
  ![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
</div>

<br/>

## ✨ Features

- **🔔 Custom HUD Alerts**: Configure keyword triggers that display a vibrant and customizable alert directly on your screen when typed in the chat!
- **🧩 Automatic Akari Puzzle Solver**: A smart system (using Backtracking) that scans a 10x10 board directly in the game world, detects walls/numbers, and calculates the exact solution.
- **✨ Visual Feedback with Particles**: The puzzle solution is drawn in the world using green particles (`Happy Villager`) so you know exactly where to click or place the light.
- **⚙️ Fully Configurable**: Integrated Mod Menu and a graphical interface (HUD Editor) allowing you to drag and drop the alert anywhere on the screen.
- **🔍 Inspection Tool**: Built-in commands to inspect entities, hitboxes, and blocks directly in-game, with detailed output in the console.

---

## 🚀 How to Use the Commands

The base for all mod commands is `/sapo`. Here is the list of what you can do:

### 🧩 Akari Puzzle (Light Up)
- `/sapo resolver` - Scans the room at the configured coordinate, processes the 10x10 puzzle using a backtracking algorithm, and displays the solution through green particles on the floor.
- `/sapo limpar` - Removes all active puzzle particles.

### 🔔 HUD Alerts
- `/sapo gatilho <text>` - Sets the word that, when appearing in chat, will trigger the alert (e.g., "alive", "dead", "[Server]").
- `/sapo alerta <text>` - Sets the message that will appear large on the screen.
- `/sapo cor <hex>` - Changes the alert color (e.g., `FF0000` for red).
- `/sapo tempo <seconds>` - How long the text will stay on the screen.
- `/sapo testar` - Tests the current HUD configurations.
- `/sapo simular <text>` - Simulates a message arriving in chat to test your triggers.
- `/sapo editarHUD` - Opens the interactive screen to reposition the alert with the mouse.

### ⚙️ Utilities
- `/sapo` - Opens the Mod Menu Settings screen.
- `/sapo help` - Displays the full list of commands in chat.
- `/sapo inspecionar` - Look at a block or entity and use this command to generate a complete Debug Log in the console (useful for finding Names, Tags, and ItemDisplays).
- `/sapo debug` - Toggles developer mode on/off.

---

## 🛠️ How Does the Solver Work?

**SapoPuzzle** is the brain behind the magic:
1. **Scanning**: The mod checks a 10x10 grid starting from a fixed corner in the world (`X=-346, Y=44, Z=179`). It reads `BlockStates` to find black walls and uses an expanded `Hitbox` (`AABB`) to read the texts (`ItemDisplay`) of the floating numeric hints (0 to 4).
2. **Backtracking (Resolution)**: A virtual matrix is built. The algorithm tests placing "light bulbs" in empty spaces. If the rules of the *Akari* game are violated (e.g., two lights crossing the same hallway or a wall having the wrong amount of lights), it backtracks a step and tries another path until it finds the validated answer.
3. **Rendering**: The correct positions are sent to `ClientTickEvents`, which magically renders the particles for the player.

> *Tip: When using `/sapo resolver`, open your client`s console (Output). The mod prints the visual representation of the 10x10 matrix it read and the time it took to find the solution in milliseconds!*

---

## 💻 Installation / Dev Environment

This project uses the **Fabric** template and requires **Mojang Mappings (Mojmap)**.

To compile the mod locally:
```bash
# Clone the repository and run Gradle:
./gradlew build
```
The final `.jar` will be in the `build/libs/` folder.

---

<div align="center">
  Made with 💚 to automate the most insane moments on the server!
</div>
