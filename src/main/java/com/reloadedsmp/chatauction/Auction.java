package com.reloadedsmp.chatauction;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Auction {

    protected static ItemStack item = null;

    protected static UUID player;

    public static boolean isActive() {
        return item != null;
    }

    public static boolean auctionItem(ItemStack item, UUID player, Double price) {
        Component itemComponent = item.displayName().hoverEvent(item.asHoverEvent()).color(NamedTextColor.AQUA);
        Bukkit.broadcast(Component.text(String.format("Item auctioned for %,.2f ", price)).color(NamedTextColor.GOLD).append(itemComponent));
        return true;
    }

}
