package com.exemplo.peventos.gui;

import com.exemplo.peventos.PEventos;
import com.exemplo.peventos.config.MessageManager;
import com.exemplo.peventos.model.EventoOption;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public abstract class BaseMenu implements InventoryHolder {

    protected final PEventos plugin;
    protected final Inventory inventory;

    protected BaseMenu(PEventos plugin, int size, String titleKey) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, size, plugin.messages().text("menu.titulos." + titleKey));
    }

    public abstract void render();
    public abstract void handleClick(Player player, int slot, ClickType click);

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        render();
        plugin.openMenus().add(this);
        player.openInventory(inventory);
    }

    protected void navigate(Player player, Supplier<BaseMenu> next) {
        plugin.getServer().getScheduler().runTask(plugin, () -> next.get().open(player));
    }

    protected boolean allowed(Player player, String permission) {
        if (player.hasPermission(permission)) return true;
        plugin.messages().send(player, "sem-permissao");
        return false;
    }

    protected void done(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        plugin.refreshMenus();
    }

    protected MessageManager msg() {
        return plugin.messages();
    }


    protected void fill() {
        ItemStack filler = build(Material.GRAY_STAINED_GLASS_PANE, " ", Collections.emptyList());
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
    }

    protected ItemStack icon(Material material, String path, String... pairs) {
        return build(material,
                msg().text("menu.itens." + path + ".nome", pairs),
                msg().list("menu.itens." + path + ".descricao", pairs));
    }

    protected ItemStack build(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (!lore.isEmpty()) meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack backButton() {
        return icon(Material.ARROW, "voltar");
    }


    protected String stateText(boolean on) {
        return msg().text(on ? "menu.ativado" : "menu.desativado");
    }

    protected String optionName(EventoOption option) {
        return msg().text("menu.itens.opcoes." + option.getKey() + ".nome");
    }

    protected ItemStack optionIcon(EventoOption option) {
        String state = stateText(plugin.state().isEnabled(option));
        List<String> lore = new ArrayList<>(msg().list("menu.itens.opcoes." + option.getKey() + ".descricao"));
        lore.add("");
        lore.add(msg().text("menu.estado-linha", "estado", state));
        lore.add(msg().text("menu.dica-alternar"));
        return build(option.getIcon(), optionName(option), lore);
    }

    protected ItemStack optionState(EventoOption option) {
        boolean on = plugin.state().isEnabled(option);
        return build(on ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                msg().text("menu.estado-nome", "opcao", optionName(option), "estado", stateText(on)),
                Collections.singletonList(msg().text("menu.dica-alternar")));
    }

    protected void toggleOption(Player player, EventoOption option) {
        boolean enabled = plugin.state().toggle(option);
        msg().broadcast("opcao-alterada",
                "opcao", optionName(option),
                "estado", stateText(enabled),
                "jogador", player.getName());
    }
}
