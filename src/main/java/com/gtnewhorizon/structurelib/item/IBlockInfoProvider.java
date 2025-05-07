package com.gtnewhorizon.structurelib.item;

import com.gtnewhorizon.structurelib.structure.BlockInfo;
import net.minecraft.item.ItemStack;

public interface IBlockInfoProvider {
    BlockInfo getBlockInfo(ItemStack itemStack);

    default boolean matches(ItemStack itemStack, BlockInfo other) {
        return this.getBlockInfo(itemStack).equals(other);
    }
}
