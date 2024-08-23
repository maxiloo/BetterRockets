package max.betterrockets;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModComponents {

    public static final ComponentType<Integer> ROCKETS_LOADED = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(BetterRockets.MOD_ID, "rockets_loaded"),
            ComponentType.<Integer>builder().codec(Codec.INT).build()
    );

    protected static void registerModComponents() {
        BetterRockets.LOGGER.info("Registering ModComponents for " + BetterRockets.MOD_ID);
    }
}
