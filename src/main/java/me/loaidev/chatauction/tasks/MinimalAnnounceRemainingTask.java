package me.loaidev.chatauction.tasks;

import me.loaidev.chatauction.Auction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class MinimalAnnounceRemainingTask extends BukkitRunnable {

    protected Integer seconds;

    public MinimalAnnounceRemainingTask(Integer seconds) {
        this.seconds = seconds;
    }

    @Override
    public void run() {
        // announce time left in a simpler 1 line message and remove the task from the tasks list
        if (Auction.isActive()) {
            Bukkit.broadcast(Auction.prefix().color(NamedTextColor.YELLOW)
                    .append(Component.text("Ending in "))
                    .append(Component.text(seconds, NamedTextColor.GREEN))
                    .append(Component.text(" seconds.")));
            Auction.tasks.remove(this.getTaskId());
        }
    }
}
