package com.bubblegumfloat.event;

import com.bubblegumfloat.effect.ModMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Central home for all @SubscribeEvent handlers that aren't tied to a single item/effect class.
 *
 * Responsible for:
 *  - Detecting the exact tick the Floating effect expires (the bubble "pop"), since a
 *    plain MobEffect has no built-in "onExpire" callback we can hook.
 *  - Playing the pop sound + particle burst at that moment.
 *  - Granting one fall-damage-free landing afterwards, by canceling the next LivingFallEvent.
 *
 * This class is registered statically to MinecraftForge.EVENT_BUS in BubbleGumFloat's constructor.
 */
public class ModEvents {

    // Players who currently have the Floating effect active, as of the last tick we checked.
    // Used purely to detect the transition "had it last tick, don't have it now" == pop.
    private static final Set<UUID> FLOATING_LAST_TICK = new HashSet<>();

    // Players who just popped their bubble and are owed exactly one fall-damage-free landing.
    private static final Set<UUID> FALL_DAMAGE_IMMUNE = new HashSet<>();

    /**
     * Runs once per player per server tick. We use this instead of a Forge "effect expired"
     * event because Forge only fires MobEffectEvent.Expired in a handful of removal paths;
     * polling hasEffect() here is simple and 100% reliable regardless of how the effect ends
     * (natural timeout, /effect clear, milk, death, etc. all fall out of the FLOATING set cleanly).
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side.isClient()) {
            return;
        }

        Player player = event.player;
        UUID id = player.getUUID();
        boolean hasFloating = player.hasEffect(ModMobEffects.FLOATING.get());

        if (hasFloating) {
            FLOATING_LAST_TICK.add(id);
            return;
        }

        // Was floating last tick, isn't anymore -> the bubble just popped.
        if (FLOATING_LAST_TICK.remove(id) && player instanceof ServerPlayer serverPlayer) {
            popBubble(serverPlayer);
            FALL_DAMAGE_IMMUNE.add(id);
        }
    }

    /**
     * Plays the pop sound + spawns a particle burst at the player, called the instant the
     * Floating effect runs out.
     */
    private static void popBubble(ServerPlayer player) {
        ServerLevel level = player.serverLevel();

        // 4. Pop sound effect (vanilla firework blast doubles nicely as a "pop").
        level.playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.PLAYERS, 1.0F, 1.4F);

        // Particle burst at the player's position.
        level.sendParticles(ParticleTypes.POOF,
                player.getX(), player.getEyeY(), player.getZ(),
                20, 0.3, 0.3, 0.3, 0.02);

        player.displayClientMessage(Component.literal("Your bubble pops!"), true);
    }

    /**
     * 5. Fall damage immunity: cancel the very next fall event for a player who just had
     * a bubble pop out from under them, then clear their immunity so future falls are normal.
     */
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (FALL_DAMAGE_IMMUNE.remove(player.getUUID())) {
            event.setCanceled(true);
        }
    }

    /**
     * Housekeeping: don't leak UUIDs in these sets for players who disconnect mid-float/fall.
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        FLOATING_LAST_TICK.remove(id);
        FALL_DAMAGE_IMMUNE.remove(id);
    }
}
