package com.gtnewhorizon.structurelib.commands;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.structure.StructureUtility;
import com.gtnewhorizon.structurelib.util.StructureData;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.*;

import static com.gtnewhorizon.structurelib.util.StructureData.StructureDataEntry;

public class CommandBuild extends SubCommand {
    public CommandBuild() {
        super("build");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            StructureDataEntry data = StructureData.data(sender);

            if (!data.isReady()) {
                if (data.corners()[0] == null) {
                    sender.addChatMessage(new ChatComponentTranslation("structurelib.command.build.error.0"));
                }

                if (data.corners()[1] == null) {
                    sender.addChatMessage(new ChatComponentTranslation("structurelib.command.build.error.1"));
                }

                if (data.controller() == null) {
                    sender.addChatMessage(new ChatComponentTranslation("structurelib.command.build.error.2"));
                }

                if (data.facing() == null) {
                    sender.addChatMessage(new ChatComponentTranslation("structurelib.command.build.error.3"));
                }

                return;
            }

            String structureDefinition = StructureUtility.getPseudoJavaCode(sender.getEntityWorld(),
                                                                            data.facing(),
                                                                            data.box(),
                                                                            data.controller(),
                                                                            false);

            StructureLib.LOGGER.info(structureDefinition);
        } else if (args.length == 1 && "help".equalsIgnoreCase(args[0])) {
            printHelp(sender, null);
        } else {
            throw new WrongUsageException(StatCollector.translateToLocal("structurelib.command.errorMessage"));
        }
    }

    @Override
    public void printHelp(ICommandSender sender, String command) {
        ChatStyle header = new ChatStyle().setColor(EnumChatFormatting.AQUA);
        sender.addChatMessage(new ChatComponentText("/structurelib build").setChatStyle(header));

        sender.addChatMessage(new ChatComponentTranslation("structurelib.command.build.desc.0"));

        ChatStyle requirements = new ChatStyle().setColor(EnumChatFormatting.RED);
        sender.addChatMessage(new ChatComponentText("structurelib.command.build.desc.1").setChatStyle(requirements));
        sender.addChatMessage(new ChatComponentText("/structurelib pos1"));
        sender.addChatMessage(new ChatComponentText("/structurelib pos2"));
        sender.addChatMessage(new ChatComponentText("/structurelib facing"));
    }
}
