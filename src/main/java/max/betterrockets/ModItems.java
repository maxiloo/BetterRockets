package max.betterrockets;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static Item register(Item item, String id) {
        Identifier itemID = Identifier.of(BetterRockets.MOD_ID, id);

        Item registeredItem = Registry.register(Registries.ITEM, itemID, item);

        BetterRockets.LOGGER.info("Successfully registered {}", id);

        return registeredItem;
    }

    public static final Item ROCKET_TANK = register(
            new Item(new Item.Settings()),
            "rocket_tank"
    );

    public static void initialize() {
        // Get the event for modifying entries in the ingredients group.
// And register an event handler that adds our suspicious item to the ingredients group.
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ModItems.ROCKET_TANK));
    }
}
