# Agent instructions

## Required documentation review

**For every change, verify that `README.md` and the documentation in `docs/` remain
accurate and up to date.** Update affected content in the same change, not as a deferred
follow-up. If no documentation changes are needed, explicitly state that the README
and documentation were reviewed and explain why they remain accurate.

- Changes to public APIs, defaults, exceptions, lifecycle, delivery semantics, supported
  platforms, dependencies, build commands, or setup must be reflected wherever documented.
- Keep `docs/api-reference.md`, platform guides, and examples consistent with the code.
- Prefer source-backed examples in `docs/minimal-examples.md`; keep their tests current.
- Update `docs/index.md` and `docs/llms.txt` when adding, moving, or removing relevant guides.
- Distinguish behavior on `main` from behavior available in published Maven versions.
- Write project files, documentation, commit messages, and GitHub content in English.

## Repository map

- `rinku-core/`: shared model, URL parsing/building, dispatch, listeners, and platform adapters.
  The published core artifact is `dev.theolm:rinku`, not `rinku-core`.
- `rinku-compose-ext/`: optional Compose listeners and platform wrappers.
- `samples/common/`: shared sample UI and models.
- `samples/voyager/` and `samples/decompose/`: navigation integrations and platform apps.
  Only Voyager includes a Swift iOS application.
- `build-logic/`: shared Gradle conventions and platform configuration.
- `gradle/libs.versions.toml`: dependency versions.
- `docs/` and `mkdocs.yml`: published documentation and source includes.
- `.github/workflows/`: authoritative CI commands and platform coverage.

## Contracts to preserve

- Rinku receives deep links; route interpretation and navigation belong to the application.
  Do not add a navigation-library dependency to the core.
- External links are filtered before mapping. Internal `Rinku.handleDeepLink(url)` calls
  bypass both steps by default. Enabling an unconfigured filter/mapper throws when reached.
- Shared listeners consume events once; delivery is not broadcast. Use one logical listener.
- Mobile/internal dispatch retains a single pending value. Desktop/Web adapters use a
  shared bounded FIFO with 64 pending platform links, rejecting the newest on overflow.
- Internal dispatch parses/emits asynchronously on `Dispatchers.Main`; listeners execute
  in their collecting coroutine's context. Preserve cancellation and lifecycle ownership.
- Desktop initialization precedes window creation; forwarded processes must not open a UI.
- Web receives initial URLs and `popstate`. History writes, SPA hosting, hash routing,
  and deployment-prefix normalization are not adapter responsibilities.
- URL registration is application/installer-owned. Incoming URLs are untrusted input;
  parsing and filtering do not authenticate senders or replace authorization checks.
- Preserve public signatures, defaults, and supported targets unless the requested change
  explicitly requires changing them. Document and test any intentional contract change.

## Implementation guidelines

- Follow surrounding Kotlin style, naming, source-set layout, and Gradle conventions.
- Put genuinely shared behavior in `commonMain`; keep OS/browser integration platform-specific.
- Add regression tests for behavior changes, including invalid input and lifecycle cleanup.
- Never edit generated outputs under `build/`, `.gradle/`, or `.kotlin/`, or local machine
  settings such as `local.properties`. Do not overwrite unrelated user changes.
- Do not change release versions, publish artifacts, or deploy documentation unless asked.
- Do not run Git commands, commit, push, merge, or create GitHub content without user
  authorization. Permission for a specific Git operation does not authorize unrelated work.

## Validation

Run commands from the repository root. Select checks based on the affected behavior;
documentation-only edits do not require the complete platform matrix. Gradle builds
require a suitable JDK (CI uses Java 17) and may require Android SDK configuration.

### Documentation

```shell
python -m pip install mkdocs-material
mkdocs build --strict
```

Run the strict build for changes to published documentation, source includes, or MkDocs
configuration. Check local links and ensure code examples still match public APIs.
The strict build checks includes and documentation structure; it does not compile code.

### Shared/JVM behavior and desktop samples

```shell
./gradlew :rinku-core:jvmTest :rinku-compose-ext:compileKotlinJvm \
  :samples:voyager:desktopApp:jvmJar :samples:decompose:desktopApp:jvmJar
```

Shared tests include `DocumentationExamplesTest`, which verifies the examples embedded
in the documentation. Desktop CI runs on macOS, Windows, and Linux; one local OS cannot
establish native compatibility on all three.

### Android samples

```shell
./gradlew :samples:voyager:androidApp:assembleDebug :samples:decompose:androidApp:assembleDebug
```

APK compilation requires the Android SDK. Native deep-link delivery additionally needs
device/emulator testing; the existing Android CI runs Maestro flows in `maestro/`.

### Web

Install Chrome/Chromium and set `CHROME_BIN` if it is not discoverable.

```shell
./gradlew --max-workers=1 -Pkotlin.daemon.jvmargs=-Xmx4g \
  :rinku-core:jsBrowserTest :rinku-core:wasmJsBrowserTest \
  :rinku-compose-ext:jsBrowserTest :rinku-compose-ext:wasmJsBrowserTest
```

For sample changes, also build the affected JS/Wasm distributions with the corresponding
`:samples:<voyager|decompose>:webApp:<js|wasmJs>BrowserDistribution` tasks. See
`.github/workflows/web.yaml` for the exact matrix and publication metadata checks.

### iOS (macOS with Xcode)

```shell
./gradlew :rinku-core:iosSimulatorArm64Test :rinku-compose-ext:compileKotlinIosSimulatorArm64
```

These commands do not build the Swift app. Verify Swift integration separately in
`samples/voyager/iosApp/iosApp.xcodeproj`. Native URL scheme registration and packaged
cold/warm delivery require platform-specific manual tests.

## Completion checklist

- Review the README and documentation for every change; update all affected content.
- Run relevant tests/builds, and report the exact checks performed and their results.
- Clearly distinguish executed checks from source inspection or expected CI coverage.
- State any unavailable toolchain, untested platform, or remaining verification step.
- Do not claim a test, build, or native integration passed unless it was actually verified.
