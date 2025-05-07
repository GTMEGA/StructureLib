package com.gtnewhorizon.structurelib.item;

import com.gtnewhorizon.structurelib.structure.BlockInfo;
import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockInfoProvider implements IBlockInfoProvider {
    BlockInfo blockInfo;

    public ItemBlockInfoProvider(@NotNull ItemStack itemStack) {
        this.blockInfo = new BlockInfo(((ItemBlock) itemStack.getItem()).field_150939_a, itemStack.getItemDamage());
    }

    @Override
    public BlockInfo getBlockInfo(ItemStack itemStack) {
        return this.blockInfo;
    }
}
