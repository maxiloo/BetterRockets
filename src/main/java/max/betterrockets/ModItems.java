package max.betterrockets;

import max.betterrockets.item.custom.RechargeableFireworkItem;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class ModItems {

    public static final Item RECHARGEABLE_FIREWORK = registerItem("rechargeable_firework");

    private static void addItemsToToolItemGroup(FabricCreativeModeTabOutput entries) {
        entries.accept(RECHARGEABLE_FIREWORK);
    }

    private static Item registerItem(String name) {
        Identifier itemID = Identifier.fromNamespaceAndPath(BetterRockets.MOD_ID, name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, itemID);

        // Create Item.Settings and set the registry key directly
        Item.Properties settings = new Item.Properties()
                .setId(key)
                .stacksTo(1); // Add your settings here

        // Register the item with the settings
        Item item = new RechargeableFireworkItem(settings);
        return Registry.register(BuiltInRegistries.ITEM, itemID, item);
    }

    public static void registerModItems() {
        BetterRockets.LOGGER.info("Registering ModItems for " + BetterRockets.MOD_ID);

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(ModItems::addItemsToToolItemGroup);
    }
}
