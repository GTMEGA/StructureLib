package com.gtnewhorizon.structurelib.alignment.constructable;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.alignment.IAlignment;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.item.IBlockInfoProvider;
import com.gtnewhorizon.structurelib.structure.BlockInfo;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.IStructureElement;
import com.gtnewhorizon.structurelib.util.Vec3Impl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class ConstructableUtility {
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
                    if (tTileEntity instanceof ICallbackable) {
                        val callbackable = (ICallbackable) tTileEntity;
                        val callback = new OnElementScanCallback();

                        callbackable.runCallback(aPlayer, aX, aY, aZ, callback);

                        for (val extendedBlockInfo : callback.blocksToPlace) {
                            val position = extendedBlockInfo.position;

                            for (int i = 0; i < aPlayer.inventory.getSizeInventory(); i++) {
                                val itemstack = aPlayer.inventory.getStackInSlot(i);

                                if (!(itemstack.getItem() instanceof IBlockInfoProvider)) {
                                    continue;
                                }

                                val blockInfoProvider = ((IBlockInfoProvider) itemstack.getItem());
                                val blockInfo = blockInfoProvider.getBlockInfo(itemstack);

                                for (val blockInfoOption : extendedBlockInfo.blockInfo) {
                                    if (blockInfoOption.equals(blockInfo)) {
                                        val didPlace = aWorld.setBlock(position.get0(),
                                                                       position.get1(),
                                                                       position.get2(),
                                                                       blockInfoOption.block,
                                                                       blockInfoOption.meta,
                                                                       3);

                                        if (didPlace) {
                                            aPlayer.inventory.decrStackSize(i, 1);
                                        }
                                    }
                                }
                            }
                        }
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

            }
        }
        return false;
    }

    private static class OnElementScanCallback implements IStructureDefinition.Callback {
        final List<ExtendedBlockInfo> blocksToPlace;

        public OnElementScanCallback() {
            this.blocksToPlace = new ArrayList<>();
        }

        @Override
        public boolean onElementScan(int x, int y, int z,
                                     int a, int b, int c,
                                     World world,
                                     Object o,
                                     IStructureElement iStructureElement,
                                     boolean scanSuccessful) {
            if (scanSuccessful) {
                return true;
            }

            val blockInfo = iStructureElement.getBlockInfo(o, world, x, y, z);

            this.blocksToPlace.add(new ExtendedBlockInfo(blockInfo, new Vec3Impl(x, y, z)));

            return true;
        }
    }

    @RequiredArgsConstructor
    private static class ExtendedBlockInfo {
        final BlockInfo[] blockInfo;
        final Vec3Impl position;
    }
}
