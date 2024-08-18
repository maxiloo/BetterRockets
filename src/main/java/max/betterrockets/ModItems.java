package max.betterrockets;

import max.betterrockets.item.custom.RechargeableFireworkItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item RECHARGEABLE_FIREWORK = registerItem("rechargeable_firework", new RechargeableFireworkItem(new Item.Settings()));

    private static void addItemsToToolItemGroup(FabricItemGroupEntries entries) {
        entries.add(RECHARGEABLE_FIREWORK);
    }

    private static Item registerItem(String name, Item item) {
        Identifier itemID = Identifier.of(BetterRockets.MOD_ID, name);

        Item registeredItem = Registry.register(Registries.ITEM, itemID, item);

        BetterRockets.LOGGER.info("Successfully registered {}", name);

        return registeredItem;
    }

    public static void registerModItems() {
        BetterRockets.LOGGER.info("Registering ModItems for " + BetterRockets.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(ModItems::addItemsToToolItemGroup);
    }
}
