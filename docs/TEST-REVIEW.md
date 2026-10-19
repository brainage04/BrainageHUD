# Test review, 2026-10-19

## Frozen rubric

- Keep: production behaviour with an independent expected result, a boundary, a failure case or a real lifecycle transition. Real registry fixtures and a small block-world fixture are allowed; they must not duplicate the algorithm under test.
- Redundant: the same input class and observable outcome are covered by a named retained test. A unit test is not redundant merely because a client scenario also uses the feature.
- Tautological: a copied algorithm, mock echo, registration identity, configuration constant or wiring-only assertion with no independent behavioural outcome.
- Names and assertion summaries identify candidates; inspect flagged bodies before removal. Recording-only scenes are presentation coverage, not passing behavioural tests.

## Unit tests

Baseline: 52 JUnit tests in `fabric/src/test`. Unless marked otherwise, every method below is kept.

- `GetEnchantInfoCommandTest`: `matchesNamesRegardlessOfCase`, `listsEveryPartialMatchInRegistryOrder`, `findsNothingForAnUnknownName`. Name matching, order and missing results.
- `ModCommandsTest`: `fullbrightAcceptsOnlyTheSupportedRange`, `blacklistRejectsUnknownEnchantments`, `wrongWaypointsInputStillRunsTheUsageMessage`, `fullbrightAmountsDropTrailingZeros`. Real Brigadier rejection/fallback behaviour and user-facing number formatting. The waypoint test checks parsing, not the usage message itself; the client chat-feedback scene checks the message.
- `ModCommandsTest.getEnchantInfoReadsAnIdAsAnEnchantmentId`: redundant, removed. It only checked parsed node names. `BrainageHUDClientGameTest.exerciseEnchantCommands` executes the ID command and checks the resulting enchantment ID.
- `ModCommandsTest.getEnchantInfoFallsBackToANameThatIsNotAnId`: redundant, removed. It only checked parsed node names. The retained case-insensitive unit test and `exerciseEnchantCommands`' quoted-name lookup cover the actual result.
- `FishingHudTest`: `poolFiveWideAndTwoDeepIsOpenWater`, `lilyPadsCountAsOpenAbove`, `singleBlockOfWaterIsNotOpenWater`, `waterOneBlockDeepIsNotOpenWater`, `poolNarrowerThanFiveIsNotOpenWater`, `blockTwoAboveTheBobberBlocksOpenWater`, `waterAboveAnAirPocketIsNotOpenWater`. Each changes a distinct open-water constraint; the fixture stores blocks and does not implement the water algorithm.
- `FishingHudTest`: `chancesInOpenWaterWithoutLuckAreTheLootTableWeights`, `chancesOutsideOpenWaterLeaveOutTreasure`, `luckOfTheSeaThreeShiftsWeightFromFishAndJunkToTreasure`, `chancesAlwaysAddUpToOneHundred`. Exact examples plus an independent conservation property over fractional luck values, not a copy of rounding.
- `PositionHudTest`: `labelsEachSectorCentre`, `switchesLabelsOnSectorBoundaries`, `recognisesFarmingToolsByWholeWordsInTheirName`. Compass wrapping, boundary changes and whole-word matching.
- `StatusEffectHudTest`: `formatsDurationsUnderAnHourAsMinutesAndSeconds`, `formatsDurationsFromAnHourWithHours`. Distinct formatting boundary.
- `EnchantInfoHudTest`: `diamondSwordListsPresentLevelsThenMissingEnchantments`, `maceGroupsMutuallyExclusiveEnchantmentsOntoOneLine`, `maxedItemReportsNoMissingEnchantments`, `rendersNothingWhenNothingIsHeldOrTheItemIsNotApplicable`. Real items and a controlled registry exercise applicability, blacklist, conflicts and completion.
- `ClickRateTrackerTest`: `countsEveryClickEvenWhenNothingReadsInBetween`, `clicksExpireOneSecondLater`. Lost-click regression and exact expiry boundary.
- `InventoryCounterTest`: `countsEveryMatchingSlotInSlotOrder`, `listsNoSlotBreakdownForASingleStack`, `groupsByKeyInTheOrderEachIsFirstFound`. Filtering, aggregation, one-stack formatting and encounter order.
- `MathUtilsTest`: `keepsFractionAtWorldBorderCoordinates`, `padsToExactlyTheRequestedPlaces`, `zeroPlacesHasNoTrailingSeparator`, `outOfRangePlacesAreClampedInsteadOfThrowing`. Precision and supported formatting bounds.
- `RollingAverageTest`: `averagesOnlyTheMostRecentSamples`, `shrinksImmediatelyWhenTheWindowIsLowered`, `isZeroWithoutSamples`. Eviction, resizing and clearing; not redundant with tick-rate tests because production ping sampling has a different implementation.
- `StringUtilsTest`: `labelsWriteAcronymsInCapitals`. Config labels, not stored constants.
- `TickRateTrackerTest`: `needsTwoReportsBeforeItHasARate`, `measuresHowFastTheServerActuallyTicks`, `weightsIrregularReportsByElapsedTime`, `forgetsOlderIntervalsWhenTheWindowShrinks`, `restartsWhenGameTimeGoesBackwards`, `resetDiscardsThePreviousReport`. Distinct sampling and lifecycle behaviours.
- `WaypointActionsTest`: `namesUpToTheMaximumLengthAreAccepted`, `namesWithFormattingCodesQuotesOrControlCharactersAreRejected`, `vanillaDimensionsHaveNamesAndOthersKeepTheirId`. Name validation and dimension-name fallback.
- `WaypointStoreTest`: `quickNamesFillTheFirstGapIgnoringCase`. Gap reuse and case-insensitive uniqueness.

