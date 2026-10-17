package max.betterrockets;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;
import net.minecraft.core.component.DataComponents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterRockets implements ModInitializer {

	public static final String MOD_ID = "better-rockets";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModComponents.registerModComponents();

		// Show the rocket bundle tooltip right after the vanilla firework tooltip ("Flight Duration")
		ItemComponentTooltipProviderRegistry.addAfter(DataComponents.FIREWORKS, ModComponents.ROCKETS_LOADED);
	}
}