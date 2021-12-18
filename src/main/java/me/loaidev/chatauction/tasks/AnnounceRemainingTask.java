package me.loaidev.chatauction.tasks;

import me.loaidev.chatauction.Auction;
import org.bukkit.scheduler.BukkitRunnable;

public class AnnounceRemainingTask extends BukkitRunnable {

    protected Integer seconds;

    public AnnounceRemainingTask(Integer seconds) {
        this.seconds = seconds;
    }

    @Override
    public void run() {
        // announce time left and remove the task from the tasks list
        if (Auction.isActive()) {
            Auction.announceRemaining(seconds);
            Auction.tasks.remove(this.getTaskId());
        }
    }
}
