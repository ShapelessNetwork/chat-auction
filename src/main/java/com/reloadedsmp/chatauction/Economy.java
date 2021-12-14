package com.reloadedsmp.chatauction;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

// this acts as a proxy between our plugin and vault's economy api
public class Economy {

    private static net.milkbowl.vault.economy.Economy econ;

    public static boolean init(ChatAuction plugin) {
        // register the vault hooks
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) return false;

        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (rsp == null) return false;

        econ = rsp.getProvider();
        return true;
    }

    public static boolean has(OfflinePlayer player, Double amount) {
        // check if player has at least an amount of money
        return econ.has(player, amount);
    }

    public static EconomyResponse withdraw(OfflinePlayer player, Double amount) {
        // remove money from the player's balance
        return econ.withdrawPlayer(player, amount);
    }

    public static EconomyResponse deposit(OfflinePlayer player, Double amount) {
        // deposit money into the player's balance
        return econ.depositPlayer(player, amount);
    }

    public static Double parseValue(String value) {
        // parse strings (player inputs) to double
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException ignore) {
            return (double) 0;
        }
    }

    public static String format(Double value) {
        // unified format for showing transactions "$20.00"
        return String.format("$%,.2f", value);
    }

    public static Component format(Double value, TextColor color) {
        // colored component of the formatted number
        return Component.text(format(value), color);
    }
}
