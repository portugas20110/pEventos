package com.exemplo.peventos.model;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public enum EventoOption {

    PVP("pvp", Material.DIAMOND_SWORD, true),
    QUEBRAR("quebrar", Material.IRON_PICKAXE, true),
    COLOCAR("colocar", Material.GRASS_BLOCK, true),
    INTERACAO("interacao", Material.CHEST, true),
    QUEDA("queda", Material.FEATHER, true),
    FOME("fome", Material.COOKED_BEEF, true),
    DROPAR("dropar", Material.DROPPER, true),
    CHAT("chat", Material.WRITABLE_BOOK, true),
    MOVIMENTO("movimento", Material.LEATHER_BOOTS, true),
    ELIMINACAO("eliminacao", Material.SKELETON_SKULL, false);

    private final String key;
    private final Material icon;
    private final boolean defaultValue;

    EventoOption(String key, Material icon, boolean defaultValue) {
        this.key = key;
        this.icon = icon;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public Material getIcon() {
        return icon;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    public boolean isRule() {
        return this != ELIMINACAO;
    }

    public static List<EventoOption> rules() {
        List<EventoOption> list = new ArrayList<>();
        for (EventoOption option : values()) {
            if (option.isRule()) list.add(option);
        }
        return list;
    }
}
