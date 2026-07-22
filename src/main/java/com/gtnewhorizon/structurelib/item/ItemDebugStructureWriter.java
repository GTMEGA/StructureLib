package com.gtnewhorizon.structurelib.item;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.StructureUtility;
import com.gtnewhorizon.structurelib.util.Box;
import com.gtnewhorizon.structurelib.util.Vec3Impl;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.List;

import static com.gtnewhorizon.structurelib.StructureLibAPI.MOD_ID;
import static com.gtnewhorizon.structurelib.StructureLibAPI.getBlockHint;

public class ItemDebugStructureWriter extends Item {
    public enum Mode {
        Build, Clear
    }

    @SideOnly(Side.CLIENT)
    private IIcon eraserIcon;

    Vec3Impl pos1;
    Vec3Impl pos2;

    private static final String TAG_POS_1 = "pos1";
    private static final String TAG_POS_2 = "pos2";
    private static final String TAG_POS_CONTROLLER = "pos3";
    private static final String TAG_MODE = "mode";

    public ItemDebugStructureWriter() {
        setMaxStackSize(1);
        setUnlocalizedName("structurelib.debugStructureWriter");
        setTextureName(MOD_ID + ":itemDebugStructureWriter");
        setHasSubtypes(true);
        setCreativeTab(StructureLib.creativeTab);
    }

    public static void checkNBT(ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
    }

    public static Mode readModeFromNBT(ItemStack itemStack) {
        checkNBT(itemStack);
        return Mode.values()[itemStack.stackTagCompound.getByte(TAG_MODE)];
    }

    public static void writeModeToNBT(ItemStack itemStack, Mode mode) {
        checkNBT(itemStack);
        itemStack.getTagCompound().setByte(TAG_MODE, (byte) mode.ordinal());
    }

    public static Vec3Impl readPosFromNBT(ItemStack itemStack, String NBTTag) {
        checkNBT(itemStack);

        int[] components = itemStack.getTagCompound().getIntArray(NBTTag);

        return components.length == 3 ? new Vec3Impl(components) : null;
    }

    public static void writePosToNBT(ItemStack itemStack, Vec3Impl pos, String NBTTag) {
        checkNBT(itemStack);
        itemStack.getTagCompound().setIntArray(NBTTag, pos.components());
    }

    @Override
    public boolean onItemUseFirst(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        Vec3Impl pos = new Vec3Impl(x, y, z);
        doStuff(itemStack, world, pos, player);
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        doStuff(itemStack, world, null, player);
        return itemStack;
    }

    private void doStuff(ItemStack itemStack, World world, Vec3Impl pos, EntityPlayer player) {
        Mode mode = readModeFromNBT(itemStack);
        if (mode == Mode.Clear) {
            StructureUtility.LAST_NICE_CHARS_POINTER = 0;
            StructureUtility.GLOBAL_MAP.clear();
            player.addChatMessage(new ChatComponentText("Clearing Global Maps"));
        } else {
            if (pos == null) return;
            if (pos1 == null) {
                pos1 = pos;
                player.addChatMessage(new ChatComponentText("Set Position 1"));
                StructureLibAPI.hintParticleTinted(world, pos.get0(), pos.get1(), pos.get2(), getBlockHint(), 0, new short[]{255, 0, 255});
            } else if (pos2 == null) {
                pos2 = pos;
                player.addChatMessage(new ChatComponentText("Set Position 2"));
                StructureLibAPI.hintParticleTinted(world, pos.get0(), pos.get1(), pos.get2(), getBlockHint(), 1, new short[]{255, 0, 255});
            } else {
                writeStructure(itemStack, player,pos1,pos2,pos);
                player.addChatMessage(new ChatComponentText("Writing Structure To Logs"));
                StructureLib.proxy.clearHints(world);
                pos1 = null;
                pos2 = null;
            }
        }
    }

    private void writeStructure(ItemStack itemStack, EntityPlayer player,Vec3Impl pos1, Vec3Impl pos2, Vec3Impl posController) {

        if (pos1 == null || pos2 == null || posController == null) {
            return;
        }

        Box box = new Box(pos1, pos2);

        ExtendedFacing facing = StructureUtility.getExtendedFacingFromLookVector(player.getLookVec());

        player.addChatMessage(new ChatComponentText(facing.getName2()));
        StructureUtility.USE_GLOBAL_MAP = true;
        String structureDefinition = StructureUtility.getPseudoJavaCode(player.getEntityWorld(),
                facing,
                box,
                posController,
                player.isSneaking());

        StructureUtility.USE_GLOBAL_MAP = false;
        StructureLib.LOGGER.info(structureDefinition);
        StringSelection structureText = new StringSelection(structureDefinition);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(structureText, null);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister) {
        super.registerIcons(iconRegister);
        this.eraserIcon = iconRegister.registerIcon(MOD_ID + ":itemDebugStructureEraser");
    }

    @Override
    public IIcon getIconIndex(ItemStack itemStack) {
        return readModeFromNBT(itemStack) != Mode.Clear ? this.itemIcon : this.eraserIcon;
    }

    @Override
    public IIcon getIconFromDamage(int damage) {
        Mode mode = Mode.values()[damage];

        return mode != Mode.Clear ? this.itemIcon : this.eraserIcon;
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack) {
        return String.format("%s (%s)", super.getItemStackDisplayName(itemStack),
                StatCollector.translateToLocal("item.structurelib.debugStructureWriter.mode." + readModeFromNBT(itemStack).ordinal()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack itemStack, EntityPlayer player, List description, boolean p_77624_4_) {
        Mode mode = readModeFromNBT(itemStack);

        String modeString = String.format("%s (%s)", EnumChatFormatting.DARK_AQUA + StatCollector.translateToLocal("item.structurelib.debugStructureWriter.desc.0"),
                StatCollector.translateToLocal("item.structurelib.debugStructureWriter.mode." + mode.ordinal()));
        description.add(modeString);

        switch (mode) {
//            case SetCorners:
//                description.add(StatCollector.translateToLocal("item.structurelib.debugStructureWriter.desc.1"));
//                description.add("");
//                description.add(StatCollector.translateToLocal("item.structurelib.debugStructureWriter.desc.2"));
//                description.add(StatCollector.translateToLocal("item.structurelib.debugStructureWriter.desc.3"));
//                break;
//            case SetController:
//                description.add(StatCollector.translateToLocal("item.structurelib.debugStructureWriter.desc.4"));
//                break;
            case Build:
                description.add(StatCollector.translateToLocal("item.structurelib.debugStructureWriter.desc.5"));
                description.add(StatCollector.translateToLocal("item.structurelib.debugStructureWriter.desc.6"));
                description.add(StatCollector.translateToLocal("item.structurelib.debugStructureWriter.desc.7"));
                break;
//            case Refresh:
//                description.add(StatCollector.translateToLocal("item.structurelib.debugStructureWriter.desc.9"));
//                break;
            case Clear:
                description.add(StatCollector.translateToLocal("item.structurelib.debugStructureWriter.desc.10"));
                break;
        }
        description.add("");
        description.add(StatCollector.translateToLocal("item.structurelib.debugStructureWriter.desc.11"));
    }
}
