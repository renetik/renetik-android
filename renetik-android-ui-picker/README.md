[![Android Build](https://github.com/renetik/renetik-android/actions/workflows/android.yml/badge.svg)](https://github.com/renetik/renetik-android/actions/workflows/android.yml)

## Renetik Android UI Picker

Part of [Renetik Android](https://github.com/renetik/renetik-android/).

Number/value picker wheel built on
[renetik/NumberPicker](https://github.com/renetik/NumberPicker) 2.5.1, a fork of
ShawnLin013/NumberPicker, with Renetik Android property bindings and list
loading helpers.

- **`CSNumberPicker`**: picker wheel exposing selection as a `CSProperty<Int>`.
  `index` changes once for each item the user picks.
- **List loading**: display arbitrary row lists with `load(data, selected)`.
- **Two-way binding**: keep picker position and `CSProperty<Int>` in sync with
  `index(property)`.
- **Picker configuration**: min/max, circulate, text editing, and scroll events.

### Installation

Add JitPack and the UI picker dependency:

```gradle
repositories {
    google()
    mavenCentral()
    maven { url = 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.renetik.renetik-android:renetik-android-ui-picker:2.0.1'
}
```

Use `master-SNAPSHOT` instead of `2.0.1` to test the latest Renetik Android
`master`.

### Compatibility

- **minSdk**: 26
- **compileSdk**: 37
- **Java**: 21
- **Kotlin**: 2.3.21
- **AGP**: 9.2.1

### Quick start

```kotlin
import renetik.android.event.property.CSProperty.Companion.property
import renetik.android.ui.picker.CSNumberPicker
import renetik.android.ui.picker.index
import renetik.android.ui.picker.load

val picker = CSNumberPicker(context)
picker.load(listOf("one", "two", "three"), selected = 1)

val selectedIndex = property(0)
picker.index(selectedIndex)
```

### Core concepts

- **`CSNumberPicker`**: NumberPicker wrapper with a Renetik selection property.
- **`load` extensions**: convert arbitrary rows to displayed picker values.
- **`index(property)`**: two-way binding between picker value and
  `CSProperty<Int>`.

### Dependencies

- **Renetik modules**: [Core](../renetik-android-core),
  [Event](../renetik-android-event), [UI](../renetik-android-ui),
  [Controller](../renetik-android-controller).
- **External runtime**: [renetik/NumberPicker](https://github.com/renetik/NumberPicker)
  `2.5.1`.
- **Test scope**: [Testing](../renetik-android-testing).

### Related Renetik libraries

- [Renetik Android](https://github.com/renetik/renetik-android/)
- [Renetik Android UI](../renetik-android-ui)
- [Renetik Android Framework](../renetik-android-framework)

### Contributing

Issues and PRs are welcome. Please include a clear description and small,
focused changes.

### License

This library is released under the terms of [LICENSE.txt](../LICENSE.txt).

---

If you find this useful, consider supporting the broader Renetik Instruments
effort at [renetik.com](https://www.renetik.com) or get in touch for
[Hire](https://renetik.github.io).
