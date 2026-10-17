package max.betterrockets.item.custom;

import max.betterrockets.ModComponents;
import max.betterrockets.component.LoadedRocketsComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import java.util.ArrayList;

public class RechargeableFireworkItem extends Item {

    public static final int MAX_LOAD = 512;
    private static final int ALMOST_EMPTY = 64;
    private static final int ITEM_BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.44F, 0.53F, 1.0F);
    private static final int ALMOST_EMPTY_BAR_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 0.33F, 0.33F);

    public RechargeableFireworkItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference) {
        if (clickType == ClickAction.PRIMARY && otherStack.is(Items.FIREWORK_ROCKET) && !isFull(stack)) {
            int fireworks_other_stack = otherStack.getCount();
            int fireworks_loaded_old = getLoadedFireworks(stack);

            int flightDuration = otherStack.getOrDefault(DataComponents.FIREWORKS, new Fireworks(1, new ArrayList<>())).flightDuration();

            if (fireworks_loaded_old == 0) {
                setFireworkType(stack, flightDuration);
            } else if (flightDuration != getFireworkType(stack)) {
                return false;
            }

            int fireworks_loaded = fireworks_other_stack + fireworks_loaded_old;
            if (fireworks_loaded > MAX_LOAD) {
                fireworks_other_stack = fireworks_loaded - MAX_LOAD;
                fireworks_loaded = MAX_LOAD;
            } else {
                fireworks_other_stack = 0;
            }
            setLoadedFireworks(stack, fireworks_loaded);
            otherStack.setCount(fireworks_other_stack);
            if (fireworks_loaded > fireworks_loaded_old) {
                playInsertSound(player);
            }
            removeLegacyLore(stack);
            return true;
        } else if (clickType == ClickAction.SECONDARY && !isEmpty(stack) && otherStack.isEmpty()) {
            if (slot.allowModification(player)) {
                int loadedFireworks = getLoadedFireworks(stack);
                int firework_return_size = Math.min(loadedFireworks, 64);
                int firework_left = loadedFireworks - firework_return_size;
                ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET, firework_return_size);
                fireworkStack.set(DataComponents.FIREWORKS, new Fireworks(getFireworkType(stack), new ArrayList<>()));
                cursorStackReference.set(fireworkStack);
                setLoadedFireworks(stack, firework_left);
                playRemoveOneSound(player);
                removeLegacyLore(stack);
                return true;
            }
        } else return clickType == ClickAction.SECONDARY;
        return false;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {

        ItemStack itemStack = user.getItemInHand(hand);

        if (!world.isClientSide() && user.isFallFlying()) {
            if (isEmpty(itemStack)) {
                playEmptySound(world, user);
                return InteractionResult.FAIL;
            }

            user.startUsingItem(hand);

            spawnFireworkEntity(world, user, itemStack);

            int loaded_fireworks = getLoadedFireworks(itemStack);
            loaded_fireworks--;
            setLoadedFireworks(itemStack, loaded_fireworks);
            removeLegacyLore(itemStack);
            return InteractionResult.SUCCESS;

        } else {
            return InteractionResult.FAIL;

        }
    }

    public void spawnFireworkEntity(Level world, Player user, ItemStack itemStack) {
        ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET, 1);
        fireworkStack.set(DataComponents.FIREWORKS, new Fireworks(getFireworkType(itemStack), new ArrayList<>()));
        FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(world, fireworkStack, user);
        world.addFreshEntity(fireworkRocketEntity);
    }

    private boolean isFull(ItemStack itemStack) {
        return getLoadedFireworks(itemStack) == MAX_LOAD;
    }

    private boolean isEmpty(ItemStack itemStack) {
        return getLoadedFireworks(itemStack) <= 0;
    }

    public int getLoadedFireworks(ItemStack itemStack) {
        LoadedRocketsComponent loaded = itemStack.get(ModComponents.ROCKETS_LOADED);
        return loaded == null ? 0 : loaded.count();
    }

    public void setLoadedFireworks(ItemStack itemStack, int number) {
        // LoadedRocketsComponent clamps the value to 0..MAX_LOAD
        itemStack.set(ModComponents.ROCKETS_LOADED, new LoadedRocketsComponent(number));
    }

    public int getFireworkType(ItemStack itemStack) {
        return itemStack.getOrDefault(ModComponents.ROCKET_TYPE, 1);
    }

    public void setFireworkType(ItemStack itemStack, int number) {
        itemStack.set(ModComponents.ROCKET_TYPE, number);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        LoadedRocketsComponent loaded = stack.get(ModComponents.ROCKETS_LOADED);
        return loaded != null && loaded.count() < MAX_LOAD;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) (getLoadedFireworks(stack) * 13.0f / MAX_LOAD);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return getLoadedFireworks(stack) <= ALMOST_EMPTY ? ALMOST_EMPTY_BAR_COLOR : ITEM_BAR_COLOR;
    }

    /**
     * Older versions of this mod wrote the tooltip into the LORE component.
     * The tooltip is now provided by {@link LoadedRocketsComponent}, so the old lore is removed
     * to avoid showing the information twice.
     */
    public void removeLegacyLore(ItemStack stack) {
        if (stack.has(DataComponents.LORE)) {
            stack.remove(DataComponents.LORE);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        removeLegacyLore(stack);
    }

    public void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    public void playEmptySound(Level world, Player user) {
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }
}