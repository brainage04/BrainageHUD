# BrainageHUD todo

## Loader parity findings (2026-09-29)

From running the release NeoForge jar on a real NeoForge 26.2.0.41-beta server and client. Items marked *both loaders* come from shared code.

- [ ] **Medium, both loaders:** `/fullbright` replies but changes nothing on 26.2. `MixinDimensionType.java:14-19` overrides `DimensionType.ambientLight()`, which 26.2's lightmap (built from `LightmapRenderState`) no longer calls.
- [ ] **Medium, NeoForge:** F1 doesn't hide the HUD elements (HudRendererLib's NeoForge layers; tracked in HudRendererLib/todo.md).
- [ ] Low: Inventory Stats skips armour slots although the README says it lists every occupied slot.
