package com.bubblegumfloat.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Custom "Floating" MobEffect.
 *
 * DESIGN CHOICE: this is implemented as a proper MobEffect (like Levitation/Slow Falling)
 * rather than a raw tick-event/capability velocity hack. Reasons:
 *  - It plugs into the vanilla effect system for free: HUD icon + swirl particles,
 *    automatic duration countdown, /effect command support, milk-bucket removal,
 *    and other mods that inspect entity effects (e.g. anti-fall-damage mods) all
 *    just work without any extra bookkeeping on our side.
 *  - The "when does it end" question becomes trivial: the effect's own duration
 *    reaching zero IS the end condition, so ModEvents can detect the pop moment
 *    simply by polling LivingEntity#hasEffect on a tick listener instead of running
 *    a second, independently-synced timer/capability.
 *  - applyEffectTick already runs once per tick while the effect is active, which is
 *    exactly the hook we need to nudge vertical motion smoothly every tick.
 *
 * The actual "lift" is done by directly setting the entity's vertical delta-movement
 * each tick (server side only - motion syncs to clients automatically), easing the
 * speed down over the last second so the ascent stops smoothly instead of snapping.
 */
public class FloatingMobEffect extends MobEffect {

    // Peak ascent speed in blocks/tick. 0.18 * 200 ticks (10s) ~= 36 blocks of climb,
    // comfortably in the requested 30-40 block "wide view" range.
    private static final double ASCEND_SPEED = 0.18D;

    // Number of ticks (at the end of the effect) over which we ease the ascent
    // speed back down to zero, so the player doesn't just stop dead in the air.
    private static final int EASE_OUT_TICKS = 20;

    public FloatingMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF8FD8E8); // bubblegum-pink tint for the HUD icon/particles
    }

    /**
     * Called every tick the effect is active (see isDurationEffectTick below).
     */
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return; // only drive motion server-side; it syncs to the client automatically
        }

        int remainingTicks = getRemainingTicks(entity);
        double speed = ASCEND_SPEED;
        if (remainingTicks < EASE_OUT_TICKS) {
            // Linearly ease the climb speed down to 0 over the final EASE_OUT_TICKS
            // so the bubble "runs out of lift" smoothly instead of cutting off hard.
            speed *= Math.max(0.0D, remainingTicks) / (double) EASE_OUT_TICKS;
        }

        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x, speed, motion.z);

        // Prevent fall-distance from silently accumulating while ascending, and
        // force a velocity packet to the client so the smooth motion isn't jittery.
        entity.fallDistance = 0.0F;
        entity.hurtMarked = true;
    }

    /**
     * Returning true means applyEffectTick fires every single tick rather than
     * only on specific intervals (which is how effects like Poison/Regeneration behave).
     */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    private int getRemainingTicks(LivingEntity entity) {
        var instance = entity.getEffect(ModMobEffects.FLOATING.get());
        return instance != null ? instance.getDuration() : 0;
    }
}
