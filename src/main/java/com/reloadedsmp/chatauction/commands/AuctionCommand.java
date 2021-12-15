package com.reloadedsmp.chatauction.commands;

import com.reloadedsmp.chatauction.Auction;
import com.reloadedsmp.chatauction.AuctionStorage;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AuctionCommand implements CommandExecutor {

    protected Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false; // can't use it from console
        if (args.length < 1) return invalid("Please specify a starting bid.", player); // missing argument

        // Logic to claim the stored auction items
        if (args[0].equals("claim")) {
            if (!AuctionStorage.exists(player.getUniqueId())) // return if player doesn't have items to claim
                return invalid("You don't have any items to claim", player);
            // get the player's items in storage
            List<ItemStack> items = AuctionStorage.get(player.getUniqueId());
            // attempt to add the items to the player's inventory
            Map<Integer, ItemStack> remaining = player.getInventory().addItem(items.toArray(new ItemStack[0]));
            if (remaining.isEmpty()) { // remove the storage if player has nothing else to claim.
                AuctionStorage.remove(player.getUniqueId());
                player.sendMessage(Component.text("Items claimed.", NamedTextColor.GREEN));
                return true;
            }
            // if the player didn't have enough space for the items, we will add them back to their storage.
            AuctionStorage.set(player.getUniqueId(), remaining.values());
            player.sendMessage(Component.text("Not all items were claimed.", NamedTextColor.YELLOW));
            return true;
        }
        // cooldown logic
        if (cooldowns.containsKey(player.getUniqueId())) {
            // get seconds left (15 minutes cooldown)
            long secondsLeft = ((cooldowns.get(player.getUniqueId()) / 1000) + 60 * 15) - (System.currentTimeMillis() / 1000);
            // if player is on cooldown, notify them.
            if (secondsLeft > 0) {
                return invalid("You can't use that command for another " + formatTime((int) secondsLeft), player);
            }
        }

        // don't allow 2 active auctions at once.
        if (Auction.isActive()) return invalid("There is currently an active auction.", player);
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            return invalid("Please hold the item that you want to auction in your hand.", player);
        }
        // parse the starting price provided by the player
        Double price;
        if ((price = Economy.parseValue(args[0])) <= 0) return invalid("Please enter a valid starting bid.", player);
        // get the itemStack and start the auction
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!Auction.startAuction(item, player, price)) {
            Auction.resetAuction();
            return invalid("There was a problem with creating the auction.", player);
        }
        // add player to the cooldown list
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        // finally, we remove the item from the player's hand
        player.getInventory().setItemInMainHand(null);
        return true;
    }

    protected boolean invalid(String message, CommandSender sender) {
        // return error to player and exit
        sender.sendMessage(Component.text(message, NamedTextColor.RED));
        return false;
    }

    protected String formatTime(int totalSeconds) {
        // This will format the message to "x Minute(s) y Second(s)"
        int secs = totalSeconds % 60;
        int minutes = totalSeconds / 60;
        StringBuilder builder = new StringBuilder(24);
        if (minutes != 0) {
            builder.append(minutes).append(" Minutes(s) ");
        }
        builder.append(secs).append(" Second(s) ");
        return builder.toString();
    }
}
