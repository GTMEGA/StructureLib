package com.gtnewhorizon.structurelib.alignment.constructable;

import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.util.Vec3Impl;
import org.apache.commons.lang3.tuple.Pair;

public interface IDeconstructable<T> {
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
}