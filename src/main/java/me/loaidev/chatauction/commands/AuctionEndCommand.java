package me.loaidev.chatauction.commands;

import me.loaidev.chatauction.Auction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class AuctionEndCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // only for admins to stop the current auction
        if (Auction.isActive()) {
            Auction.endAuction();
        }
        return true;
    }
}
