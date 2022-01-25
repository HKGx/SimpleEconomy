package com.hkgx.simple_economy;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

final public class SimpleEconomyCommands {
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("simple_economy").executes(SimpleEconomyCommands::simpleEconomy));

        dispatcher.register(
                literal("balance")
                        .executes(ctx -> balance(ctx, ctx.getSource().getPlayer()))
                        .then(
                                argument("player", EntityArgumentType.player())
                                        .executes(
                                                ctx -> balance(ctx,
                                                        EntityArgumentType.getPlayer(ctx, "player")))));
        dispatcher.register(
                literal("deposit")
                        .then(argument("amount", IntegerArgumentType.integer(1))
                                .executes(SimpleEconomyCommands::depositDiamonds)));
        dispatcher.register(
                literal("withdraw")
                        .then(argument("amount", IntegerArgumentType.integer(1))
                                .executes(SimpleEconomyCommands::depositDiamonds)));
    }

    public static int withdrawDiamonds(CommandContext<ServerCommandSource> context) {
        final var sender = context.getSource();
        final var amount = IntegerArgumentType.getInteger(context, "amount");
        final var text = Text.of(String.format("Withdrawing %d diamonds!", amount));
        sender.sendFeedback(text, true);
        return amount;
    }

    public static int depositDiamonds(CommandContext<ServerCommandSource> context) {
        final var sender = context.getSource();
        final var amount = IntegerArgumentType.getInteger(context, "amount");
        final var text = Text.of(String.format("Depositing %d diamonds!", amount));
        sender.sendFeedback(text, true);
        return 0;
    }

    public static int balance(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
        final var sender = context.getSource();
        final var text = Text
                .of(String.format("Balance for %s is %d!", player.getName().getString(), 0));
        sender.sendFeedback(text, true);
        return 0;
    }

    public static int simpleEconomy(CommandContext<ServerCommandSource> context) {
        final var sender = context.getSource();
        sender.sendFeedback(Text.of("fucc"), true);
        return 0;
    }
}
