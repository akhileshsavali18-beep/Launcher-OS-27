# Launcher OS 27 — Step 19

## Lock Screen Experience

Step 19 adds an iOS 27-inspired **in-app Lock Screen** component.

Included:
- Large live clock
- Date
- Weather location, condition and temperature
- Battery percentage
- Charging/ready status
- Flashlight shortcut callback
- Camera shortcut callback
- Glass-style cards
- Tap-anywhere dismiss behavior
- Compose test tag: `lock_screen_overlay`

The component is intentionally an **in-app overlay**. It does not attempt to replace Android's protected system lock screen.

### Repository note

The current GitHub repository originally contained only `launcher-os-27.zip`, rather than the full Android source tree. The Step 19 component has therefore been added as source code directly to GitHub. Full file-by-file synchronization of the previously generated local ZIP requires the project source tree to be present in GitHub or a binary upload path for the ZIP.
