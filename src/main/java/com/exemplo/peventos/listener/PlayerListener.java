package com.exemplo.peventos.listener;

import com.exemplo.peventos.PEventos;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class PlayerListener implements Listener {

    private final PEventos plugin;

    public PlayerListener(PEventos plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.times().handleJoin(event.getPlayer());
        plugin.cooldown().handleJoin(event.getPlayer());
        plugin.players().handleJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.times().handleQuit(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        plugin.times().handleDeath(event);
        plugin.players().handleDeath(event.getEntity());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        plugin.times().handleRespawn(event.getPlayer());
        plugin.players().handleRespawn(event.getPlayer());
    }
}
