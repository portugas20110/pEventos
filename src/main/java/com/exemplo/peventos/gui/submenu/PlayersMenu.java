package com.exemplo.peventos.gui.submenu;

import com.exemplo.peventos.PEventos;
import com.exemplo.peventos.gui.BaseMenu;
import com.exemplo.peventos.gui.MainMenu;
import com.exemplo.peventos.model.EventoOption;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public final class PlayersMenu extends BaseMenu {

    private static final int HEAL = 10;
    private static final int TP_ALL = 11;
    private static final int SPAWN = 12;
    private static final int KIT = 13;
    private static final int REVIVE = 14;
    private static final int ALIVE = 15;
    private static final int CLEAR = 16;
    private static final int ELIMINATION = 22;
    private static final int ELIMINATION_STATE = 31;
    private static final int BACK = 35;

    public PlayersMenu(PEventos plugin) {
        super(plugin, 36, "jogadores");
    }

    @Override
    public void render() {
        fill();
        inventory.setItem(HEAL, icon(Material.GOLDEN_APPLE, "jogadores.curar"));
        inventory.setItem(TP_ALL, icon(Material.ENDER_PEARL, "jogadores.tpall"));
        inventory.setItem(SPAWN, icon(Material.COMPASS, "jogadores.spawn"));
        inventory.setItem(KIT, icon(Material.CHEST_MINECART, "jogadores.kit"));
        inventory.setItem(REVIVE, icon(Material.TOTEM_OF_UNDYING, "jogadores.reviver"));
        inventory.setItem(ALIVE, icon(Material.PLAYER_HEAD, "jogadores.vivos",
                "vivos", String.valueOf(plugin.players().alive().size()),
                "eliminados", String.valueOf(plugin.players().eliminatedCount())));
        inventory.setItem(CLEAR, icon(Material.LAVA_BUCKET, "jogadores.limpar"));
        inventory.setItem(ELIMINATION, optionIcon(EventoOption.ELIMINACAO));
        inventory.setItem(ELIMINATION_STATE, optionState(EventoOption.ELIMINACAO));
        inventory.setItem(BACK, backButton());
    }

    @Override
    public void handleClick(Player player, int slot, ClickType click) {
        if (slot == BACK) {
            navigate(player, () -> new MainMenu(plugin));
            return;
        }
        if (slot != HEAL && slot != TP_ALL && slot != SPAWN && slot != KIT && slot != REVIVE
                && slot != ALIVE && slot != CLEAR && slot != ELIMINATION && slot != ELIMINATION_STATE) {
            return;
        }
        if (!allowed(player, "eventos.gerir")) return;

        switch (slot) {
            case HEAL -> plugin.players().healAll(player);
            case TP_ALL -> plugin.players().teleportAllTo(player);
            case SPAWN -> {
                if (click.isRightClick()) plugin.players().setSpawn(player);
                else plugin.players().teleportAllToSpawn(player);
            }
            case KIT -> {
                if (click.isRightClick()) plugin.kits().save(player);
                else plugin.players().giveKit(player);
            }
            case REVIVE -> msg().send(player, "todos-revividos",
                    "quantidade", String.valueOf(plugin.players().reviveAll()));
            case ALIVE -> plugin.players().listAlive(player);
            case CLEAR -> {
                if (!click.isShiftClick()) {
                    msg().send(player, "limpar-confirmar");
                    return;
                }
                plugin.players().clearAll(player);
            }
            default -> toggleOption(player, EventoOption.ELIMINACAO);
        }
        done(player);
    }
}
