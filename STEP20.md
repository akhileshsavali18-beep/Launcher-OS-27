# Step 20 — Lock Screen Integration Layer

Added:
- `LockScreenController.kt`
- Explicit show/dismiss/toggle state
- Reusable host integration contract for `LockScreenOverlay`

The controller is intentionally separate from the overlay so the main launcher can decide when to show it.

## Current repository state

The repository still does not contain the original Android source tree; it contains the original `launcher-os-27.zip` plus the Step 19/20 source additions. Because the existing `MainActivity.kt` / `HomeScreen.kt` are not present as UTF-8 source files in GitHub, this step does not pretend to wire them blindly.

Once those source files are present in GitHub, the host integration is:
1. Create `remember { LockScreenController() }`.
2. Add the lock-screen trigger to the launcher UI.
3. Render `LockScreenOverlay` when `controller.visible`.
4. Connect flashlight/camera callbacks to the existing launcher actions.
5. Pass the existing weather and battery state.
