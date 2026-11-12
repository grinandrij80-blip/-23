package com.summoncount;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(SummonCountMod.MOD_ID)
public class SummonCountMod {
    public static final String MOD_ID = "summoncount";
    public static final int MAX_COUNT = 500;

    public SummonCountMod() {
        RegisterCommandsEvent.BUS.addListener(SummonCountMod::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            LiteralArgumentBuilder.<CommandSourceStack>literal("summon")
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("entity", StringArgumentType.word())
                    .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("pos", StringArgumentType.word())
                        .then(RequiredArgumentBuilder.<CommandSourceStack, Integer>argument("count", IntegerArgumentType.integer(1, MAX_COUNT))
                            .executes(context -> {
                                int count = IntegerArgumentType.getInteger(context, "count");

                                String text = context.getRange().get(context.getInput()).stripTrailing();
                                String baseCommand = text.substring(0, text.lastIndexOf(' '));

                                CommandSourceStack source = context.getSource();
                                CommandSourceStack quiet = source.withSuppressedOutput();

                                for (int i = 0; i < count - 1; i++) {
                                    dispatcher.execute(baseCommand, quiet);
                                }
                                dispatcher.execute(baseCommand, source);
                                return count;
                            })))));
    }
}
