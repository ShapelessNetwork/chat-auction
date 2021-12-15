package com.reloadedsmp.chatauction.commands;

import com.reloadedsmp.chatauction.Auction;
import com.reloadedsmp.chatauction.Economy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BidCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false; // no console
        if (!Auction.isActive()) // check for active auction
            return invalid("There is no active auction.", player);
        if (Auction.getSellerUUID().equals(player.getUniqueId())) // don't allow the player to bid on their own auction
            return invalid("You can not bid on your own auction!", player);
        // parse the bid value provided by the player
        Double value;
        if (args.length < 1 || (value = Economy.parseValue(args[0])) <= 0) {
            return invalid("Please enter a valid number.", player);
        }
        // attempt to place the bid and return an error if it occurs
        String error;
        if ((error = Auction.placeBid(value, player)) != null)
            return invalid(error, player);

        player.sendMessage(Component.text("Successfully placed a bid of ", NamedTextColor.GREEN)
                .append(Component.text(Economy.format(value), NamedTextColor.GOLD)));
        return true;
    }

    protected boolean invalid(String message, CommandSender sender) {
        // return error to player and exit
        sender.sendMessage(Component.text(message, NamedTextColor.RED));
        return false;
    }
}
