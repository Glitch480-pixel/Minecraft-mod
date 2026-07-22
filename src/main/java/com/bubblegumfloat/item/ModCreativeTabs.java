package com.bubblegumfloat.item;

import com.bubblegumfloat.BubbleGumFloat;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * A dedicated creative-mode tab for BubbleGumFloat items so Bubble Gum is easy to find
 * in the creative inventory. Its icon is the Bubble Gum item itself.
 */
public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BubbleGumFloat.MOD_ID);

    public static final RegistryObject<CreativeModeTab> BUBBLEGUMFLOAT_TAB =
            CREATIVE_MODE_TABS.register("bubblegumfloat_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.bubblegumfloat"))
                    .icon(() -> new ItemStack(ModItems.BUBBLE_GUM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BUBBLE_GUM.get());
                    })
                    .build());
}
