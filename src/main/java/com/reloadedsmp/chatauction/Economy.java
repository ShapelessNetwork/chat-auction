package com.reloadedsmp.chatauction;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Economy {

    private static net.milkbowl.vault.economy.Economy econ;

    public static boolean init(ChatAuction plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) return false;

        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (rsp == null) return false;

        econ = rsp.getProvider();
        return true;
    }

    public static Double getBalance(OfflinePlayer player) {
        return econ.getBalance(player);
    }

    public static boolean has(OfflinePlayer player, Double amount) {
        return econ.has(player, amount);
    }

    public static Double parseAmount(String amount) {
        try {
            return Double.valueOf(amount);
        } catch (NumberFormatException ignore) {
            return (double) 0;
        }
    }
}
