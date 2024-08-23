package max.betterrockets;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterRockets implements ModInitializer {

	public static final String MOD_ID = "better-rockets";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModComponents.registerModComponents();
	}
}