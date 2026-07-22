package com.bubblegumfloat.item;

import com.bubblegumfloat.BubbleGumFloat;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * DeferredRegister for all items added by this mod.
 */
public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BubbleGumFloat.MOD_ID);

    public static final RegistryObject<Item> BUBBLE_GUM =
            ITEMS.register("bubble_gum", () -> new BubbleGumItem(new Item.Properties().stacksTo(16)));
}
