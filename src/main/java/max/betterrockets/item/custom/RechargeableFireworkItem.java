package max.betterrockets.item.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class RechargeableFireworkItem extends Item {
    private static final double MAX_VELOCITY = 4;
    private static final double VELOCITY_INCREMENT = 0.3;
    private static final int MAX_USE_TIME = 10;
    private static final int COOLDOWN = 30;
    private static final int MAX_CHARGE = 64;
    private int charge;

    public RechargeableFireworkItem(Settings settings) {
        super(settings);
        this.charge = MAX_CHARGE;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient && user.isFallFlying()) {
            user.setCurrentHand(hand);
            user.getItemCooldownManager().set(this, COOLDOWN);
        }
        return new TypedActionResult<>(ActionResult.SUCCESS, itemStack);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        super.usageTick(world, user, stack, remainingUseTicks);
        if (user.isFallFlying()) {
            boostPlayer((PlayerEntity) user);
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return MAX_USE_TIME;
    }

    private void boostPlayer(PlayerEntity user) {

        double velocity = user.getVelocity().length();

        if (velocity < MAX_VELOCITY) {
            user.addVelocity(user.getRotationVector().x * VELOCITY_INCREMENT, user.getRotationVector().y * VELOCITY_INCREMENT, user.getRotationVector().z * VELOCITY_INCREMENT);
            user.velocityModified = true; // Mark velocity as modified
        }
    }

}