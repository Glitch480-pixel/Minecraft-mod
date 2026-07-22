# BubbleGumFloat

A Minecraft Forge 1.20.1 mod that adds **Bubble Gum** (`bubblegumfloat:bubble_gum`): chew
it to blow a big bubble that lifts you gently into the sky for 30 seconds, then pops -
dropping you back down without taking fall damage.

## Requirements

- JDK 17 (Forge 1.20.1 / ForgeGradle 6 requirement)
- Internet access on first build, to download Forge (`maven.minecraftforge.net`) and the
  Gradle 8.1.1 distribution (`services.gradle.org`) — the sandbox this mod was authored in
  blocks both hosts, so the project could not be test-built there; build it on a normal
  dev machine.

## Build & run

```bash
# make the wrapper script executable once (already committed as executable, but just in case)
chmod +x gradlew

# compile + package the mod jar (output in build/libs/)
./gradlew build

# launch a dev client with the mod loaded
./gradlew runClient

# launch a dev dedicated server with the mod loaded
./gradlew runServer
```

The built jar will be at `build/libs/bubblegumfloat-1.0.0.jar`.

If `./gradlew build` fails to resolve `net.minecraftforge:forge:1.20.1-47.3.0`, that exact
Forge build may have been retired — open
https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html, pick any
current 1.20.1 build number, and update `forge_version` in `gradle.properties` to match.

## What's implemented

- `item/BubbleGumItem.java` — right-click handler: sound, applies the Floating effect,
  shows the "You blow a big bubble..." action-bar message, consumes the item (skipped in
  creative), and starts an 8s cooldown.
- `effect/FloatingMobEffect.java` — custom `MobEffect` ("Floating") that eases the player
  upward at ~0.06 blocks/tick for 30 seconds (about a 35-block climb — same target height
  as the original 10s version, just spread over 3x longer), easing back down to 0 over the
  last second so the ascent stops smoothly instead of snapping. See the design-choice
  comment at the top of that file for why a `MobEffect` was used instead of a raw
  tick/capability velocity hack.
- `event/ModEvents.java` — detects the moment the Floating effect expires (the "pop"),
  plays a pop sound + particle burst, and grants the player immunity from the very next
  `LivingFallEvent` so the landing after a bubble pop deals no fall damage.
- `item/ModCreativeTabs.java` — a dedicated "BubbleGumFloat" creative tab holding the item.

## Assets

- `assets/bubblegumfloat/textures/item/bubble_gum.png` — a 16x16 placeholder texture: a
  generic wrapped-candy silhouette (pink gum piece, light-blue-speckled white wrapper
  twists), generated programmatically since no art tool was available. It's an original,
  generic design, not a copy of any real candy brand's artwork/logo. Swap this file for
  real artwork any time — no code changes needed, just keep it 16x16 (or any square
  power-of-two size) RGBA PNG at that path.
- `assets/bubblegumfloat/models/item/bubble_gum.json` — standard `item/generated` model
  pointing at the texture above.
- `assets/bubblegumfloat/lang/en_us.json` — display names ("Bubble Gum", the creative tab
  name, and the "Floating" effect name).

No mixins are used or needed — everything is implemented with plain Forge APIs
(`DeferredRegister`, a custom `MobEffect`, and `@SubscribeEvent` listeners on the Forge
event bus), which is the more stable/idiomatic approach for this kind of feature in
Forge 1.20.1.

## Tuning knobs

| What | Where | Default |
|---|---|---|
| Float duration | `BubbleGumItem.FLOAT_DURATION_TICKS` | 600 (30s) |
| Item cooldown | `BubbleGumItem.COOLDOWN_TICKS` | 160 (8s) |
| Ascent speed | `FloatingMobEffect.ASCEND_SPEED` | 0.06 blocks/tick |
| Ease-out window | `FloatingMobEffect.EASE_OUT_TICKS` | 20 (1s) |