After pruning: 50 unit tests. No copied algorithm or mock-echo tests were found.

## Client and server GameTests

One Fabric client GameTest runs against its own dedicated-server fixture. There are no standalone server GameTests: this client-only mod has no server entrypoint. NeoForge server GameTests are disabled for that reason; this does not prove NeoForge client parity.

Retained assertions:

- Editor reload: reopening/reverting the element editor must leave every live element selectable in its registry. This crosses a config replacement boundary, not just initial registration.
- `position-and-counters`: real player position, chunk coordinates, C/E counters and compass output.
- `performance-and-network`: measured server TPS. Other performance/reach/toggle-sprint output is recorded, not asserted.
- `custom-huds`: actual attack input reaches the click counter. Armour and keystroke drawing are recorded, not pixel-asserted.
- `enchant-info`: real held pickaxe produces present levels, a missing header and the Fortune/Silk Touch group.
- `status-effects`: real effects and duration toggles change lines; vanilla icon settings change the top-right offset. Large screenshots preserve icon layout evidence.
- `inventory-trackers`: real split stacks produce projectile totals and food encounter order.
- `enchant-commands`: execute held/explicit item lookup, ID/name/partial/absent enchantment lookup, blacklist add/query/remove and duplicate/absent warnings.
- `inventory-stats`: real key input must print all four armour slots and the off hand.
- `waypoints`: real create/manage key input, World Centre edit/delete restrictions and hide/show buttons.
- `chat-feedback`: command usage, invalid coordinates, list content and chat colours.
- `waypoints-in-world`: recording-only beam, gem, pulse and labels, kept for presentation review.
- `fishing`: real bobber in the fixture pool changes empty HUD to open-water chances.
- `fullbright-0`, `fullbright-1`, `fullbright--1`: screenshots of a sealed unlit room, with a pixel-brightness comparison of actual terrain rather than command acknowledgement.

Removed `assertModKeysHaveTheirOwnCategory`: tautological registration identity plus hard-coded category title. It checked constants and wiring, not whether the keys worked. Create/manage waypoint and Inventory Stats input assertions remain.
