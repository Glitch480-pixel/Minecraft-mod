package com.bubblegumfloat.item;

import com.bubblegumfloat.effect.ModMobEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The Bubble Gum item. Right-click (use) to blow a bubble that lifts you gently into
 * the sky for 30 seconds, then pops - dropping you back down without fall damage.
 *
 * See com.bubblegumfloat.effect.FloatingMobEffect for the actual lift behaviour, and
 * com.bubblegumfloat.event.ModEvents for the pop detection + fall-damage immunity.
 */
public class BubbleGumItem extends Item {

    // 30 seconds of float time (20 ticks/second).
    private static final int FLOAT_DURATION_TICKS = 20 * 30;

    // 8 second cooldown - within the requested 5-10s range, prevents spamming.
    private static final int COOLDOWN_TICKS = 20 * 8;

    public BubbleGumItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Everything here is gameplay-affecting, so only run it on the server;
        // the client will simply mirror the resulting state (item count, effect, etc.).
        if (!level.isClientSide) {
            // 1. Bubble-blowing sound. No vanilla "bubble gum" sound exists, so we reuse
            //    the slime squish sound as a stand-in - swap this out in sounds.json/resources
            //    later if you add a custom sound event.
            level.playSound(null, player.blockPosition(), SoundEvents.SLIME_SQUISH,
                    SoundSource.PLAYERS, 1.0F, 1.2F);

            // 2. Apply the custom Floating effect; FloatingMobEffect drives the actual ascent.
            player.addEffect(new MobEffectInstance(
                    ModMobEffects.FLOATING.get(),
                    FLOAT_DURATION_TICKS,
                    0,      // amplifier - unused by FloatingMobEffect, kept at 0
                    false,  // not ambient
                    true,   // show particles
                    true    // show icon
            ));

            // 3. Feedback message.
            player.displayClientMessage(Component.literal("You blow a big bubble..."), true);

            // 6. Consume one Bubble Gum, respecting creative mode.
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            // 7. Cooldown so the item can't be spammed. Vanilla already blocks use() from
            //    being called again while on cooldown (see ServerPlayerGameMode#useItem).
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
