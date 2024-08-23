package max.betterrockets.item.custom;

import max.betterrockets.ModComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;


public class RechargeableFireworkItem extends Item {

    public RechargeableFireworkItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        ItemStack itemStack = user.getStackInHand(hand);
        if (!world.isClient && user.isFallFlying()) {
            user.setCurrentHand(hand);
            FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(world, itemStack, user);
            world.spawnEntity(fireworkRocketEntity);
            int loaded_fireworks = itemStack.getOrDefault(ModComponents.ROCKETS_LOADED, 64);
            loaded_fireworks--;
            itemStack.set(ModComponents.ROCKETS_LOADED, loaded_fireworks);
            user.sendMessage(Text.of("§9" + loaded_fireworks + " Fireworks reimain"), true);
        }
        return new TypedActionResult<>(ActionResult.SUCCESS, itemStack);
    }

}