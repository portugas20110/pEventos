package com.exemplo.peventos.manager;

import com.exemplo.peventos.PEventos;
import com.exemplo.peventos.config.MessageManager;
import com.exemplo.peventos.model.EventoTeam;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TimeManager {

    private final PEventos plugin;
    private final NamespacedKey armorKey;

    private final Map<UUID, EventoTeam> members = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();
    private final Map<EventoTeam, Team> boardTeams = new EnumMap<>(EventoTeam.class);
    private boolean active;

    public TimeManager(PEventos plugin) {
        this.plugin = plugin;
        this.armorKey = new NamespacedKey(plugin, "armadura_time");
    }

    public boolean isActive() {
        return active;
    }

    public EventoTeam getTeam(Player player) {
        return members.get(player.getUniqueId());
    }

    public boolean isSameTeam(Player a, Player b) {
        if (!active) return false;
        EventoTeam teamA = members.get(a.getUniqueId());
        return teamA != null && teamA == members.get(b.getUniqueId());
    }


    public void activate(CommandSender sender) {
        MessageManager msg = plugin.messages();
        if (active) {
            msg.send(sender, "times-ja-ativos");
            return;
        }
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        int min = plugin.getConfig().getInt("times.minimo-jogadores", 2);
        if (players.size() < min) {
            msg.send(sender, "times-poucos-jogadores", "minimo", String.valueOf(min));
            return;
        }

        active = true;
        setupBoardTeams();

        Collections.shuffle(players);
        for (int i = 0; i < players.size(); i++) {
            assign(players.get(i), i % 2 == 0 ? EventoTeam.AZUL : EventoTeam.VERMELHO);
        }

        msg.broadcast("times-ativados",
                "azul", String.valueOf(count(EventoTeam.AZUL)),
                "vermelho", String.valueOf(count(EventoTeam.VERMELHO)));
    }

    public void deactivate(CommandSender sender) {
        if (!active) {
            plugin.messages().send(sender, "times-nao-ativos");
            return;
        }
        reset();
        plugin.messages().broadcast("times-desativados");
    }

    public void shutdown() {
        if (active) reset();
    }

    private void reset() {
        for (UUID uuid : new ArrayList<>(members.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) release(player);
        }
        members.clear();
        savedArmor.clear();
        for (Team team : boardTeams.values()) {
            try {
                team.unregister();
            } catch (IllegalStateException ignored) {
                // já havia sido removido
            }
        }
        boardTeams.clear();
        active = false;
    }


    public void handleJoin(Player player) {
        if (!active) {
            stripTeamArmor(player);
            return;
        }
        if (members.containsKey(player.getUniqueId())) return;
        assign(player, count(EventoTeam.AZUL) <= count(EventoTeam.VERMELHO)
                ? EventoTeam.AZUL : EventoTeam.VERMELHO);
    }

    public void handleQuit(Player player) {
        if (active && members.containsKey(player.getUniqueId())) {
            release(player);
        }
    }

    public void handleDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!active || !members.containsKey(player.getUniqueId())) return;
        event.getDrops().removeIf(this::isTeamArmor);
        if (!event.getKeepInventory()) {
            savedArmor.put(player.getUniqueId(), new ItemStack[4]);
        }
    }

    public void handleRespawn(Player player) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            EventoTeam team = members.get(player.getUniqueId());
            if (active && team != null && player.isOnline()) {
                giveArmor(player, team);
            }
        });
    }


    public int getCount(EventoTeam team) {
        return (int) count(team);
    }

    public String getTeamName(EventoTeam team) {
        return teamName(team);
    }

    public void reshuffle(CommandSender sender) {
        if (!active) {
            plugin.messages().send(sender, "times-nao-ativos");
            return;
        }
        reset();
        activate(sender);
    }

    public void reapplyArmor(Player player) {
        EventoTeam team = members.get(player.getUniqueId());
        if (active && team != null) giveArmor(player, team);
    }

    private void assign(Player player, EventoTeam team) {
        UUID uuid = player.getUniqueId();
        members.put(uuid, team);
        savedArmor.putIfAbsent(uuid, copyArmor(player.getInventory().getArmorContents()));
        giveArmor(player, team);
        Team boardTeam = boardTeams.get(team);
        if (boardTeam != null) boardTeam.addEntry(player.getName());
        plugin.messages().send(player, "voce-no-time", "time", teamName(team));
    }

    private void release(Player player) {
        UUID uuid = player.getUniqueId();
        EventoTeam team = members.remove(uuid);
        ItemStack[] saved = savedArmor.remove(uuid);
        if (saved != null) {
            player.getInventory().setArmorContents(saved);
        } else {
            stripTeamArmor(player);
        }
        if (team != null) {
            Team boardTeam = boardTeams.get(team);
            if (boardTeam != null) boardTeam.removeEntry(player.getName());
        }
    }

    private void giveArmor(Player player, EventoTeam team) {
        PlayerInventory inv = player.getInventory();
        inv.setHelmet(piece(Material.LEATHER_HELMET, team));
        inv.setChestplate(piece(Material.LEATHER_CHESTPLATE, team));
        inv.setLeggings(piece(Material.LEATHER_LEGGINGS, team));
        inv.setBoots(piece(Material.LEATHER_BOOTS, team));
    }

    private ItemStack piece(Material material, EventoTeam team) {
        ItemStack item = new ItemStack(material);
        ItemMeta base = item.getItemMeta();
        if (base instanceof LeatherArmorMeta meta) {
            meta.setColor(team.getArmorColor());
            meta.setDisplayName(teamName(team));
            meta.setUnbreakable(true);
            meta.getPersistentDataContainer().set(armorKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private boolean isTeamArmor(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(armorKey, PersistentDataType.BYTE);
    }

    private void stripTeamArmor(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack[] armor = inv.getArmorContents();
        boolean changed = false;
        for (int i = 0; i < armor.length; i++) {
            if (isTeamArmor(armor[i])) {
                armor[i] = null;
                changed = true;
            }
        }
        if (changed) inv.setArmorContents(armor);
    }

    private ItemStack[] copyArmor(ItemStack[] source) {
        ItemStack[] copy = new ItemStack[source.length];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i] == null ? null : source[i].clone();
        }
        return copy;
    }

    private long count(EventoTeam team) {
        return members.values().stream().filter(t -> t == team).count();
    }

    private String teamName(EventoTeam team) {
        return plugin.messages().text("times." + team.getKey() + ".nome");
    }

    private void setupBoardTeams() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        Scoreboard board = manager.getMainScoreboard();
        for (EventoTeam team : EventoTeam.values()) {
            String name = "evento_" + team.getKey();
            Team old = board.getTeam(name);
            if (old != null) old.unregister();
            Team boardTeam = board.registerNewTeam(name);
            boardTeam.setAllowFriendlyFire(false);
            try {
                boardTeam.setColor(team.getChatColor());
            } catch (Throwable ignored) {
            }
            boardTeams.put(team, boardTeam);
        }
    }
}
