package max.betterrockets;

import com.mojang.serialization.Codec;
import max.betterrockets.component.LoadedRocketsComponent;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class ModComponents {

    public static final DataComponentType<LoadedRocketsComponent> ROCKETS_LOADED = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(BetterRockets.MOD_ID, "rockets_loaded"),
            DataComponentType.<LoadedRocketsComponent>builder().persistent(LoadedRocketsComponent.CODEC).build()
    );

    public static final DataComponentType<Integer> ROCKET_TYPE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(BetterRockets.MOD_ID, "rocket_type"),
            DataComponentType.<Integer>builder().persistent(Codec.INT).build()
    );

    protected static void registerModComponents() {
        BetterRockets.LOGGER.info("Registering ModComponents for " + BetterRockets.MOD_ID);
    }
}
