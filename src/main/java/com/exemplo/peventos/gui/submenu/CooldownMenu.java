package com.exemplo.peventos.gui.submenu;

import com.exemplo.peventos.PEventos;
import com.exemplo.peventos.gui.BaseMenu;
import com.exemplo.peventos.gui.MainMenu;
import com.exemplo.peventos.manager.CooldownManager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;

public final class CooldownMenu extends BaseMenu {

    private static final int PRESET_BASE = 10;
    private static final int MAX_PRESETS = 5;
    private static final int STOP = 16;
    private static final int BACK = 22;

    public CooldownMenu(PEventos plugin) {
        super(plugin, 27, "cooldown");
    }

    private List<Integer> presets() {
        List<Integer> list = new ArrayList<>(plugin.getConfig().getIntegerList("cooldown.presets"));
        if (list.isEmpty()) {
            list = new ArrayList<>(List.of(30, 60, 120, 300, 600));
        }
        return list.size() > MAX_PRESETS ? list.subList(0, MAX_PRESETS) : list;
    }

    @Override
    public void render() {
        fill();
        String state = msg().text(plugin.cooldown().isRunning() ? "menu.cooldown-ativo" : "menu.cooldown-inativo");
        List<Integer> presets = presets();
        for (int i = 0; i < presets.size(); i++) {
            String time = CooldownManager.format(presets.get(i));
            inventory.setItem(PRESET_BASE + i, icon(Material.CLOCK, "cooldown.preset",
                    "tempo", time, "estado", state));
        }
        inventory.setItem(STOP, icon(Material.BARRIER, "cooldown.parar", "estado", state));
        inventory.setItem(BACK, backButton());
    }

    @Override
    public void handleClick(Player player, int slot, ClickType click) {
        if (slot == BACK) {
            navigate(player, () -> new MainMenu(plugin));
            return;
        }
        if (slot == STOP) {
            if (!allowed(player, "eventos.cooldown")) return;
            plugin.cooldown().cancel(player);
            done(player);
            return;
        }
        int index = slot - PRESET_BASE;
        List<Integer> presets = presets();
        if (index >= 0 && index < presets.size()) {
            if (!allowed(player, "eventos.cooldown")) return;
            plugin.cooldown().start(player, presets.get(index));
            done(player);
        }
    }
}
