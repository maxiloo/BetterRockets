package max.betterrockets.item.custom;

import max.betterrockets.ModComponents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class RechargeableFireworkItem extends Item {

    private static final int MAX_LOAD = 512;
    private static final int ITEM_BAR_COLOR = packRGB(0.4F, 0.4F, 1.0F);

    public RechargeableFireworkItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.RIGHT && otherStack.isOf(Items.FIREWORK_ROCKET) && !isFull(stack)) {
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
            return true;
        }
        return false;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {

        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient && user.isGliding()) {
            if (isEmpty(itemStack)) {
                playEmptySound(world, user);
                return ActionResult.FAIL;
            }

            user.setCurrentHand(hand);

            spawnFireworkEntity(world, user, itemStack);

            int loaded_fireworks = getLoadedFireworks(itemStack);
            loaded_fireworks--;
            setLoadedFireworks(itemStack, loaded_fireworks);

            if (loaded_fireworks == 64 || loaded_fireworks == 8) {
                user.sendMessage(Text.translatable("itemLoadWarningLastX.better-rockets.rechargeable_firework", loaded_fireworks).formatted(Formatting.GOLD), true);
            } else if (loaded_fireworks == 1) {
                user.sendMessage(Text.translatable("itemLoadWarningLastOne.better-rockets.rechargeable_firework", loaded_fireworks).formatted(Formatting.RED, Formatting.BOLD), true);
            }

            return ActionResult.SUCCESS;

        } else {
            return ActionResult.FAIL;

        }
    }

    public void spawnFireworkEntity(World world, PlayerEntity user, ItemStack itemStack) {
        FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(world, itemStack, user);
        itemStack.set(DataComponentTypes.FIREWORKS, new FireworksComponent(getFireworkType(itemStack), new ArrayList<>()));
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
        return ITEM_BAR_COLOR;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {

        int loadedFireworks = stack.getOrDefault(ModComponents.ROCKETS_LOADED, -1);

        if (loadedFireworks > 0) {
            tooltip.add(Text.translatable("itemTooltip.better-rockets.rechargeable_firework_type", getFireworkType(stack)).formatted(Formatting.BLUE));
            tooltip.add(Text.translatable("itemTooltip.better-rockets.rechargeable_firework", getLoadedFireworks(stack), MAX_LOAD).formatted(Formatting.GOLD));
        } else if (loadedFireworks == 0){
            tooltip.add(Text.translatable("itemTooltip.better-rockets.rechargeable_firework_empty").formatted(Formatting.GOLD));
        }
    }

    public void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    public void playEmptySound(World world, PlayerEntity user) {
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    private static int packRGB(float red, float green, float blue) {
        int r = (int) (red * 255.0F);
        int g = (int) (green * 255.0F);
        int b = (int) (blue * 255.0F);
        return (r << 16) | (g << 8) | b;
    }
}