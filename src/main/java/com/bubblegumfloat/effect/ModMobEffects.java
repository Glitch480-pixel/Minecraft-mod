package com.bubblegumfloat.effect;

import com.bubblegumfloat.BubbleGumFloat;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * DeferredRegister for our custom MobEffect(s).
 * "Floating" is the effect applied by Bubble Gum that lifts the player upward.
 */
public class ModMobEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, BubbleGumFloat.MOD_ID);

    public static final RegistryObject<MobEffect> FLOATING =
            MOB_EFFECTS.register("floating", FloatingMobEffect::new);
}
