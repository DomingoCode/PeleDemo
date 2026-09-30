# Pele Demo – Android take-home assignment

A small demo payment app (no real payments): enter an amount, optionally choose installments, currency and a signature, and get a receipt.
Built with Kotlin, Jetpack Compose (Material 3), Compose Navigation and ViewModels. `minSdk 24` (Android 7.0).

## Setup

1. Open the `PeleDemo` folder in Android Studio (Ladybug or newer) and let Gradle sync.
   The project uses the Gradle wrapper (Gradle 8.9), AGP 8.7.3, Kotlin 2.0.21 and JDK 17 (Android Studio's bundled JDK works).
2. Run the `app` configuration on a device/emulator with Android 7.0+.
3. Unit tests: `./gradlew test`

No API key or network access is needed for the core flow.

## What is completed

- **Main screen**: settings button, analog clock (ticks every second), amount field, installments switch + 1–12 picker
  (disabled while the switch is off), USD / ILS toggle, signature switch, Submit and Cancel.
- **Settings screen**: three switches (installments / currency / signature) that show or hide the matching option on the main screen; back button in the top bar.
- **Validation**: the amount is required and must be greater than zero. Input is filtered while typing (digits and one separator, max 2 decimals), errors are shown under the field and cleared on the next edit.
- **Cancel** clears the current transaction and restores the initial state of the main screen.
- **Signature screen**: drawing canvas, Submit (disabled until something is drawn), Cancel (back to the main screen with the entered data intact) and a Clear button.
- **Receipt screen**: amount (always), installments / currency / signature only when relevant to the transaction. Finish (and system Back) returns to the main screen and starts a new transaction.
- Rotation-safe (state lives in ViewModels) and edge-to-edge, light/dark theme, accessibility labels for the clock, signature and switch rows.
- Unit tests for the business rules and for the transaction ViewModel.

## What remains

- **Bonus: currency conversion** (Convert button, conversion screen, exchange-rate API, loading/success/error/retry states) – not implemented yet; planned as a separate step after the core flow.
- Bonus: visual polish beyond the Material 3 defaults.
- Screenshots / screen recording.

## Architecture (MVVM, kept simple)

```
domain/   Pure Kotlin: models + PaymentRules (validation, input filtering, building the receipt)
data/     SettingsRepository (in-memory StateFlow), ClockSource (Flow of the current time)
ui/       Composables + ViewModels, one package per screen
          transaction/TransactionViewModel  – Main -> Signature -> Receipt flow
          settings/SettingsViewModel, clock/ClockViewModel
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

## Assumptions

- The written requirements win over the mockups where they differ: "Installments" and "Cancel" (the mockups say "Payments" and "Exit").
- Default currency is ILS. Initially: amount empty, installments off (1), signature off.
- An option hidden in Settings is treated as **off**: its value is reset and it does not appear on the receipt. If the currency option is hidden, the receipt has no currency line.
- The installments line is shown on the receipt whenever installments are on (even for 1).
- Settings are kept in memory for the process lifetime (not persisted). `SettingsRepository` is the single place to swap in DataStore.
- Back on the receipt behaves like Finish. Back on the signature screen behaves like Cancel.
- The decimal separator is normalized to `.`; amounts are limited to 9 integer and 2 decimal digits.
- After the process is killed and recreated, screens that depend on lost in-memory state (Receipt, or Signature submit without a form) return to the main screen instead of crashing.
