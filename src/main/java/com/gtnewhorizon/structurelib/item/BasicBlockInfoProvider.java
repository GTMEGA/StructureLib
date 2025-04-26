package com.gtnewhorizon.structurelib.item;

import com.gtnewhorizon.structurelib.structure.BlockInfo;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class BasicBlockInfoProvider implements IBlockInfoProvider {
    BlockInfo blockInfo;

    public BasicBlockInfoProvider(Block block) {
        this.blockInfo = new BlockInfo(block, 0);
    }

    @Override
    public BlockInfo getBlockInfo(ItemStack itemStack) {
        return this.blockInfo;
    }

    @Override
    public boolean matches(BlockInfo other) {
        return this.blockInfo.equals(other);
    }
}
