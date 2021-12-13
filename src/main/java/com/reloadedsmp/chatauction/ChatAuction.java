package com.reloadedsmp.chatauction;

import com.reloadedsmp.chatauction.commands.AuctionCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatAuction extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!Economy.init(this)) {
            getLogger().severe("Disabled due to error in initializing vault economy");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.getCommand("auction").setExecutor(new AuctionCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
