package com.exemplo.peventos;

import com.exemplo.peventos.command.EventoCommand;
import com.exemplo.peventos.config.MessageManager;
import com.exemplo.peventos.data.EventoState;
import com.exemplo.peventos.gui.BaseMenu;
import com.exemplo.peventos.listener.MenuListener;
import com.exemplo.peventos.listener.PlayerListener;
import com.exemplo.peventos.listener.RulesListener;
import com.exemplo.peventos.manager.CooldownManager;
import com.exemplo.peventos.manager.EntradaManager;
import com.exemplo.peventos.manager.KitManager;
import com.exemplo.peventos.manager.PlayerManager;
import com.exemplo.peventos.manager.TimeManager;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public final class PEventos extends JavaPlugin {

    private MessageManager messages;
    private EventoState state;
    private TimeManager timeManager;
    private CooldownManager cooldownManager;
    private KitManager kitManager;
    private PlayerManager playerManager;
    private EntradaManager entradaManager;

    private final Set<BaseMenu> openMenus = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        messages = new MessageManager(this);
        state = new EventoState(this);
        timeManager = new TimeManager(this);
        cooldownManager = new CooldownManager(this);
        kitManager = new KitManager(this);
        playerManager = new PlayerManager(this);
        entradaManager = new EntradaManager(this);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new MenuListener(this), this);
        pm.registerEvents(new RulesListener(this), this);
        pm.registerEvents(new PlayerListener(this), this);

        PluginCommand command = getCommand("evento");
        if (command != null) {
            EventoCommand executor = new EventoCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        getLogger().info("pEventos ativado.");
    }

    @Override
    public void onDisable() {
        if (timeManager != null) timeManager.shutdown();
        if (cooldownManager != null) cooldownManager.shutdown();
    }

    public void resetEvent(CommandSender sender) {
        timeManager.shutdown();
        cooldownManager.shutdown();
        state.resetAll();
        playerManager.reviveAll();
        messages.broadcast("evento-resetado");
        refreshMenus();
    }

    public void refreshMenus() {
        for (BaseMenu menu : openMenus) {
            menu.render();
        }
    }

    public MessageManager messages() {
        return messages;
    }

    public EventoState state() {
        return state;
    }

    public TimeManager times() {
        return timeManager;
    }

    public CooldownManager cooldown() {
        return cooldownManager;
    }

    public KitManager kits() {
        return kitManager;
    }

    public PlayerManager players() {
        return playerManager;
    }

    public EntradaManager entrada() {
        return entradaManager;
    }

    public Set<BaseMenu> openMenus() {
        return openMenus;
    }
}
