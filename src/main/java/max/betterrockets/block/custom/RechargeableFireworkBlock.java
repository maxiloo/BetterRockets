package max.betterrockets.block.custom;

import max.betterrockets.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class RechargeableFireworkBlock extends Block implements BlockEntityProvider {
    public static final IntProperty DURABILITY = IntProperty.of("durability", 0, 64);

    public RechargeableFireworkBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(DURABILITY, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DURABILITY);
    }


    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            int charge = state.get(RechargeableFireworkBlock.DURABILITY);
            player.sendMessage(Text.of("Damage: " + charge), false);
            return ActionResult.SUCCESS;
        }
        return ActionResult.CONSUME;
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        if (world instanceof ServerWorld) {
            int durability = state.get(RechargeableFireworkBlock.DURABILITY);

            ItemStack itemStack = new ItemStack(ModItems.RECHARGEABLE_FIREWORK);
            itemStack.setDamage(durability);

            Block.dropStack((World) world, pos, itemStack);

            super.onBroken(world, pos, state);
        }
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RechargeableFireworkBlockEntity(pos, state);
    }

}
