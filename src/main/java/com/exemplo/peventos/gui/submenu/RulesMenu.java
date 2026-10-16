package com.exemplo.peventos.gui.submenu;

import com.exemplo.peventos.PEventos;
import com.exemplo.peventos.gui.BaseMenu;
import com.exemplo.peventos.gui.MainMenu;
import com.exemplo.peventos.model.EventoOption;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public final class RulesMenu extends BaseMenu {

    private static final int ICON_ROW = 9;
    private static final int STATE_ROW = 18;
    private static final int BACK = 31;

    private final List<EventoOption> options = EventoOption.rules();

    public RulesMenu(PEventos plugin) {
        super(plugin, 36, "regras");
    }

    @Override
    public void render() {
        fill();
        for (int i = 0; i < options.size(); i++) {
            inventory.setItem(ICON_ROW + i, optionIcon(options.get(i)));
            inventory.setItem(STATE_ROW + i, optionState(options.get(i)));
        }
        inventory.setItem(BACK, backButton());
    }

    @Override
    public void handleClick(Player player, int slot, ClickType click) {
        if (slot == BACK) {
            navigate(player, () -> new MainMenu(plugin));
            return;
        }
        int index = -1;
        if (slot >= ICON_ROW && slot < ICON_ROW + 9) index = slot - ICON_ROW;
        else if (slot >= STATE_ROW && slot < STATE_ROW + 9) index = slot - STATE_ROW;

        if (index >= 0 && index < options.size()) {
            toggleOption(player, options.get(index));
            done(player);
        }
    }
}
