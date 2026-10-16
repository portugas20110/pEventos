package com.exemplo.peventos.manager;

import com.exemplo.peventos.PEventos;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class EntradaManager {

    private final PEventos plugin;

    public EntradaManager(PEventos plugin) {
        this.plugin = plugin;
    }

    public boolean isOpen() {
        return !Bukkit.hasWhitelist();
    }

    public void open(CommandSender sender) {
        Bukkit.setWhitelist(false);
        plugin.messages().broadcast("entrada-aberta");
    }

    public void close(CommandSender sender) {
        // Quem já está online entra na whitelist, para poder reconectar se cair.
        if (plugin.getConfig().getBoolean("entrada.adicionar-online-ao-fechar", true)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setWhitelisted(true);
            }
        }
        Bukkit.setWhitelist(true);
        plugin.messages().broadcast("entrada-fechada");
    }

    public void toggle(CommandSender sender) {
        if (isOpen()) {
            close(sender);
        } else {
            open(sender);
        }
    }
}
