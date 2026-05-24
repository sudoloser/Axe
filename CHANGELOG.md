# 5/23/2026 [v1.2.0]
### Added
- External server gateway for 24/7 reliability
### Fixed
- Fields like "Activity platform" being a mix of input and dropdown. (Switched to dropdown only)
- Images not filling properly in rpc preview. 
- Gifs not animating

# 5/20/2026 [v1.1.1]
### Added
- Custom RPC Memory: Configuration is now automatically saved and restored when reopening the app.
### Fixed
- RPC State Conflict: Console RPC and Custom RPC toggles now operate independently.
- Custom RPC Toggle Visibility: The toggle remains visible on the home screen even after clearing fields.
### Removed
- Custom Button Shapes: Removed all references and UI settings for button shapes.
### Modified
- UI Consistency: Applied a consistent `RoundedCornerShape(24.dp)` to all home screen feature buttons.

# 5/13/2026 [v1.1.0]
### Added
- Media RPC: Add "Show artist as title" option (mutually exclusive with "Show song as title"). ([Issue #495](https://github.com/dead8309/Kizzy/issues/495))
- Quick Settings: Added "Floating Overlay" to Axe Quickie tile for fast access.
- Meta Quest: Fixed `java.lang.IllegalStateException: Size is unspecified` crash by enforcing bounded constraints and explicit image scaling. ([Issue #471](https://github.com/dead8309/Kizzy/issues/471))
- Login: Added a note about passkey login not working.
- Experimental RPC: Added ability to edit album title and a toggle to disable it. ([Issue #427](https://github.com/dead8309/Kizzy/issues/427))

# 5/8/2026 [v1.0.0]
### Added
- Floating Axe Overlay: Quick access to RPC controls (toggle service, switch presets, configure) while in other apps.
- Per-App Custom RPC Configuration: Assign specific custom configs to individual apps.
- Shizuku-Based App Detection: Faster and more accurate foreground detection (optional).
- New release workflow for automated building, signing, and uploading artifacts.
- Preview button for per-app custom RPC configurations.
- Long-press to edit assigned custom RPC configs directly from the app list.
- Rename all occurences of Kizzy to Axe
- Experimental button shapes for the main screen
- Developer credits in credits page
- Modern rounded text input fields in custom RPC
- Experimental note for button shapes in display settings
### Fixed
- Timezone offset issue in custom rpc timestamp picker. ([rootdevss pull request on Kizzy](https://github.com/dead8309/Kizzy/pull/480/changes/f1106546e3dea3135929e4e372488654cb9dc927))
- Corrected Turkish translations for clarity and consistency. ([kyoyacchi's pull request on Kizzy](https://github.com/dead8309/Kizzy/pull/491))
### Removed
- Discord from the sidebar & the Discord and YouTube chips.
