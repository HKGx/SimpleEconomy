package com.hkgx.simple_economy;

import static com.hkgx.simple_economy.EconomyComponents.BALANCE;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

final public class SimpleEconomyCommands {
    public static final Logger LOGGER = LoggerFactory.getLogger("simple_economy");

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
                                .executes(SimpleEconomyCommands::depositDiamonds))
                        .then(literal("all")
                                .executes(SimpleEconomyCommands::depositDiamondsAll)));
        dispatcher.register(
                literal("withdraw")
                        .then(argument("amount", IntegerArgumentType.integer(1))
                                .executes(SimpleEconomyCommands::withdrawDiamonds)));
        dispatcher.register(
                literal("pay")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> transfer(
                                                ctx.getSource().getPlayer(),
                                                IntegerArgumentType.getInteger(ctx, "amount"),
                                                EntityArgumentType.getPlayer(ctx, "player"))))));
    }

    public static int getDiamondsCount(PlayerInventory inventory) {
        var diamondsCount = 0;
        for (var itemStack : inventory.main) {
            if (itemStack.getItem() == Items.DIAMOND) {
                diamondsCount += itemStack.getCount();
            }
        }
        return diamondsCount;
    }

    public static int getDiamondBlocksCount(PlayerInventory inventory) {
        var blocksCount = 0;
        for (var itemStack : inventory.main) {
            if (itemStack.getItem() == Items.DIAMOND_BLOCK) {
                blocksCount += itemStack.getCount();
            }
        }
        return blocksCount;
    }

    public static synchronized int depositDiamonds(PlayerEntity player, int amount) {
        // TODO: autoconvert diamond blocks to diamonds
        var balance = BALANCE.get(player);
        final var inventory = player.getInventory();
        final var diamondsCount = getDiamondsCount(inventory);
        if (diamondsCount < amount) {
            player.sendSystemMessage(Text.of("You don't have enough diamonds!"), Util.NIL_UUID);
            return 1;
        }
        Inventories.remove(inventory, (is) -> is.getItem() == Items.DIAMOND, amount, false);
        balance.deposit(amount);
        return 0;
    }

    public static int depositDiamonds(CommandContext<ServerCommandSource> context) {
        final var sender = context.getSource();
        if (!(sender.getEntity() instanceof ServerPlayerEntity)) {
            return 1;
        }
        final var player = (ServerPlayerEntity) sender.getEntity();
        final var amount = IntegerArgumentType.getInteger(context, "amount");
        return depositDiamonds(player, amount);
    }

    public static int depositDiamondsAll(CommandContext<ServerCommandSource> context) {
        final var sender = context.getSource();
        if (!(sender.getEntity() instanceof ServerPlayerEntity)) {
            return 1;
        }
        final var player = (ServerPlayerEntity) sender.getEntity();
        final var count = getDiamondsCount(player.getInventory());
        return depositDiamonds(player, count);

    }

    public static int withdrawDiamonds(CommandContext<ServerCommandSource> context) {
        final var sender = context.getSource();
        if (!(sender.getEntity() instanceof ServerPlayerEntity)) {
            return 1;
        }
        final var player = (ServerPlayerEntity) sender.getEntity();
        final var amount = IntegerArgumentType.getInteger(context, "amount");
        var balance = BALANCE.get(player);
        if (balance.getBalance() < amount) {
            sender.sendError(Text.of("You don't have enough diamonds!"));
            return 1;
        }
        balance.withdraw(amount);
        final var blocks = amount / 9;
        final var remainder = amount % 9;
        final var diamondBlocksStack = new ItemStack(Items.DIAMOND_BLOCK, blocks);
        final var diamondStack = new ItemStack(Items.DIAMOND, remainder);
        player.getInventory().offerOrDrop(diamondBlocksStack);
        player.getInventory().offerOrDrop(diamondStack);
        final var text = Text
                .of(String.format("Withdrawn %d diamonds as %d blocks and %d diamonds!", amount, blocks, remainder));
        sender.sendFeedback(text, false);
        return amount;
    }

    public static int balance(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
        final var sender = context.getSource();
        final var bal = BALANCE.get(player).getBalance();
        final var text = Text
                .of(String.format("Balance for %s is %d!", player.getName().getString(), bal));
        sender.sendFeedback(text, true);
        return 0;
    }

    public static synchronized int transfer(PlayerEntity fromPlayer, int amount, PlayerEntity toPlayer) {
        var fromBalance = BALANCE.get(fromPlayer);
        var toBalance = BALANCE.get(toPlayer);
        if (fromBalance.getBalance() < amount) {
            fromPlayer.sendSystemMessage(Text.of("You don't have enough diamonds!"), Util.NIL_UUID);
            return 1;
        }
        final var toSenderMessage = new LiteralText("You paid ")
                .append(new LiteralText(String.valueOf(amount))
                        .setStyle(Style.EMPTY
                                .withItalic(true)
                                .withBold(true)))
                .append(new LiteralText(" diamonds to "))
                .append(((Entity) toPlayer).getDisplayName());

        fromPlayer.sendMessage(toSenderMessage, false);

        final var toRecipentMessage = fromPlayer.getDisplayName().shallowCopy()
                .append(new LiteralText(" has paid "))
                .append(new LiteralText(String.valueOf(amount))
                        .setStyle(Style.EMPTY
                                .withItalic(true)
                                .withBold(true)))
                .append(new LiteralText(" diamonds to you!"));

        toPlayer.sendSystemMessage(toRecipentMessage, Util.NIL_UUID);
        // Let's hope that it won't allow for race conditions!
        fromBalance.withdraw(amount);
        toBalance.deposit(amount);
        return 0;
    }

    public static int simpleEconomy(CommandContext<ServerCommandSource> context) {
        final var sender = context.getSource();
        sender.sendFeedback(Text.of("fucc"), true);
        return 0;
    }
}
