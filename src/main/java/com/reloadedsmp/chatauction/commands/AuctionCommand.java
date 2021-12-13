package com.reloadedsmp.chatauction.commands;

import com.reloadedsmp.chatauction.Auction;
import com.reloadedsmp.chatauction.Economy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AuctionCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (Auction.isActive()) return invalid("There is currently an active auction.", player);
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            return invalid("Please hold the item that you want to auction in your hand.", player);
        }
        if (args.length < 1) return invalid("Please specify a starting bid.", player);
        Double price;
        if ((price = Economy.parseAmount(args[0])) <= 0) return invalid("Please enter a valid starting bid", player);
        ItemStack item = player.getInventory().getItemInMainHand();
        Auction.auctionItem(item, player.getUniqueId(), price);
        player.getInventory().setItemInMainHand(null);
        return true;
    }

    protected boolean invalid(String message, CommandSender sender) {
        sender.sendMessage(Component.text(message).color(NamedTextColor.RED));
        return false;
    }
}
