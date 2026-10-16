package com.exemplo.peventos.gui.submenu;

import com.exemplo.peventos.PEventos;
import com.exemplo.peventos.gui.BaseMenu;
import com.exemplo.peventos.gui.MainMenu;
import com.exemplo.peventos.model.EventoTeam;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public final class TeamsMenu extends BaseMenu {

    private static final int ACTIVATE = 10;
    private static final int DEACTIVATE = 12;
    private static final int RESHUFFLE = 14;
    private static final int INFO = 16;
    private static final int BACK = 22;

    public TeamsMenu(PEventos plugin) {
        super(plugin, 27, "times");
    }

    @Override
    public void render() {
        fill();
        String state = msg().text(plugin.times().isActive() ? "menu.times-ativos" : "menu.times-inativos");
        inventory.setItem(ACTIVATE, icon(Material.LIME_CONCRETE, "times.ativar", "estado", state));
        inventory.setItem(DEACTIVATE, icon(Material.RED_CONCRETE, "times.desativar", "estado", state));
        inventory.setItem(RESHUFFLE, icon(Material.ENDER_EYE, "times.sortear"));
        inventory.setItem(INFO, icon(Material.PAPER, "times.info",
                "azul", String.valueOf(plugin.times().getCount(EventoTeam.AZUL)),
                "vermelho", String.valueOf(plugin.times().getCount(EventoTeam.VERMELHO)),
                "estado", state));
        inventory.setItem(BACK, backButton());
    }

    @Override
    public void handleClick(Player player, int slot, ClickType click) {
        switch (slot) {
            case ACTIVATE -> {
                if (!allowed(player, "eventos.times")) return;
                plugin.times().activate(player);
                done(player);
            }
            case DEACTIVATE -> {
                if (!allowed(player, "eventos.times")) return;
                plugin.times().deactivate(player);
                done(player);
            }
            case RESHUFFLE -> {
                if (!allowed(player, "eventos.times")) return;
                plugin.times().reshuffle(player);
                done(player);
            }
            case BACK -> navigate(player, () -> new MainMenu(plugin));
            default -> { }
        }
    }
}
