package com.exemplo.peventos.gui;

import com.exemplo.peventos.PEventos;
import com.exemplo.peventos.gui.submenu.CooldownMenu;
import com.exemplo.peventos.gui.submenu.PlayersMenu;
import com.exemplo.peventos.gui.submenu.RulesMenu;
import com.exemplo.peventos.gui.submenu.TeamsMenu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Collections;

public final class MainMenu extends BaseMenu {

    private static final int RULES = 10;
    private static final int TEAMS = 12;
    private static final int COOLDOWN = 14;
    private static final int PLAYERS = 16;
    private static final int ENTRY = 22;
    private static final int ENTRY_STATE = 31;
    private static final int RESET = 34;

    public MainMenu(PEventos plugin) {
        super(plugin, 36, "principal");
    }

    @Override
    public void render() {
        fill();

        inventory.setItem(RULES, icon(Material.COMPARATOR, "principal.regras"));
        inventory.setItem(TEAMS, icon(Material.BLUE_BANNER, "principal.times",
                "estado", msg().text(plugin.times().isActive() ? "menu.times-ativos" : "menu.times-inativos")));
        inventory.setItem(COOLDOWN, icon(Material.CLOCK, "principal.cooldown",
                "estado", msg().text(plugin.cooldown().isRunning() ? "menu.cooldown-ativo" : "menu.cooldown-inativo")));
        inventory.setItem(PLAYERS, icon(Material.PLAYER_HEAD, "principal.jogadores",
                "vivos", String.valueOf(plugin.players().alive().size()),
                "eliminados", String.valueOf(plugin.players().eliminatedCount())));

        boolean open = plugin.entrada().isOpen();
        String entryText = msg().text(open ? "menu.entrada-aberta" : "menu.entrada-fechada");
        inventory.setItem(ENTRY, icon(Material.IRON_DOOR, "principal.entrada", "estado", entryText));
        inventory.setItem(ENTRY_STATE, build(
                open ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                msg().text("menu.estado-nome",
                        "opcao", msg().text("menu.itens.principal.entrada.nome"),
                        "estado", entryText),
                Collections.singletonList(msg().text("menu.dica-alternar"))));

        inventory.setItem(RESET, icon(Material.TNT, "principal.resetar"));
    }

    @Override
    public void handleClick(Player player, int slot, ClickType click) {
        switch (slot) {
            case RULES -> navigate(player, () -> new RulesMenu(plugin));
            case TEAMS -> navigate(player, () -> new TeamsMenu(plugin));
            case COOLDOWN -> navigate(player, () -> new CooldownMenu(plugin));
            case PLAYERS -> navigate(player, () -> new PlayersMenu(plugin));
            case ENTRY, ENTRY_STATE -> {
                if (!allowed(player, "eventos.entrada")) return;
                plugin.entrada().toggle(player);
                done(player);
            }
            case RESET -> {
                if (!allowed(player, "eventos.gerir")) return;
                if (!click.isShiftClick()) {
                    msg().send(player, "reset-confirmar");
                    return;
                }
                plugin.resetEvent(player);
                done(player);
            }
            default -> { }
        }
    }
}
