package com.reloadedsmp.chatauction;

import com.reloadedsmp.chatauction.tasks.AnnounceRemainingTask;
import com.reloadedsmp.chatauction.tasks.EndAuctionTask;
import com.reloadedsmp.chatauction.tasks.MinimalAnnounceRemainingTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.milkbowl.vault.economy.EconomyResponse;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// This contains most of the plugin's logic
public class Auction {

    // keep track of the scheduled tasks, we map the task's id (key) to the time remaining when it is fired
    public static Map<Integer, Integer> tasks = new HashMap<>();

    // item currently being auctioned
    protected static ItemStack currentItem = null;

    // uuid of the seller
    protected static UUID seller = null;

    // current highest bid (not present in the "bids")
    protected static Bid highestBid = null;

    // minimum bid required
    protected static Double minimumBid = null;

    // list of all players and their last bid (the highest bid isn't present here)
    protected static Map<UUID, Bid> bids = new HashMap<>();

    public static UUID getSellerUUID() {
        return seller;
    }

    public static boolean isActive() {
        return currentItem != null;
    }

    public static boolean startAuction(ItemStack item, Player player, Double price) {
        if (currentItem != null) return false;
        // set user, item and starting price
        seller = player.getUniqueId();
        currentItem = item;
        minimumBid = price;
        announceAuction(price, player); // announce the auction to players
        // create 3 scheduled tasks which will run when there are 30, 60 and 90 seconds remaining
        for (int i = 90; i > 0; i -= 30) {
            tasks.put(new AnnounceRemainingTask(i).runTaskLater(ChatAuction.instance, (120 - i) * 20L).getTaskId(), i);
        }
        // create 2 scheduled tasks which will run when there are 10 and 5 seconds remaining
        for (int i = 10; i > 0; i -= 5) {
            tasks.put(new MinimalAnnounceRemainingTask(i).runTaskLater(ChatAuction.instance, (120 - i) * 20L).getTaskId(), i);
        }
        // create task to end the auction
        tasks.put(new EndAuctionTask().runTaskLater(ChatAuction.instance, 120 * 20L).getTaskId(), 0);
        return true;
    }

    public static String placeBid(Double value, Player player) {
        if (highestBid != null && highestBid.getPlayerUUID().equals(player.getUniqueId()))
            return "You already have the highest bid!";
        if (value < minimumBid) return String.format("You must bid at least %s", Economy.format(minimumBid));

        // calculate how much a player needs to pay
        double payment = value;
        // if player already has a bid we will subtract the old bid from the new one to calculate the required payment
        if (bids.containsKey(player.getUniqueId())) {
            payment = value - (bids.get(player.getUniqueId()).getValue());
        }
        if (!Economy.has(player, payment)) return "You have insufficient balance to make that bid.";

        EconomyResponse response = Economy.withdraw(player, payment);
        // return any economy error
        if (!response.transactionSuccess()) return response.errorMessage;
        Bid bid = new Bid(player.getUniqueId(), value);

        // if there was a bid, move it to the normal bids and set the new bid to be the highest
        if (highestBid != null)
            bids.put(highestBid.getPlayerUUID(), highestBid);
        highestBid = bid;
        // new minimum bid will be at least 5% more than the new bid
        minimumBid = value * 1.05;
        // if the player had an old bid we will remove it since they have the highest bid now
        bids.remove(player.getUniqueId());

        // show withdrawn amount "-$50"
        player.sendMessage(Component.text(String.format("-%s", Economy.format(payment)), NamedTextColor.DARK_RED, TextDecoration.BOLD));
        // broadcast to everyone except the player that a bid was made
        // "[player] placed a bid of [value] on [item]. (minimum bid is now [minimum]"
        broadcastExcept(
                prefix().color(NamedTextColor.YELLOW)
                        .append(player.displayName().color(NamedTextColor.GREEN))
                        .append(Component.text(" placed a bid of ", NamedTextColor.YELLOW))
                        .append(Economy.format(value, NamedTextColor.GREEN))
                        .append(Component.text(" on "))
                        .append(itemComponent())
                        .append(Component.text(". (minimum bid is now "))
                        .append(Economy.format(minimumBid, NamedTextColor.GREEN))
                        .append(Component.text(")"))
                , player);
        // if the task that should run before auction ends by 10 seconds doesn't exist
        // (the auction ends in less than 10 seconds)
        // cancel all the tasks and create new tasks to make the auction end after 10 seconds from the newest bid
        if (!tasks.containsValue(10)) {
            tasks.forEach((id, time) -> Bukkit.getScheduler().cancelTask(id));
            tasks = new HashMap<>();
            tasks.put(new MinimalAnnounceRemainingTask(5).runTaskLater(ChatAuction.instance, 5 * 20L).getTaskId(), 5);
            tasks.put(new EndAuctionTask().runTaskLater(ChatAuction.instance, 10 * 20L).getTaskId(), 0);
        }
        return null;
    }

