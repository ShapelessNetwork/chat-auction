package com.reloadedsmp.chatauction.tasks;

import com.reloadedsmp.chatauction.Auction;
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
