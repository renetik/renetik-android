# Changelog

## Unreleased

- `renetik-android-ui-picker` uses
  [renetik/NumberPicker](https://github.com/renetik/NumberPicker) 2.5.1 in
  place of ShawnLin013 NumberPicker 2.4.13. The wheel now changes its value at
  half an item, where its snap turns, so `CSNumberPicker.index` changes once
  for each item picked. Before, a drag released between two items could report
  the value flipping back and forth, or a change after the scroll went idle.
  The package stays `com.shawnlin.numberpicker`.
- A descending wheel draws its value at the centre with any item count, and a
  tap picks the item drawn under it. Before, a wheel with an even item count
  drew the value minus one at the centre in descending order, and could ignore
  a tap on the item next to the centre.
- `CSNumberPicker.index(property)` keeps the picker's value equal to the
  property in both orders; it no longer shifts a descending picker by one.

## 2.0.1 — 2026-07-06

Publishing and repository layout cleanup after the 2.0 unification release:

- Configured JitPack with an explicit `publishToMavenLocal` install command so
  tagged builds publish all module artifacts with the JitPack-provided group
  and version.
- Moved published modules out of the intermediate `library/` folder into the
  repository root module layout.
- Renamed the project from Renetik Android Framework to Renetik Android,
  including Maven group ID, GitHub/JitPack links, API docs paths, sample labels,
  module documentation, workflow references, and verification scripts.
- Added a dedicated release guide and clarified the difference between local
  verification, Git tags, GitHub Releases, and JitPack artifact verification.

## 2.0 — 2026-07-05

Unification of all Renetik Android libraries into this single
repository, with a package reorganization (the `extensions` packages were
dissolved into the domain packages of the types they extend), removal of dead
and unused deprecated API, a `sample/` app whose main screen is a live module
checklist, tests for every published module, and an API docs site published
from Dokka to GitHub Pages.
