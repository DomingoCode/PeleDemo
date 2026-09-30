# Pele Demo – Android take-home assignment

A small demo payment app (no real payments): enter an amount, optionally choose installments, currency and a signature, and get a receipt.
Built with Kotlin, Jetpack Compose (Material 3), Compose Navigation and ViewModels. `minSdk 24` (Android 7.0).
Visuals follow the "Receipt" redesign handoff (brand palette, Manrope/Fraunces typography, custom launcher icon) — see
[Visual design](#visual-design-receipt-redesign) below.

## Setup

1. Open the `PeleDemo` folder in Android Studio (Ladybug or newer) and let Gradle sync.
   The project uses the Gradle wrapper (Gradle 8.9), AGP 8.7.3, Kotlin 2.0.21 and JDK 17 (Android Studio's bundled JDK works).
2. Run the `app` configuration on a device/emulator with Android 7.0+.
3. Unit tests: `./gradlew test`

No API key is needed. The Convert screen (bonus) calls the free, key-less
[open.er-api.com](https://www.exchangerate-api.com/docs/free) exchange-rate API, so the device/emulator needs internet
access for that screen only; the core flow works fully offline.

## What is completed

- **Main screen**: settings button, analog clock (ticks every second), amount field (large Fraunces digits, currency symbol
  as a leading icon), installments switch + 1–12 picker (disabled while the switch is off, with a "N × {symbol}{amount} per
  month" line once it's on), a USD / ILS segmented toggle right under the installments row, signature switch, Submit and Cancel.
- **Settings screen**: three switches (installments / currency / signature) that show or hide the matching option on the main screen; back button in the top bar.
- **Validation**: the amount is required and must be greater than zero. Input is filtered while typing (digits and one separator, max 2 decimals), errors are shown under the field and cleared on the next edit.
- **Cancel** clears the current transaction and restores the initial state of the main screen.
- **Signature screen**: dashed-border pad, Submit (disabled until something is drawn), Cancel (back to the main screen with the entered data intact) and a Clear button.
- **Receipt screen**: amount (always), installments / currency / signature only when relevant to the transaction, plus a
  receipt number and date. Finish (and system Back) returns to the main screen and starts a new transaction.
- Rotation-safe (state lives in ViewModels) and edge-to-edge, accessibility labels for the clock, signature and switch rows.
- **Bonus: currency conversion.** A Convert button on the Receipt screen opens a conversion screen that fetches live rates
  for the transaction's base currency (or ILS if the currency option is hidden) and shows a fixed set of well-known target
  currencies (USD, EUR, GBP, JPY, INR, ILS, CAD, AUD, CHF, CNY, minus the base — always ≥5 left), each with its rate and the
  converted amount. Loading spinner (with skeleton rows) while fetching, a retryable error state on failure (with a "Back to
  receipt" escape hatch), and Back (arrow, button or system Back) always returns to the same Receipt screen without losing
  the transaction or re-fetching already-loaded rates on rotation.
- **Bonus: visual design.** The "Receipt" redesign — brand palette, Manrope/Fraunces typography, adaptive launcher icon,
  pill buttons, and a custom torn-paper receipt card. Details in [Visual design](#visual-design-receipt-redesign).
- Unit tests for the business rules, the transaction ViewModel, the conversion rules/ViewModel and the date formatting.

## What remains

- Screenshots / screen recording.

## Architecture (MVVM, kept simple)

```
domain/   Pure Kotlin: models + PaymentRules (validation, input filtering, building the receipt)
          + ConversionRules (turns raw rates into per-currency amounts, no network/Compose)
          + formatReceiptDate/formatTime (pure timestamp formatting)
data/     SettingsRepository (in-memory StateFlow), ClockSource (Flow of the current time)
          ExchangeRateRepository + remote/ExchangeRateApi (Retrofit) – fetches live exchange rates
ui/       Composables + ViewModels, one package per screen
          transaction/TransactionViewModel  – Main -> Signature -> Receipt flow
          settings/SettingsViewModel, clock/ClockViewModel
          conversion/ConversionViewModel    – Loading/Success/Error state for the Convert screen
          theme/Color.kt, Type.kt, Theme.kt – brand palette, Manrope/Fraunces, the single light M3 scheme
          components/ReceiptShape.kt        – the receipt card's rounded-top/torn-bottom Shape
          navigation/PeleNavHost            – the only place that knows about NavController
```

- **UI is state-driven and stateless.** Each screen has a `*Route` composable that collects `StateFlow`s with
  `collectAsStateWithLifecycle()` and passes plain state + lambdas to a stateless `*Screen` composable (state hoisting, previews and testing are easy).
  The only state kept inside Composables is purely visual (e.g. whether the installments dropdown is open).
- **No business logic in Composables.** Validation, input filtering, "hidden option = off" and the receipt content are in `PaymentRules`
  (pure functions, unit tested). ViewModels only orchestrate.
- **Manual DI** (`AppContainer` + a `ViewModelProvider.Factory`) instead of Hilt: two tiny dependencies do not justify a framework. No separate use-case layer for the same reason.

### Two design decisions

1. **One Activity-scoped `TransactionViewModel` for the whole transaction flow.** Main, Signature and Receipt share the same form state,
   so Cancel/Back on the signature screen keeps the data, Finish/Cancel reset everything in one place, and the state survives rotation.
   Navigation is triggered by one-off events (`Channel`) from the ViewModel and executed by the NavHost, so the ViewModel never touches navigation APIs
   and double taps are ignored by checking the current destination.
2. **Signature stored as normalized strokes (0..1), not as a Bitmap.** It survives rotation, scales to any size, and the receipt simply re-draws the
   same strokes. The pad and the receipt preview use the same 2:1 aspect ratio so it looks identical.
3. **Conversion screen has its own short-lived ViewModel, constructed inline with the amount/base currency it needs**
   (instead of going through the shared `AppContainer` factory like the other ViewModels). It only ever needs one network
   call for its own lifetime, so a `SavedStateHandle`/route-args setup would add ceremony without benefit; `ExchangeRateRepository`
   is still shared from `AppContainer` and the conversion math lives in `ConversionRules`, so both stay unit-testable.

## Visual design ("Receipt" redesign)

A design handoff (`DESIGN.md` + mockups + brand assets) specified a visual-only refresh: same flow and functionality,
new look. It was applied in six steps — icon, theme, then one pass per screen (Main, Signature, Receipt, Conversion) —
building and testing after each one.

- **Palette & type.** `colors_brand.xml` → `ui/theme/Color.kt` → one light `ColorScheme` (`ui/theme/Theme.kt`); dynamic
  color is never used, and dark theme is intentionally out of scope, so the same light brand scheme applies in both
  system modes. Manrope (400/600/700) is the default UI font; Fraunces (600, `ui/theme/Type.kt`) is applied only at
  specific call sites — the main-screen amount, the receipt amount, the signature screen title — never as a Typography
  default, so it can't leak into the rest of the UI by accident.
- **Icon.** The old single-layer `drawable/ic_launcher.xml` was removed and replaced with an adaptive icon
  (`mipmap-anydpi-v26`, with a monochrome layer for Android 13+ themed icons) plus legacy PNGs for pre-Android-8 buckets.
- **Custom drawing.** Compose has no built-in "torn paper" shape or dashed border, so the receipt card uses a custom
  `Shape` (`ui/components/ReceiptShape.kt`: rounded top corners, zig-zag bottom) and the signature pad uses a small
  `Modifier.dashedBorder` extension.
- **Deliberately skipped** (each one optional per the brief, and not requested): the custom numeric keypad on the main
  screen (the existing validated `OutlinedTextField` does the job), the QR code on the receipt (needs a barcode
  library), and the splash screen.
- **Deviations from the mockups, in favor of the assignment ("assignment wins" per the brief):**
  - The signature screen keeps **Submit** and **Cancel** — not the mockup's "Skip" / "Pay (demo)". "Skip" would let the
    user bypass a signature the app has already determined is required for this transaction.
  - The main screen keeps the required **Settings** button and the analog **clock** even though the "amount" mockup
    doesn't show them.
  - The receipt's number and date aren't required by the assignment; they were added only to match the design's
    "RECEIPT #… / Date" rows. The number is an in-memory per-process counter (`TransactionViewModel`), not persisted.

## Assumptions

- The written requirements win over the mockups where they differ: "Installments" and "Cancel" (the mockups say "Payments" and "Exit").
- Default currency is ILS. Initially: amount empty, installments off (1), signature off.
- An option hidden in Settings is treated as **off**: its value is reset and it does not appear on the receipt. If the currency option is hidden, the receipt has no currency line.
- The installments line is shown on the receipt whenever installments are on (even for 1).
- Settings are kept in memory for the process lifetime (not persisted). `SettingsRepository` is the single place to swap in DataStore.
- Back on the receipt behaves like Finish. Back on the signature screen behaves like Cancel.
- The decimal separator is normalized to `.`; amounts are limited to 9 integer and 2 decimal digits.
- After the process is killed and recreated, screens that depend on lost in-memory state (Receipt, or Signature submit without a form) return to the main screen instead of crashing.
- Conversion targets are a fixed, hardcoded list of common currency codes (not fetched from the API), filtered to drop the base currency; the example API supports all of them, so this always yields 5–9 target currencies.
