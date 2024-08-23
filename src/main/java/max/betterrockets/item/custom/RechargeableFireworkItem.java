package max.betterrockets.item.custom;

import max.betterrockets.ModComponents;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;


public class RechargeableFireworkItem extends Item {

    private static final int MAX_LOAD = 512;
    private static final int ITEM_BAR_COLOR = MathHelper.packRgb(0.4F, 0.4F, 1.0F);

    public RechargeableFireworkItem(Item.Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.RIGHT && otherStack.isOf(Items.FIREWORK_ROCKET)) {
            if (!isFull(stack)) {
                int fireworks_other_stack = otherStack.getCount();
                int fireworks_loaded_old = getLoadedFireworks(stack);
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
        }
        return false;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient && user.isFallFlying()) {
            if (isEmpty(itemStack)) {
                playEmptySound(world, user);
                return new TypedActionResult<>(ActionResult.SUCCESS, itemStack);
            }
            user.setCurrentHand(hand);
            FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(world, itemStack, user);
            world.spawnEntity(fireworkRocketEntity);
            int loaded_fireworks = getLoadedFireworks(itemStack);
            loaded_fireworks--;
            setLoadedFireworks(itemStack, loaded_fireworks);
            if (loaded_fireworks == 64 || loaded_fireworks == 8) {
                user.sendMessage(Text.translatable("itemLoadWarningLastX.better-rockets.rechargeable_firework", loaded_fireworks).formatted(Formatting.GOLD), true);
            } else if (loaded_fireworks == 1) {
                user.sendMessage(Text.translatable("itemLoadWarningLastOne.better-rockets.rechargeable_firework", loaded_fireworks).formatted(Formatting.RED, Formatting.BOLD), true);
            }
        }
        return new TypedActionResult<>(ActionResult.SUCCESS, itemStack);
    }

    private boolean isFull(ItemStack itemStack) {
        return getLoadedFireworks(itemStack) == MAX_LOAD;
    }

    private boolean isEmpty(ItemStack itemStack) {
        return getLoadedFireworks(itemStack) <= 0;
    }

    private int getLoadedFireworks(ItemStack itemStack) {
        return itemStack.getOrDefault(ModComponents.ROCKETS_LOADED, MAX_LOAD);
    }

    private void setLoadedFireworks(ItemStack itemStack, int number) {
        if (number > MAX_LOAD) {
            number = MAX_LOAD;
        } else if (number < 0) {
            number = 0;
        }
        itemStack.set(ModComponents.ROCKETS_LOADED, number);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return getLoadedFireworks(stack) < MAX_LOAD;
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
        tooltip.add(Text.translatable("itemTooltip.better-rockets.rechargeable_firework", getLoadedFireworks(stack), MAX_LOAD).formatted(Formatting.GOLD));
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private void playEmptySound(World world, PlayerEntity user) {
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }
}