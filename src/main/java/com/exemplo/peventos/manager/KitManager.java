package com.exemplo.peventos.manager;

import com.exemplo.peventos.PEventos;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class KitManager {

    private final PEventos plugin;
    private final File file;
    private YamlConfiguration yaml;

    public KitManager(PEventos plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "kit.yml");
        this.yaml = YamlConfiguration.loadConfiguration(file);
    }

    public boolean hasKit() {
        return yaml.getBoolean("salvo", false);
    }

    public void save(Player player) {
        YamlConfiguration fresh = new YamlConfiguration();
        PlayerInventory inv = player.getInventory();

        ItemStack[] storage = inv.getStorageContents();
        for (int i = 0; i < storage.length; i++) {
            if (storage[i] != null && storage[i].getType() != Material.AIR) {
                fresh.set("storage." + i, storage[i]);
            }
        }
        if (!plugin.times().isActive()) {
            ItemStack[] armor = inv.getArmorContents();
            for (int i = 0; i < armor.length; i++) {
                if (armor[i] != null && armor[i].getType() != Material.AIR) {
                    fresh.set("armor." + i, armor[i]);
                }
            }
        }
        ItemStack offhand = inv.getItemInOffHand();
        if (offhand.getType() != Material.AIR) {
            fresh.set("offhand", offhand);
        }
        fresh.set("salvo", true);

        yaml = fresh;
        try {
            plugin.getDataFolder().mkdirs();
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Não foi possível salvar o kit.yml", e);
        }
        plugin.messages().send(player, "kit-salvo");
    }

    public void apply(Player player, boolean keepArmor) {
        PlayerInventory inv = player.getInventory();

        ItemStack[] storage = new ItemStack[inv.getStorageContents().length];
        for (int i = 0; i < storage.length; i++) {
            storage[i] = yaml.getItemStack("storage." + i);
        }
        inv.setStorageContents(storage);

        ItemStack offhand = yaml.getItemStack("offhand");
        inv.setItemInOffHand(offhand == null ? new ItemStack(Material.AIR) : offhand);

        if (!keepArmor) {
            ItemStack[] armor = new ItemStack[4];
            for (int i = 0; i < armor.length; i++) {
                armor[i] = yaml.getItemStack("armor." + i);
            }
            inv.setArmorContents(armor);
        }
    }
}
