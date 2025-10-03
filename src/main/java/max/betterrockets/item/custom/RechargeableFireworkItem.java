package max.betterrockets.item.custom;

import max.betterrockets.ModComponents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class RechargeableFireworkItem extends Item {

    private static final int MAX_LOAD = 512;
    private static final int ALMOST_EMPTY = 64;
    private static final int ITEM_BAR_COLOR = ColorHelper.fromFloats(1.0F, 0.44F, 0.53F, 1.0F);
    private static final int ALMOST_EMPTY_BAR_COLOR = ColorHelper.fromFloats(1.0F, 1.0F, 0.33F, 0.33F);

    public RechargeableFireworkItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.LEFT && otherStack.isOf(Items.FIREWORK_ROCKET) && !isFull(stack)) {
            int fireworks_other_stack = otherStack.getCount();
            int fireworks_loaded_old = getLoadedFireworks(stack);

            int flightDuration = otherStack.getOrDefault(DataComponentTypes.FIREWORKS, new FireworksComponent(1, new ArrayList<>())).flightDuration();

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
            updateTooltip(stack);
            return true;
        } else if (clickType == ClickType.RIGHT && !isEmpty(stack) && otherStack.isEmpty()) {
            if (slot.canTakePartial(player)) {
                int loadedFireworks = getLoadedFireworks(stack);
                int firework_return_size = Math.min(loadedFireworks, 64);
                int firework_left = loadedFireworks - firework_return_size;
                ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET, firework_return_size);
                fireworkStack.set(DataComponentTypes.FIREWORKS, new FireworksComponent(getFireworkType(stack), new ArrayList<>()));
                cursorStackReference.set(fireworkStack);
                setLoadedFireworks(stack, firework_left);
                playRemoveOneSound(player);
                updateTooltip(stack);
                return true;
            }
        } else return clickType == ClickType.RIGHT;
        return false;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {

        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient() && user.isGliding()) {
            if (isEmpty(itemStack)) {
                playEmptySound(world, user);
                return ActionResult.FAIL;
            }

            user.setCurrentHand(hand);

            spawnFireworkEntity(world, user, itemStack);

            int loaded_fireworks = getLoadedFireworks(itemStack);
            loaded_fireworks--;
            setLoadedFireworks(itemStack, loaded_fireworks);
            updateTooltip(itemStack);
            return ActionResult.SUCCESS;

        } else {
            return ActionResult.FAIL;

        }
    }

    public void spawnFireworkEntity(World world, PlayerEntity user, ItemStack itemStack) {
        ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET, 1);
        fireworkStack.set(DataComponentTypes.FIREWORKS, new FireworksComponent(getFireworkType(itemStack), new ArrayList<>()));
        FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(world, fireworkStack, user);
        world.spawnEntity(fireworkRocketEntity);
    }

    private boolean isFull(ItemStack itemStack) {
        return getLoadedFireworks(itemStack) == MAX_LOAD;
    }

    private boolean isEmpty(ItemStack itemStack) {
        return getLoadedFireworks(itemStack) <= 0;
    }

    public int getLoadedFireworks(ItemStack itemStack) {
        int loadedFireworks = itemStack.getOrDefault(ModComponents.ROCKETS_LOADED, 0);
        if (loadedFireworks < 0) {
            loadedFireworks = 0;
            setLoadedFireworks(itemStack, loadedFireworks);
        }
        return loadedFireworks;
    }

    public void setLoadedFireworks(ItemStack itemStack, int number) {
        if (number > MAX_LOAD) {
            number = MAX_LOAD;
        } else if (number < 0) {
            number = 0;
        }
        itemStack.set(ModComponents.ROCKETS_LOADED, number);
    }

    public int getFireworkType(ItemStack itemStack) {
        return itemStack.getOrDefault(ModComponents.ROCKET_TYPE, 1);
    }

    public void setFireworkType(ItemStack itemStack, int number) {
        itemStack.set(ModComponents.ROCKET_TYPE, number);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        int loadedFireworks = stack.getOrDefault(ModComponents.ROCKETS_LOADED, -1);

        return (loadedFireworks >= 0 && loadedFireworks < MAX_LOAD);
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return (int) (getLoadedFireworks(stack) * 13.0f / MAX_LOAD);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return getLoadedFireworks(stack) <= ALMOST_EMPTY ? ALMOST_EMPTY_BAR_COLOR : ITEM_BAR_COLOR;
    }

    public void updateTooltip(ItemStack stack) {
        List<Text> tooltip = new ArrayList<>();
        int loadedFireworks = stack.getOrDefault(ModComponents.ROCKETS_LOADED, -1);
        if (loadedFireworks > 0) {
            tooltip.add(Text.translatable("itemTooltip.better-rockets.rechargeable_firework_type", getFireworkType(stack)).formatted(Formatting.BLUE));
            tooltip.add(Text.translatable("itemTooltip.better-rockets.rechargeable_firework", getLoadedFireworks(stack), MAX_LOAD).formatted(Formatting.GOLD));
        } else if (loadedFireworks == 0) {
            tooltip.add(Text.translatable("itemTooltip.better-rockets.rechargeable_firework_empty").formatted(Formatting.GOLD));
        }

        stack.set(DataComponentTypes.LORE, new LoreComponent(tooltip));
    }

    public void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.getEntityWorld().getRandom().nextFloat() * 0.4F);
    }

    public void playEmptySound(World world, PlayerEntity user) {
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    public void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.getEntityWorld().getRandom().nextFloat() * 0.4F);
    }
}