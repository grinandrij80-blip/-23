package com.summoncount;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Extends the vanilla /summon command with an optional count at the end:
 *     /summon zombie ~ ~ ~ 10
 *
 * How it works: Brigadier merges command nodes that have the same name, so the
 * "summon -> entity -> pos" nodes below are only placeholders. They are merged
 * into vanilla's nodes (which keep their own argument types, permissions and
 * suggestions) and only the new "count" node is added under vanilla's "pos".
 * When the count is given, the normal vanilla command (without the count)
 * is simply run that many times.
 */
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

                                // Text of this command only (works also inside /execute ... run summon ...)
                                String text = context.getRange().get(context.getInput()).stripTrailing();
                                String baseCommand = text.substring(0, text.lastIndexOf(' '));

                                CommandSourceStack source = context.getSource();
                                CommandSourceStack quiet = source.withSuppressedOutput();

                                for (int i = 0; i < count - 1; i++) {
                                    dispatcher.execute(baseCommand, quiet);
                                }
                                // The last one is not silent, so the player sees the vanilla message once.
                                dispatcher.execute(baseCommand, source);
                                return count;
                            })))));
    }
}
