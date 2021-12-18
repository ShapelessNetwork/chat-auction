package me.loaidev.chatauction;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

// we will use this class to store the bid's player and value and process refunds
public class Bid {

    protected UUID player;

    protected Double value;

    public Bid(UUID player, Double value) {
        this.player = player;
        this.value = value;
    }

    public UUID getPlayerUUID() {
        return player;
    }

    public Double getValue() {
        return value;
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(player);
    }

    public EconomyResponse refund() {
        OfflinePlayer offlinePlayer;
        // if value is non-positive or the player doesn't exit return (this should never happen)
        if (value <= 0 || (offlinePlayer = getOfflinePlayer()) == null) return null;
        // deposit the money to the player
        EconomyResponse response = Economy.deposit(offlinePlayer, value);
        // if the player is online we notify him about the refund
        Player player;
        if ((player = offlinePlayer.getPlayer()) != null) {
            if (response.transactionSuccess()) {
                player.sendMessage(Economy.format(value, NamedTextColor.GREEN).append(Component.text(" has been added to your account for your auction bids.")));
            } else {
                // notify player in case of an economy error
                player.sendMessage(Component.text(String.format("There was a problem while refunding your bid value. %s", response.errorMessage), NamedTextColor.RED));
            }
        }
        return response;
    }
}
