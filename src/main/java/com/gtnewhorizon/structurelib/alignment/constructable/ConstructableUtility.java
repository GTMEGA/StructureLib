package com.gtnewhorizon.structurelib.alignment.constructable;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.alignment.IAlignment;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.events.SurvivalConstructionEvent;
import com.gtnewhorizon.structurelib.item.BasicBlockInfoProvider;
import com.gtnewhorizon.structurelib.item.IBlockInfoProvider;
import com.gtnewhorizon.structurelib.structure.BlockInfo;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.IStructureElement;
import com.gtnewhorizon.structurelib.util.Vec3Impl;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class ConstructableUtility {
    private static final int LIMIT = 16;

    private ConstructableUtility() {

    }

    public static boolean handle(ItemStack aStack, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ, int aSide) {
        StructureLibAPI.startHinting(aWorld);
        boolean ret = handle0(aStack, aPlayer, aWorld, aX, aY, aZ, aSide);
        StructureLibAPI.endHinting(aWorld);
        return ret;
    }

    private static boolean handle0(ItemStack aStack, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ, int aSide) {
        TileEntity tTileEntity = aWorld.getTileEntity(aX, aY, aZ);
        if (tTileEntity == null || aPlayer instanceof FakePlayer) {
            return aPlayer instanceof EntityPlayerMP;
        }
        if (aPlayer instanceof EntityPlayerMP) {
            //struct gen
            if (aPlayer.isSneaking()) {
                if (aPlayer.capabilities.isCreativeMode) {
                    if (tTileEntity instanceof IConstructableProvider) {
                        IConstructable constructable = ((IConstructableProvider) tTileEntity).getConstructable();
                        if (constructable != null) {
                            constructable.construct(aStack, false);
                        }
                    } else if (tTileEntity instanceof IConstructable) {
                        ((IConstructable) tTileEntity).construct(aStack, false);
                    } else if (IMultiblockInfoContainer.contains(tTileEntity.getClass())) {
                        IMultiblockInfoContainer<TileEntity> iMultiblockInfoContainer = IMultiblockInfoContainer.get(tTileEntity.getClass());
                        if (tTileEntity instanceof IAlignment) {
                            iMultiblockInfoContainer.construct(aStack, false, tTileEntity,
                                    ((IAlignment) tTileEntity).getExtendedFacing());
                        } else {
                            iMultiblockInfoContainer.construct(aStack, false, tTileEntity,
                                    ExtendedFacing.of(ForgeDirection.getOrientation(aSide)));
                        }
                    }
                } else {
                    ICallbackable<?> callbackable = null;

                    if (tTileEntity instanceof ICallbackable) {
                        callbackable = (ICallbackable<?>) tTileEntity;
                    } else if (tTileEntity instanceof ICallbackableProvider) {
                        callbackable = ((ICallbackableProvider) tTileEntity).getCallbackable();
                    }

                    if (callbackable != null) {
                        val callback = new OnElementScanCallback();

                        val startEvent = new SurvivalConstructionEvent.Start(aPlayer, aStack, aWorld, aX, aY, aZ);
                        MinecraftForge.EVENT_BUS.post(startEvent);

                        callbackable.runCallback(aPlayer, aX, aY, aZ, aStack, callback);

                        var placedBlocks = 0;

                        for (int i = 0; i < aPlayer.inventory.mainInventory.length; i++) {
                            val itemstack = aPlayer.inventory.mainInventory[i];

                            if (itemstack == null) {
                                continue;
                            }

                            IBlockInfoProvider blockInfoProvider = null;

                            if (itemstack.getItem() instanceof IBlockInfoProvider) {
                                blockInfoProvider = ((IBlockInfoProvider) itemstack.getItem());
                            } else if (itemstack.getItem() != null && ItemBlock.class.equals(itemstack.getItem().getClass())) {
                                val itemBlock = (ItemBlock) itemstack.getItem();

                                blockInfoProvider = new BasicBlockInfoProvider(itemBlock.field_150939_a);
                            }

                            if (blockInfoProvider == null) {
                                continue;
                            }

                            val blockInfoSet = callback.blocksToPlace.keySet();
                            for (var iterator = blockInfoSet.iterator(); placedBlocks < LIMIT && iterator.hasNext(); ) {
                                val blockInfo = iterator.next();
                                if (!blockInfoProvider.matches(blockInfo)) {
                                    continue;
                                }

                                val positions = callback.blocksToPlace.get(blockInfo);

                                if (positions.isEmpty()) {
                                    continue;
                                }

                                while (!positions.isEmpty() && placedBlocks < LIMIT) {
                                    val position = positions.poll();

                                    val x = position.get0();
                                    val y = position.get1();
                                    val z = position.get2();

                                    if (aWorld.isAirBlock(x, y, z) && aPlayer.inventory.mainInventory[i] != null) {
                                        val didPlace = aWorld.setBlock(x, y, z, blockInfo.block, blockInfo.meta, 3);

                                        if (didPlace) {
                                            aPlayer.inventory.decrStackSize(i, 1);

                                            placedBlocks++;
                                        }
                                    }
                                }
                            }
                        }

                        if (placedBlocks > 0) {
                            aPlayer.inventoryContainer.detectAndSendChanges();
                        }

                        val endEvent = new SurvivalConstructionEvent.End(aPlayer, aStack, aWorld, aX, aY, aZ);
                        MinecraftForge.EVENT_BUS.post(endEvent);
                    }
                }
            }
            return true;
        } else if (StructureLib.isCurrentPlayer(aPlayer)) {//particles and text client side
            if (!aPlayer.isSneaking()) {
                if (tTileEntity instanceof IConstructableProvider) {
                    IConstructable constructable = ((IConstructableProvider) tTileEntity).getConstructable();
                    if (constructable != null) {
                        constructable.construct(aStack, true);
                        StructureLib.addClientSideChatMessages(constructable.getStructureDescription(aStack));
                    }
                } else if (tTileEntity instanceof IConstructable) {
                    IConstructable constructable = (IConstructable) tTileEntity;
                    constructable.construct(aStack, true);
                    StructureLib.addClientSideChatMessages(constructable.getStructureDescription(aStack));
                    return false;
                } else if (IMultiblockInfoContainer.contains(tTileEntity.getClass())) {
                    IMultiblockInfoContainer<TileEntity> iMultiblockInfoContainer = IMultiblockInfoContainer.get(tTileEntity.getClass());
                    if (tTileEntity instanceof IAlignment) {
                        iMultiblockInfoContainer.construct(aStack, true, tTileEntity,
                                ((IAlignment) tTileEntity).getExtendedFacing());
                    } else {
                        iMultiblockInfoContainer.construct(aStack, true, tTileEntity,
                                ExtendedFacing.of(ForgeDirection.getOrientation(aSide)));
                    }
                    StructureLib.addClientSideChatMessages(IMultiblockInfoContainer.get(tTileEntity.getClass()).getDescription(aStack));
                    return false;
                }
            } else {
                if (!aPlayer.capabilities.isCreativeMode) {
                    ICallbackable<?> callbackable = null;

                    if (tTileEntity instanceof ICallbackableProvider) {
                        callbackable = ((ICallbackableProvider) tTileEntity).getCallbackable();
                    } else if (tTileEntity instanceof ICallbackable<?>) {
                        callbackable = (ICallbackable<?>) tTileEntity;
                    }

                    if (callbackable != null) {
                        aPlayer.swingItem();
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    private static class OnElementScanCallback implements IStructureDefinition.Callback {
        final Map<BlockInfo, Queue<Vec3Impl>> blocksToPlace;

        public OnElementScanCallback() {
            this.blocksToPlace = new Object2ObjectArrayMap<>();
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public boolean onElementScan(int x, int y, int z,
                                     int a, int b, int c,
                                     World world,
                                     Object o,
                                     IStructureElement iStructureElement,
                                     boolean scanSuccessful) {
            if (scanSuccessful) {
                return true;
            }

            val blockInfoList = iStructureElement.getBlockInfo(o, world, x, y, z);

            val position = new Vec3Impl(x, y, z);

            for (val blockInfo : blockInfoList) {
                this.blocksToPlace.computeIfAbsent(blockInfo, key -> new ArrayDeque<>())
                                  .add(position);
            }

            return true;
        }
    }

    @RequiredArgsConstructor
    private static class ExtendedBlockInfo {
        final BlockInfo[] blockInfo;
        final Vec3Impl position;
    }
}