    public static void endAuction() {
        // refund all the bids that lost
        bids.values().forEach(Bid::refund);
        // get the seller
        OfflinePlayer offlineSeller = Bukkit.getOfflinePlayer(getSellerUUID());
        if (!offlineSeller.hasPlayedBefore()) return;
        Player seller = offlineSeller.getPlayer();

        // if no one bid on the item we will return the item and notify them if they are online
        if (highestBid == null) {
            AuctionStorage.put(currentItem, offlineSeller.getUniqueId());
            if (seller != null) {
                seller.sendMessage(Component.text("Your item wasn't sold. you can claim it by using /auction claim .", NamedTextColor.YELLOW));
            }
        } else {
            // give the item to the highest bidder
            AuctionStorage.put(currentItem, highestBid.getPlayerUUID());
            // deposit the money into the seller's account
            Economy.deposit(offlineSeller, highestBid.value);
            Player buyer = highestBid.getOfflinePlayer().getPlayer();
            // if the buyer is online, notify them
            if (buyer != null) {
                buyer.sendMessage(Component.text("You successfully bought ", NamedTextColor.YELLOW)
                        .append(itemComponent())
                        .append(Component.text(" for "))
                        .append(Economy.format(highestBid.getValue(), NamedTextColor.GREEN))
                        .append(Component.text(". You can claim it by using /auction claim")));
            }
            // if the seller is online, notify them
            if (seller != null) {
                seller.sendMessage(Component.text("Your item has been sold for ", NamedTextColor.YELLOW).append(Economy.format(highestBid.value, NamedTextColor.GREEN)));
                seller.sendMessage(Component.text(String.format("+%s", Economy.format(highestBid.value)), NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
            }
        }
        // broadcast the end of the auction
        Bukkit.broadcast(startLine());
        Bukkit.broadcast(Component.text("Auction ended.", NamedTextColor.YELLOW, TextDecoration.BOLD));
        Bukkit.broadcast(Component.text("Item: ", NamedTextColor.YELLOW).append(itemComponent()));
        // if no one bids
        if (highestBid == null) {
            Bukkit.broadcast(Component.text("There was no bids.", NamedTextColor.YELLOW));
        } else {
            // if there was a bidder
            Bukkit.broadcast(Component.text("Final Price: ", NamedTextColor.YELLOW).append(Component.text(Economy.format(highestBid.value), NamedTextColor.GREEN)));
        }
        Bukkit.broadcast(endLine());
        resetAuction();
    }

    public static void resetAuction() {
        // reset all the variables
        seller = null;
        currentItem = null;
        highestBid = null;
        bids = new HashMap<>();
        minimumBid = null;
        // cancel any tasks that still existed (if cancelled manually)
        tasks.forEach((id, time) -> Bukkit.getScheduler().cancelTask(id));
        tasks = new HashMap<>();
    }

    public static void broadcastExcept(Component message, Player player) {
        // broadcast to everyone and console except the specified player
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.equals(player))
                .forEach(p -> p.sendMessage(message));
        Bukkit.getConsoleSender().sendMessage(message);
    }

    public static void announceRemaining(Integer seconds) {
        // announce the long form of the time remaining (used in scheduled tasks)
        Bukkit.broadcast(startLine());
        Bukkit.broadcast(Component.text(seconds, NamedTextColor.GREEN, TextDecoration.BOLD).append(Component.text(" Seconds remaining", NamedTextColor.YELLOW)));
        Bukkit.broadcast(Component.text("Item: ", NamedTextColor.YELLOW).append(itemComponent()));
        if (highestBid == null) {
            Bukkit.broadcast(Component.text("Starting price: ", NamedTextColor.YELLOW).append(Component.text(Economy.format(minimumBid), NamedTextColor.GREEN)));
        } else {
            Bukkit.broadcast(Component.text("Current bid: ", NamedTextColor.YELLOW).append(Component.text(Economy.format(highestBid.value), NamedTextColor.GREEN)));
        }
        Bukkit.broadcast(endLine());
    }

    protected static void announceAuction(Double price, Player seller) {
        // announce the start of the auction
        Bukkit.broadcast(startLine());
        Bukkit.broadcast(Component.text("Item: ", NamedTextColor.YELLOW).append(itemComponent()));
        Bukkit.broadcast(Component.text("Seller: ", NamedTextColor.YELLOW).append(seller.displayName().color(NamedTextColor.GREEN)));
        Bukkit.broadcast(Component.text("Starting Price: ", NamedTextColor.YELLOW).append(Component.text(Economy.format(price), NamedTextColor.GREEN)));
        Bukkit.broadcast(endLine());
    }

    protected static Component startLine() {
        // first line that contains "Auction"
        Component line = Component.text(StringUtils.repeat(" ", 23), NamedTextColor.AQUA, TextDecoration.STRIKETHROUGH);
        return line.append(Component.text(" Auction ").decoration(TextDecoration.STRIKETHROUGH, false)).append(line);
    }

    protected static Component endLine() {
        // long line at the end of the message
        return Component.text(StringUtils.repeat(" ", 57), NamedTextColor.AQUA, TextDecoration.STRIKETHROUGH);
    }

    protected static Component itemComponent() {
        // returns the hoverable component of the current item.
        Component item = currentItem.displayName().hoverEvent(currentItem.asHoverEvent()).color(NamedTextColor.GRAY);
        if (currentItem.getAmount() > 1) {
            return item.append(Component.text(String.format("x%d", currentItem.getAmount())));
        }
        return item;
    }

    public static Component prefix() {
        // prefix in short messages, wrapped in an empty component so children don't inherit color or bold
        return Component.text().append(Component.text("Auction: ", NamedTextColor.AQUA, TextDecoration.BOLD)).asComponent();
    }

}
