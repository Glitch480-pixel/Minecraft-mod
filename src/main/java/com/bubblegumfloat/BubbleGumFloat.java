package com.bubblegumfloat;

import com.bubblegumfloat.effect.ModMobEffects;
import com.bubblegumfloat.item.ModCreativeTabs;
import com.bubblegumfloat.item.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod entry point. Registers all DeferredRegister instances to the mod event bus
 * and hooks up the Forge (game) event bus listeners defined in {@link com.bubblegumfloat.event.ModEvents}.
 */
@Mod(BubbleGumFloat.MOD_ID)
public class BubbleGumFloat {

    public static final String MOD_ID = "bubblegumfloat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public BubbleGumFloat() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register deferred registries (items, mob effects, creative tabs) to the mod bus.
        ModItems.ITEMS.register(modEventBus);
        ModMobEffects.MOB_EFFECTS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        // Register the (static) game-event listener class to the main Forge event bus.
        // This is where LivingFallEvent / PlayerTickEvent handlers live.
        MinecraftForge.EVENT_BUS.register(com.bubblegumfloat.event.ModEvents.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("BubbleGumFloat loaded - chew responsibly.");
    }
}
