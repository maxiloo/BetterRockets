package max.betterrockets;

import max.betterrockets.item.custom.RechargeableFireworkItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item RECHARGEABLE_FIREWORK = registerItem("rechargeable_firework");

    private static void addItemsToToolItemGroup(FabricItemGroupEntries entries) {
        entries.add(RECHARGEABLE_FIREWORK);
    }

    private static Item registerItem(String name) {
        Identifier itemID = Identifier.of(BetterRockets.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, itemID);

        // Create Item.Settings and set the registry key directly
        Item.Settings settings = new Item.Settings()
                .registryKey(key)
                .maxCount(1); // Add your settings here

        // Register the item with the settings
        Item item = new RechargeableFireworkItem(settings);
        return Registry.register(Registries.ITEM, itemID, item);
    }

    public static void registerModItems() {
        BetterRockets.LOGGER.info("Registering ModItems for " + BetterRockets.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(ModItems::addItemsToToolItemGroup);
    }
}
