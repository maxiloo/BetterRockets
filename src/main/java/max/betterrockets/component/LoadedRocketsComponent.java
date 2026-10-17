package max.betterrockets.component;

import com.mojang.serialization.Codec;
import max.betterrockets.ModComponents;
import max.betterrockets.item.custom.RechargeableFireworkItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

/**
 * Number of firework rockets loaded into a rocket bundle.
 * <p>
 * Implements {@link TooltipProvider}, so the tooltip is rendered directly from the component
 * (registered via {@code ItemComponentTooltipProviderRegistry} in {@code BetterRockets#onInitialize}).
 */
public record LoadedRocketsComponent(int count) implements TooltipProvider {

    /**
     * Serialized as a plain int – identical to the previous {@code Integer} component,
     * so rocket bundles in existing worlds keep their loaded rockets.
     */
    public static final Codec<LoadedRocketsComponent> CODEC =
            Codec.INT.xmap(LoadedRocketsComponent::new, LoadedRocketsComponent::count);

    public LoadedRocketsComponent {
        count = Math.clamp(count, 0, RechargeableFireworkItem.MAX_LOAD);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag, DataComponentGetter components) {
        if (count > 0) {
            int flightDuration = components.getOrDefault(ModComponents.ROCKET_TYPE, 1);
            tooltip.accept(Component.translatable("itemTooltip.better-rockets.rechargeable_firework_type", flightDuration)
                    .withStyle(ChatFormatting.BLUE));
            tooltip.accept(Component.translatable("itemTooltip.better-rockets.rechargeable_firework", count, RechargeableFireworkItem.MAX_LOAD)
                    .withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.accept(Component.translatable("itemTooltip.better-rockets.rechargeable_firework_empty")
                    .withStyle(ChatFormatting.GOLD));
        }
    }
}
