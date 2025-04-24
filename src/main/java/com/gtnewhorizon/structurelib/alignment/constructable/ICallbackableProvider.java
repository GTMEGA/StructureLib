package com.gtnewhorizon.structurelib.alignment.constructable;

import org.jetbrains.annotations.Nullable;

/**
 * Implement this interface if this tile entity MIGHT be callbackable
 */
public interface ICallbackableProvider {
    /**
     * @return null if not callbackable, an instance otherwise.
     */
    @Nullable
    ICallbackable getCallbackable();
}
