package com.gtnewhorizon.structurelib.events;

import lombok.RequiredArgsConstructor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * Fired off when a player begins building a multiblock using the blueprint
 */
@RequiredArgsConstructor
public class SurvivalConstructionEvent extends Event {
    /**
     * The player initiating the structure construction
     */
    public final EntityPlayer player;

    /**
     * The item the player is holding
     */
    public final ItemStack itemStack;

    /**
     * The world the player is in
     */
    public final World world;

    /**
     * The location of the multiblock controller
     */
    public final int x, y, z;

    public static class Start extends SurvivalConstructionEvent {
        public Start(EntityPlayer player, ItemStack itemStack, World world, int x, int y, int z) {
            super(player, itemStack, world, x, y, z);
        }
    }

    public static class End extends SurvivalConstructionEvent {
        public End(EntityPlayer player, ItemStack itemStack, World world, int x, int y, int z) {
            super(player, itemStack, world, x, y, z);
        }
    }
}
