package com.gtnewhorizon.structurelib.structure.adders;

import com.gtnewhorizon.structurelib.structure.BlockInfo;
import com.gtnewhorizon.structurelib.structure.BlockInfoError;
import net.minecraft.tileentity.TileEntity;

public interface ITileAdder<T> {
	/**
	 * Callback to add hatch, needs to check if tile is valid (and add it)
	 *
	 * @param tileEntity tile
	 * @return managed to add hatch (structure still valid)
	 */
	boolean apply(T t, TileEntity tileEntity);

	default BlockInfo[] getBlockInfo(T t) {
		return new BlockInfo[]{new BlockInfoError("UNKNOWN_TILE")};
	}
}
