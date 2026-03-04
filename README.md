# ADialogAPI

![Modrinth Downloads](https://img.shields.io/modrinth/dt/adialogapi?style=for-the-badge&logo=modrinth&color=blue)
![License](https://img.shields.io/github/license/isArkaDarkTime/ADialogAPI?style=for-the-badge&color=blue)
![Java Version](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&)
![Platform](https://img.shields.io/badge/Platform-Paper%20-green?style=for-the-badge&)

**ADialogAPI** is a modern, high-performance interactive dialog system for Minecraft servers. It allows you to create immersive RPG-like conversations, complex menus, and user-input forms using simple YAML or a powerful Java API.

---

## ✨ Features

- **MiniMessage Integration:** Full support for modern formatting (RGB, gradients, hover/click events).
- **Action System:** Trigger commands, sounds, titles, potion effects, and inventory changes directly from dialog buttons.
- **Dynamic Inputs:** Capture player input through the dialog interface and use it in commands or messages.
- **Cooldown Support:** Prevent dialog spam with per-player cooldowns defined in config.
- **Developer API:** Custom events for opening, clicking, and closing dialogs to build complex logic.

---

## 🚀 Quick Start (For Admins)

### 1. Installation
1. Download the latest `.jar` from [Releases](https://github.com/isArkaDarkTime/ADialogAPI/releases).
2. Place it in your server's `plugins` folder.
3. Restart the server.

### 2. Configuration Example
Dialogs are stored in the `dialogs/` folder.
<details>
  <summary>Basic example of dialog yaml</summary>

  ```yaml
# ─────────────────────────────────────────────
# Dialog: notice example
# A simple informational window with a single OK button.
# Recommended for announcements, tips, or welcome messages.
# ─────────────────────────────────────────────

# Displayed at the top of the dialog window.
title: "<gold><bold>✦ Welcome to the Server!"

# true  - Player can press ESC to close the dialog.
# false - Player must click the OK button to close.
can_close_with_escape: false

# Cooldown in seconds before the same player can open this dialog again.
# 0 = no cooldown (default).
cooldown: 0

# Dialog type:
# - notice: single OK button
# - confirmation: Yes / No buttons
type: notice

# Optional: register a command that opens this dialog.
# Leave empty ("") to skip.
open_command: "welcome"

# Optional: permission required to use open_command.
# Leave empty ("") to allow all players.
open_permission: ""

body:
  # Simple text line in the dialog.
  - type: text
    content: "<gray>Hello, traveler! We're glad to see you here."

  # Empty text used as spacer.
  - type: text
    content: ""

  - type: text
    content: "<yellow>Explore, build, and have fun!"
```
</details>

---

## 🛠 Developer API (For Programmers) (In Future)

### Events

Listen to custom events to integrate ADialogAPI with your systems:

* `DialogOpenEvent`: Fired when a dialog is about to open (Cancellable).
* `DialogButtonClickEvent`: Captures button clicks and any associated user inputs.
* `DialogCloseEvent`: Fired when a dialog is closed (supports reasons like ESCAPE or PLUGIN).

### Accessing the Manager

You can open dialogs via the `DialogManager`:

```java
ADialogAPI().getApi().showDialog(player, "dialog_id");
```

---

## 📜 Commands & Permissions

The main command is `/adialogapi` (aliases: `/adapi`).

| Command                          | Description                          | Permission                  |
|----------------------------------|--------------------------------------|-----------------------------|
| `/adialogapi help`               | View all available commands          | `adialogapi.command.help`   |
| `/adialogapi show <id> [player]` | Open a dialog for yourself or others | `adialogapi.command.show`   |
| `/adialogapi list`               | List all loaded dialogs              | `adialogapi.command.list`   |
| `/adialogapi reload [id]`        | Reload all or a specific dialog      | `adialogapi.command.reload` |
| `/adialogapi create <id>`        | Create a new dialog template         | `adialogapi.command.create` |
| `/adialogapi delete <id>`        | Remove an existing dialog            | `adialogapi.command.delete` |
| `/adialogapi info <id>`          | View detailed info about a dialog    | `adialogapi.command.info`   |

---

## 🤝 Support

- **Issues:** Report bugs or request features via [GitHub Issues](https://github.com/isArkaDarkTime/ADialogAPI/issues).
- **Discussions:** Ask questions and share your configs in the [Discussions](https://github.com/isArkaDarkTime/ADialogAPI/discussions) tab.
- **Discord server:** My official discord server for support, bug-reports and fun - [ArkaDarkTime Hub](https://dsc.gg/arkadarktimehub).

---

## ⚖️ License

Distributed under the **GNU GPL v3 License**. See [LICENSE](LICENSE) for details.
