# Common Development Commands

- `gradle --no-daemon testDebugUnitTest` — run the JVM regression tests; this repository does not include a Gradle Wrapper.
- `gradle --no-daemon assembleDebug` — build the installable debug APK.
- `gradle --no-daemon assembleRelease` — build the unsigned, minified release APK.
- `apltk codegraph --help` — inspect available CodeGraph commands before source exploration when `apltk` is provisioned.

# Project Business Goals

- Provide a standalone Wear OS client for browsing Xiaoheihe feeds, posts, images, comments, and account content.
- Keep core reading useful when the upstream detail API requires a browser captcha by falling back to feed data already on the watch.
- Preserve watch-friendly controls, local preferences, crash diagnostics, and optional image-saving behavior.

## Prohibitions

- Do not read `result.link` until the API status is exactly `ok`; rejected or missing payloads must use the cached-post fallback (`HeyboxApiStatus.kt`, `PostDetail.kt`).
- Do not replace the signer with a placeholder or change it without updating the captured-vector regression test (`HeyboxSignerTest.kt`).
- Do not commit generated output, local SDK settings, IDE state, or signing keys; the root `.gitignore` defines these boundaries.
- Keep detail fallback logs limited to status and link ID; do not add the raw response, Cookie, nonce, or hkey to that path.

# Project Documentation Index

- `README.md` — user-facing capabilities, API behavior, and build prerequisites.
- `docs/README.md` — documentation evidence and maintenance rules.
- `docs/features/content-reading.md` — feed, detail, fallback, image, and comment behavior.
- `docs/features/account-and-settings.md` — login, personal content, settings, and diagnostics.
- `docs/architecture/android-app.md` — single-module Android application boundaries and data flow.
- `docs/principles/api-error-handling.md` — signed-request, status-gating, fallback, and regression-test conventions.
