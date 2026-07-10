# 7/10/2026 [v1.3.0]
### Added
- Bug Report Form: In-app bottom sheet to submit bug reports with screenshots via Discord webhook
- Image Upload: Images are now uploaded through Imgur CDN using OkHttp engine for reliable TLS support
### Fixed
- App list flickering: Fixed constant placeholder flashing in Floating Overlay and App Detection app lists by caching app icon references
- Inconsistent top bar styling: Custom RPC, Media RPC, Experimental RPC, App Detection, Floating Overlay, and Credits screens now use consistent `surface` color matching the Home screen
- Changelog dropdown arrow icon for proper theming support
- Smooth changelog height animation: Loading-to-content transition now animates smoothly via `animateContentSize`
### Removed
- Shizuku: Removed all Shizuku-based app detection code, including the detection strategy, permissions, provider, settings UI, preferences key, and dependencies

# 6/27/2026 [v1.2.2]
### Fixed
- Hyperlinks in changelog rendering as garbled HTML instead of clickable links
### Changed
- Replaced WebView-based changelog rendering with native Compose text for improved performance
### Added
- Version selector dropdown to browse changelog history across releases
- Search button to Overlay Settings

# 6/26/2026 [v1.2.1]
### Fixed
- JSON parsing memory issue [dead8309, 67503ca](https://github.com/dead8309/Kizzy/commit/67503ca)
- Cover art cache key now includes title for proper deduplication [dead8309, 9668e05](https://github.com/dead8309/Kizzy/commit/9668e05)
- Placeholder regex in template processor [dead8309, faf7c85](https://github.com/dead8309/Kizzy/commit/faf7c85)
- RPC activity name not being set correctly
### Added
- Changelog button next to check for updates
### Credits
- [dead8309](https://github.com/dead8309)
> Original commits from upstream
- [OpenCode](https://github.com/opencode-ai/opencode)
> For helping to merge these commits

# 6/13/2026 [v1.2.0]
### Added
- External server gateway for 24/7 reliability
### Fixed
- Fields like "Activity platform" being a mix of input and dropdown. (Switched to dropdown only)
- Images not filling properly in rpc preview. 
- Gifs not animating in rpc preview

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
