package com.exemplo.peventos.model;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public enum EventoTeam {

    AZUL("azul", Color.BLUE, ChatColor.BLUE),
    VERMELHO("vermelho", Color.RED, ChatColor.RED);

    private final String key;
    private final Color armorColor;
    private final ChatColor chatColor;

    EventoTeam(String key, Color armorColor, ChatColor chatColor) {
        this.key = key;
        this.armorColor = armorColor;
        this.chatColor = chatColor;
    }

    public String getKey() {
        return key;
    }

    public Color getArmorColor() {
        return armorColor;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }
}
