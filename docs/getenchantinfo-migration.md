# GetEnchantInfo → BrainageHUD migration

This document describes what was moved out of the standalone GetEnchantInfo mod into BrainageHUD, how the ported
behaviour is exposed now, and every known difference that remains. It is written so that GetEnchantInfo is not deleted on
a false report: everything GetEnchantInfo did on the client is present in BrainageHUD, apart from the differences listed
under [Not yet equivalent / deliberate differences](#not-yet-equivalent--deliberate-differences).

## What moved

| GetEnchantInfo | BrainageHUD | Notes |
| --- | --- | --- |
| `common/.../GetEnchantInfo.java` (mod entry, tooltip callback) | `common/.../event/ModTooltipEvents.java` | Only the tooltip highlighting callback was kept; command/config/loader registration moved to the places BrainageHUD already registers those. |
| `common/.../commands/GetEnchantInfoCommand.java` | `common/.../command/GetEnchantInfoCommand.java` | Logic unchanged. |
| `common/.../commands/GetEnchantsCommand.java` | `common/.../command/GetEnchantsCommand.java` | Logic unchanged. |
| `common/.../commands/BlacklistedEnchantsCommand.java` | `common/.../command/BlacklistedEnchantsCommand.java` | Blacklist is now persisted through BrainageHUD's config (`ConfigUtils.saveConfig()`); `enchantmentId(holder)` was folded into `EnchantmentUtils.getEnchantmentId`. |
| `common/.../commands/core/ModCommands.java` | `common/.../command/core/ModCommands.java` | Now a static `registerClientCommands(CommandDispatcher<S>, CommandBuildContext)` that both loaders call; no `ClientPlatform` abstraction and no raw `CommandDispatcher` casts, because both loaders' command sources implement `SharedSuggestionProvider`. |
| `common/.../commands/core/argument/ClientHolderReferenceArgumentType.java` | `common/.../command/core/argument/ClientHolderReferenceArgumentType.java` | The two `getHolder`/`getEnchantment`/`getItem` helpers are generic over the command source type instead of being typed to `SharedSuggestionProvider`. |
| `common/.../util/EnchantmentUtils.java` | `common/.../util/EnchantmentUtils.java` | Verbatim port plus the new helpers used by the HUD element (`getEnchantmentId`, `getMissingEnchantments`, `groupConflictingEnchantments`). |
| `common/.../config/ModConfig.java`, `ModConfigManager.java` | `common/.../config/other/EnchantInfoConfig.java` | Blacklist moved into BrainageHUD's Cloth Config file; Gson config manager dropped in favour of Cloth's serializer. |
| `common/.../platform/ClientPlatform.java` | — | Replaced by direct registration in each loader entrypoint, matching BrainageHUD's existing style. |
| `fabric/.../GetEnchantInfoFabric.java` | `fabric/.../fabric/BrainageHUDFabric.java` | Fabric entrypoint now registers the enchant commands and the item tooltip callback. |
| `neoforge/.../GetEnchantInfoNeoForge.java` | `neoforge/.../neoforge/BrainageHUDNeoForge.java` | NeoForge entrypoint now registers the enchant commands and the item tooltip callback. |

The Fabric-only `fabric/.../command/core/ModCommands.java` that used to register `/fullbright` was removed: the Fabric
entrypoint registers the ported commands from the shared class and `/fullbright` from `FullbrightCommand` itself.

## Commands

All three commands exist with unchanged names, namespaces and output:

- `/getenchantinfo <enchantmentId>` – enchant info for a registry ID (max level, incompatible enchantments, applicable items).
- `/getenchantinfo <enchantmentName>` – lookup by (case-insensitive, partial) name; exact match wins, a single partial
  match is used directly, several matches list names plus IDs, no match reports "No potential matches found!". The
  argument is read as a quoted/plain string exactly as before.
- `/getenchants` – acceptable enchantments for the held item.
- `/getenchants <item>` – acceptable enchantments for an item ID (conflicting pairs are listed separately from
  conflict-free enchantments, blacklisted enchantments are filtered out).
- `/blacklistedenchants query` / `add <enchantmentId>` / `remove <enchantmentId>` – blacklist management, with the same
  feedback messages; `add`/`remove` write through to `config/brainagehud.json`.

Command feedback still goes through `Minecraft.getInstance().player.sendSystemMessage`, i.e. exactly the messages
GetEnchantInfo printed.

## HUD element: Enchant Info

`common/.../hud/custom/EnchantInfoHud.java`, registered from `BrainageHUD.initialize()` next to the other custom
elements, so it gets the same element registration, editor, backdrop, padding, colour, shadow and max-width handling as
every other BrainageHUD element.

It renders nothing at all unless the player is holding an item (main hand) that is `isEnchantable()` **or**
`isEnchanted()`. For such an item it renders, in this order:

1. the item's hover name, as a bold header (`showItemName`),
2. one compact line per enchantment on the item, in enchantment-registry order (stable between frames),
3. a `Missing:` header (only when something is in fact missing, `showMissingHeader`),
4. the enchantments that could still be added to the item, one line each, in registry order.

Rules:

- **Present enchantments** are `Sharpness III` style (`Enchantment.getFullname`). When an enchantment is below its
  maximum level, the maximum is appended: `Sharpness III (max 5)` (`showMaxLevels`). An enchantment shown without that
  suffix is therefore already maxed, which is the point of the original mod.
- **Missing enchantments** are those that are applicable to the item (`Enchantment.canEnchant`), not already present,
  not in the blacklist, and compatible with *every* enchantment already on the item. A missing enchantment is shown at
  its maximum level, e.g. `Mending`, `Fire Aspect II`.
- **Conflicting enchantments** among the missing ones are collapsed onto a single "choose one" line joined with ` / `,
  e.g. `Breach IV / Density V` on a mace. Conflicts are computed as connected groups (vanilla exclusivity is clannish,
  so groups equal the vanilla exclusivity sets), which is the compact form of `/getenchants`'s conflicting-pairs output.
- Blacklisted enchantments are never suggested as missing; the same `enchantInfoConfig.blacklistedEnchantmentIds` list
  drives `/getenchants`, `/blacklistedenchants` and this element.

Observed output from `fabric/src/test/java/.../EnchantInfoHudTest.java` (a controlled enchantment registry with
Sharpness/Smite/Bane exclusivity, Breach/Density exclusivity and a blacklisted Fire Aspect):

```text
diamond sword: sharpness III + unbreaking III, fire aspect blacklisted
  Diamond Sword
  Sharpness III (max 5)
  Unbreaking III
  Missing:
  Mending
  Looting III
mace: no enchantments
  Mace
  Missing:
  Breach IV / Density V
  Wind Burst III
  Mending
diamond sword: sharpness V + unbreaking III + mending + looting III, fire aspect blacklisted
  Diamond Sword
  Sharpness V
  Unbreaking III
  Mending
  Looting III
```

Element options: `showItemName`, `showEnchantments`, `showMaxLevels`, `showMissingEnchantments`, `showMissingHeader`,
plus the standard `coreSettings` (enabled, x, y, anchor, overrides). Default position is middle-left
(`ElementAnchor.LEFT`, 5, 0) and the element is enabled by default, like most BrainageHUD elements.

## Config surface

New sections in `config/brainagehud.json`:

| Key | Default | Meaning |
| --- | --- | --- |
| `enchantInfoConfig.highlightMaxLevelEnchants` | `true` | GetEnchantInfo's tooltip callback: enchantment tooltip lines that are at their maximum level are shown in bold. |
| `enchantInfoConfig.blacklistedEnchantmentIds` | the ten GetEnchantInfo defaults (`binding_curse`, `vanishing_curse`, `blast_protection`, `projectile_protection`, `fire_protection`, `thorns`, `bane_of_arthropods`, `smite`, `knockback`, `frost_walker`) | Enchantments that are never suggested by `/getenchants` or by the HUD element. |
| `enchantInfoHudConfig.*` | see above | Element toggles plus `coreSettings`. |

The generated `common/src/main/generated/assets/brainagehud/lang/en_us.json` was regenerated with
`./gradlew :fabric:runDatagen`, so every new option has a label in the config editor. That regeneration also picked up
`maxWidth` override labels for the other elements that were missing from the previously committed file.

## Not yet equivalent / deliberate differences

- **Config file location.** The blacklist now lives in `config/brainagehud.json`; an existing `config/getenchantinfo.json`
  is **not** read or migrated. A user who changed their GetEnchantInfo blacklist has to re-add those entries (or copy
  the list into the new key) once.
- **Mod ID.** The commands are part of `brainagehud` now; there is no `getenchantinfo` mod to install. Both mods must not
  be installed side by side: they register the same command literals on the same client dispatcher and would both try to
  own `/getenchantinfo`, `/getenchants` and `/blacklistedenchants`.
- **Blacklist storage type.** GetEnchantInfo serialised a `Set<String>` through plain Gson. The port uses a `List<String>`
  so Cloth Config can render and edit it in the config screen; duplicate/absent entries are still prevented by the
  `add`/`remove` command logic, and invalid IDs are still ignored when the in-memory blacklist is synchronised.
- **HUD scope is non-book items.** The element uses the item's `enchantments` component. An enchanted book keeps its
  enchantments in `stored_enchantments`, is neither `isEnchantable()` nor `isEnchanted()`, and therefore shows nothing
  (the tooltip highlighting does handle books, as GetEnchantInfo did).
- **Conflict grouping granularity.** `/getenchants` prints conflicting *pairs*; the HUD groups transitively conflicting
  enchantments into one line. Vanilla exclusivity sets are cliques so the results agree for vanilla data, but a modded
  enchantment that only conflicts transitively (A–B, B–C, A compatible with C) is shown as one `A / B / C` line where the
  command would print two pairs and would not remove `A` or `C` from the acceptable list.
- **Missing-list semantics.** A missing enchantment is filtered only against enchantments already on the item, not
  against the other missing ones, so two mutually exclusive candidates are both listed (as one "choose one" line). That
  is intentional: each candidate is genuinely missing and only one of them can be added.
- **Tooltip highlighting toggle.** GetEnchantInfo always bolded maxed tooltip lines; BrainageHUD exposes that as
  `highlightMaxLevelEnchants` (default on).
- **No standalone-mod feature drop.** GetEnchantInfo had no other user-visible surface: it registered commands, the
  tooltip callback, and the config. All three are ported.

## Verification performed

- `./gradlew build` (JDK 25) – `:common:build`, `:fabric:build`, `:neoforge:build` and `:fabric:test` all succeed; both
  loader jars contain the ported classes and the regenerated language file.
- `./gradlew :fabric:test` – `EnchantInfoHudTest` runs four registry-backed cases (present + missing + blacklist +
  conflict, conflict grouping, fully-enchanted item, non-applicable items) and passed; the rendered lines above are its
  captured output.
- `./gradlew :fabric:runClientGameTest` – the shipped Fabric client GameTest now hands the fixture player an enchanted
  diamond pickaxe (Efficiency III, Unbreaking III, Mending) and asserts the element's lines in a real client before
  capturing `enchant-info-hud`. The run passed and the screenshot shows the element rendering under the position HUD:

  ```text
  Diamond Pickaxe
  Efficiency III (max 5)
  Mending
  Unbreaking III
  Missing:
  Fortune III / Silk Touch
  ```

  Screenshot: `fabric/build/run/clientGameTest/screenshots/0000_enchant-info-hud.png` (a copy was kept outside the repo
  at `/tmp/brainagehud-enchant-info-hud.png`, because the GameTest run directory is cleared on the next run).
- `./gradlew :fabric:runDatagen` – regenerated the language file.
- `./gradlew :common:spotlessJavaCheck :fabric:spotlessJavaCheck :neoforge:spotlessJavaCheck -PstrictQuality=true` – every
  file this migration adds or rewrites is clean. The check still fails on the repository's pre-existing unformatted
  sources (`common/.../hud/**`, `common/.../mixin/**`, `common/.../util/**`, `fabric/.../FullbrightCommand`,
  `DataGenerator`, `EnglishLangProvider`, `ModMenuIntegration`) and on the GameTest harness file, which was edited in its
  existing style instead of being reformatted wholesale.

The client GameTest run above is the real-client check for this migration: it boots a full client against the fixture
dedicated server, renders the element, asserts its lines and captures the screenshot listed there. The repository's
recording workflow (`./gradlew recordClientGameTest`) was not run, so no narrated video exists for the new element yet;
the GameTest step added for it appears in the recording the next time that task runs.
