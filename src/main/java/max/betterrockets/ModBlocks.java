package max.betterrockets;

import max.betterrockets.block.custom.RechargeableFireworkBlock;
import max.betterrockets.block.custom.RechargeableFireworkBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block RECHARGEABLE_FIREWORK_BLOCK = register(new RechargeableFireworkBlock(AbstractBlock.Settings.create().sounds(BlockSoundGroup.GRASS)), "rechargeable_firework_block", true);
    public static final BlockEntityType<RechargeableFireworkBlockEntity> RECHARGEABLE_FIREWORK_BLOCK_ENTITY = registerBlockEntity("rechargeable_firework_block", RechargeableFireworkBlockEntity::new, RECHARGEABLE_FIREWORK_BLOCK);

    public static Block register(Block block, String name, Boolean shouldRegisterItem) {
        Identifier id = Identifier.of(BetterRockets.MOD_ID, name);

        if (shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, id, blockItem);
        }

        Block registeredBlock =  Registry.register(Registries.BLOCK, id, block);

        BetterRockets.LOGGER.info("Successfully registered {}", name);

        return registeredBlock;
    }

    public static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String name, BlockEntityType.BlockEntityFactory<T> blockEntityClass, Block... blocks) {
        Identifier id = Identifier.of(BetterRockets.MOD_ID, name);
        BlockEntityType<T> blockEntityType = BlockEntityType.Builder.create(blockEntityClass, blocks).build(null);
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, blockEntityType);
    }

    public static void registerModBlocks() {
        BetterRockets.LOGGER.info("Registering ModBlocks for " + BetterRockets.MOD_ID);
    }
}
