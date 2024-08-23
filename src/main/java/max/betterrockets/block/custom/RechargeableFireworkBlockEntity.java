package max.betterrockets.block.custom;

import max.betterrockets.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class RechargeableFireworkBlockEntity extends BlockEntity {

    private int number = 7;
    private final static String KEY = "number";

    public RechargeableFireworkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.RECHARGEABLE_FIREWORK_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putInt(KEY, number);

        super.writeNbt(nbt, registryLookup);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        number = nbt.getInt(KEY);
    }
}