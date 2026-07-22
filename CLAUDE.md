# Common Development Commands

- `gradle --no-daemon testDebugUnitTest` — run the JVM regression tests; this repository does not include a Gradle Wrapper.
- `gradle --no-daemon assembleDebug` — build the installable debug APK.
- `gradle --no-daemon assembleRelease` — build the unsigned, minified release APK.
- `gradle --no-daemon --stacktrace testDebugUnitTest lintDebug assembleDebug assembleRelease` — reproduce the complete GitHub Actions quality gate with JDK 17 and Gradle 8.9.
- `apltk codegraph --help` — inspect available CodeGraph commands before source exploration when `apltk` is provisioned.

# Project Business Goals

- Provide a standalone Wear OS client for browsing Xiaoheihe feeds, posts, images, comments, and account content.
- Keep core reading useful when the upstream detail API requires a browser captcha by falling back to feed data already on the watch.
- Keep APK size, memory peaks, disk writes, and background work suitable for low-end watches.
- Preserve watch-friendly controls, local preferences, crash diagnostics, and optional image-saving behavior.

## Prohibitions

- Do not read `result.link` until the API status is exactly `ok`; rejected or missing payloads must use the current in-memory feed-item fallback (`src/main/java/com/m16a4666/heywear/utils/HeyboxApiStatus.kt:19-39`, `src/main/java/com/m16a4666/heywear/interact/PostDetail.kt:161-211`).
- Do not replace the signer with a placeholder or change it without updating the captured-vector regression test (`src/test/java/com/m16a4666/heywear/utils/HeyboxSignerTest.kt:6-16`).
- Do not add a database, persistent feed/detail cache, automatic network retries, or a second networking stack unless the user explicitly requests it. Standard API calls must go through the bounded `HeyboxHttpClient` (`docs/architecture/android-app.md:15-25`).
- Do not enable Coil's disk cache. Keep its memory budget small and image decoding serialized; explicit user-triggered image saving is not a cache and must retain the 1280-pixel decode limit and failure cleanup (`src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:63-78`, `src/main/java/com/m16a4666/heywear/utils/ImageSaver.kt:22-134`).
- Do not commit generated output, local SDK settings, IDE state, or signing keys; the root `.gitignore:1-11` defines these boundaries.
- Keep network and detail fallback logs limited to the endpoint, HTTP/business status, and link ID; do not log raw responses, query strings, Cookie, nonce, hkey, or login keys, and retain the 128 KiB Debug log cap (`src/main/java/com/m16a4666/heywear/utils/FileLogger.kt:10-64`, `src/test/java/com/m16a4666/heywear/utils/LoggingPrivacyTest.kt:7-39`).
- Keep CI on the single root application module: use unqualified Gradle tasks and `build/outputs/...` artifact paths; do not introduce `:app:*` or `app/build/...` unless a real submodule is added and the architecture documentation is updated (`settings.gradle.kts:1-17`, `.github/workflows/android-build.yml:48-68`).
- Keep repository write permission confined to the release job after a successful `main` push build; the build job must remain content-read-only and publish only its existing APK artifacts plus checksums (`.github/workflows/android-build.yml:18-19,69-143`).
- Do not add keystores, signing passwords, release-signing secrets, or release signing configuration to the standard CI workflow; artifacts and continuous prereleases may contain only the default debug-signed APK and unsigned Release APK (`.github/workflows/android-build.yml:18-19,51-68,69-143`, `docs/principles/build-verification.md:27-33`).

# Project Documentation Index

- `README.md` — user-facing capabilities, API behavior, build prerequisites, and continuous prerelease behavior.
- `CHANGELOG.md` — user-facing pending release notes.
- `docs/README.md` — documentation evidence and maintenance rules.
- `docs/features/content-reading.md` — feed, detail, fallback, image, and comment behavior.
- `docs/features/account-and-settings.md` — login, personal content, settings, and diagnostics.
- `docs/architecture/android-app.md` — single-module Android application, data-flow, and automated-build boundaries.
- `docs/principles/api-error-handling.md` — signed-request, status-gating, fallback, and regression-test conventions.
- `docs/principles/build-verification.md` — CI quality gate, pinned toolchain, and signing-separation conventions.
