package com.reloadedsmp.chatauction;

import com.reloadedsmp.chatauction.commands.AuctionCommand;
import com.reloadedsmp.chatauction.commands.AuctionEndCommand;
import com.reloadedsmp.chatauction.commands.BidCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatAuction extends JavaPlugin {

    public static ChatAuction instance;

    public static Configuration config;

    @Override
    public void onEnable() {
        String error = null;

        // initialize economy hooks
        if (!Economy.init(this)) {
            error = "Disabled due to error in initializing vault economy";
        }

        // initialize storage folder
        if (!AuctionStorage.init(this)) {
            error = "Disabled due to error in initializing item storage";
        }

        // log and disable on any error
        if (error != null) {
            getLogger().severe(error);
            getServer().getPluginManager().disablePlugin(this);
        }

        // register the commands
        this.getCommand("auction").setExecutor(new AuctionCommand());
        this.getCommand("bid").setExecutor(new BidCommand());
        this.getCommand("auctionend").setExecutor(new AuctionEndCommand());

        instance = this;

        // safe config and make it publicly accessible
        saveDefaultConfig();
        config = getConfig();
    }

    @Override
    public void onDisable() {
        // cancel all tasks when disabling
        getServer().getScheduler().cancelTasks(this);
    }
}
