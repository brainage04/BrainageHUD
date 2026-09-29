# BrainageHUD todo

## Loader parity findings (2026-09-29)

From running the release NeoForge jar on a real NeoForge 26.2.0.41-beta server and client. Items marked *both loaders* come from shared code.

- [x] **Medium, both loaders:** `/fullbright` replies but changes nothing on 26.2. `MixinDimensionType.java:14-19` overrides `DimensionType.ambientLight()`, which 26.2's lightmap (built from `LightmapRenderState`) no longer calls. Fixed: `MixinLightmapRenderStateExtractor` adjusts the extracted `LightmapRenderState` (ambient colour towards white, or darkness scale for negative amounts); verified on NeoForge and Fabric.
- [x] **Medium, NeoForge:** F1 doesn't hide the HUD elements (HudRendererLib's NeoForge layers; tracked in HudRendererLib/todo.md). Fixed by HudRendererLib 1.0.12 (`hudrendererlib_version` bumped); verified F1 hides BrainageHUD's elements on NeoForge.
- [ ] Low: Inventory Stats skips armour slots although the README says it lists every occupied slot.
