package com.reloadedsmp.chatauction;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// manages the storage of unclaimed items
public class AuctionStorage {

    protected static File storageFolder;

    public static boolean init(ChatAuction plugin) {
        // create folder if doesn't exist
        storageFolder = new File(plugin.getDataFolder(), "storage");
        return (storageFolder.exists() || storageFolder.mkdirs());
    }

    // get the player's stored items by uuid
    public static List<ItemStack> get(UUID uuid) {
        return get(getConfig(uuid));
    }

    // get the stored items from a specific yaml file
    public static List<ItemStack> get(YamlConfiguration config) {
        // get the items and check if it's a list or return an emtpy one
        Object items = config.get("items");
        if (!(items instanceof List)) {
            return new ArrayList<>();
        }
        // cast each item data into an ItemStack
        return ((List<?>) items).stream().map(item -> (ItemStack) item).collect(Collectors.toList());
    }

    // add item to player's storage
    public static void put(ItemStack item, UUID uuid) {
        YamlConfiguration config = getConfig(uuid);
        List<ItemStack> items = get(config);
        items.add(item);
        set(uuid, items);
    }

    // set the contents of the player's storage
    public static void set(UUID uuid, Collection<ItemStack> items) {
        YamlConfiguration config = getConfig(uuid);
        config.set("items", items);
        try {
            config.save(getFile(uuid));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // delete the yaml file
    public static boolean remove(UUID uuid) {
        File file = getFile(uuid);
        if (file.exists()) {
            return getFile(uuid).delete();
        }
        return true;
    }

    // get the configuration object for a specific user
    protected static YamlConfiguration getConfig(UUID uuid) {
        return YamlConfiguration.loadConfiguration(getOrCreateFile(uuid));
    }

    public static File getOrCreateFile(UUID uuid) {
        File file = getFile(uuid);
        if (!file.exists()) {
            try {
                file.createNewFile();
                return file;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    // check if user has any items stored
    public static boolean exists(UUID uuid) {
        return getFile(uuid).exists();
    }

    // get the File object that represents a user's storage yaml file
    protected static File getFile(UUID uuid) {
        return new File(storageFolder, uuid.toString() + ".yml");
    }
}
