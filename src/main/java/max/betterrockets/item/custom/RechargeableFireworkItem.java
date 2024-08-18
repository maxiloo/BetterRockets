package max.betterrockets.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class RechargeableFireworkItem extends Item {
    private static final int MAX_CHARGE = 64;
    private int charge;

    public RechargeableFireworkItem(Settings settings) {
        super(settings);
        this.charge = MAX_CHARGE;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient) {
            user.addVelocity(user.getRotationVector().x * 1.5, user.getRotationVector().y * 1.5, user.getRotationVector().z * 1.5);
            user.velocityModified = true; // Mark velocity as modified

            user.getItemCooldownManager().set(this, 10);
        }
        return new TypedActionResult<>(ActionResult.SUCCESS, itemStack);
    }
}
