package me.loaidev.chatauction.tasks;

import me.loaidev.chatauction.Auction;
import org.bukkit.scheduler.BukkitRunnable;

public class EndAuctionTask extends BukkitRunnable {

    @Override
    public void run() {
        // end the auction
        if (Auction.isActive()) {
            Auction.endAuction();
        }
    }
}
