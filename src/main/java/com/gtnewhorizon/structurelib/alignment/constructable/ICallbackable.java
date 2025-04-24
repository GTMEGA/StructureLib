package com.gtnewhorizon.structurelib.alignment.constructable;

import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.util.Vec3Impl;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Provides all the information needed to run a structure check with a custom callback
 * @param <T>
 */
public interface ICallbackable<T> {
    /**
     *
     * @return The structure definition of the multiblock
     */
    IStructureDefinition<T> getStructureDefinition();

    /**
     *
     * @return an array of active pieces and the corresponding offset of those pieces
     */
    Pair<String, Vec3Impl>[] getActivePieces();

    /**
     *
     * @return the orientation of the controller
     */
    ExtendedFacing getExtendedFacing();

    /**
     *
     * @return the parameterized class
     */
    Class<T> getType();

    /**
     *
     * @param player
     * @param x
     * @param y
     * @param z
     * @param callback
     */
    @SuppressWarnings("rawtypes")
    default void runCallback(EntityPlayer player, int x, int y, int z, IStructureDefinition.Callback callback) {
        val structure = this.getStructureDefinition();

        val activePieces = this.getActivePieces();

        val extendedFacing = this.getExtendedFacing();

        for (val piece : activePieces) {
            val offset = piece.getRight();

            structure.check(this.getType().cast(this), piece.getLeft(),
                            player.worldObj,
                            extendedFacing,
                            x, y, z,
                            offset.get0(), offset.get1(), offset.get2(),
                            true,
                            callback);
        }
    }
}