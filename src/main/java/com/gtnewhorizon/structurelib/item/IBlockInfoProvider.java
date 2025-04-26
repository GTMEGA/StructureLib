package com.gtnewhorizon.structurelib.item;

import com.gtnewhorizon.structurelib.structure.BlockInfo;
import net.minecraft.item.ItemStack;

public interface IBlockInfoProvider {
    BlockInfo getBlockInfo(ItemStack itemStack);

    boolean matches(BlockInfo other);
}
