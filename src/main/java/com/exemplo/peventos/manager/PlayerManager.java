package com.exemplo.peventos.manager;

import com.exemplo.peventos.PEventos;
import com.exemplo.peventos.model.EventoOption;
import com.exemplo.peventos.model.EventoTeam;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PlayerManager {

    private final PEventos plugin;
    private final Set<UUID> eliminated = new HashSet<>();

    public PlayerManager(PEventos plugin) {
        this.plugin = plugin;
    }


    public List<Player> participants() {
        List<Player> list = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            GameMode mode = player.getGameMode();
            if (mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE) {
                list.add(player);
            }
        }
        return list;
    }

    public List<Player> alive() {
        List<Player> list = new ArrayList<>();
        for (Player player : participants()) {
            if (!eliminated.contains(player.getUniqueId())) list.add(player);
        }
        return list;
    }

    public int eliminatedCount() {
        return eliminated.size();
    }


    @SuppressWarnings("deprecation")
    public void healAll(CommandSender sender) {
        for (Player player : participants()) {
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setSaturation(20f);
            player.setFireTicks(0);
        }
        plugin.messages().broadcast("curados");
    }

    public void teleportAllTo(Player target) {
        int count = 0;
        for (Player player : participants()) {
            if (player.equals(target)) continue;
            player.teleport(target.getLocation());
            count++;
        }
        plugin.messages().send(target, "teleportados-todos", "quantidade", String.valueOf(count));
    }

    public void setSpawn(Player player) {
        plugin.state().setSpawn(player.getLocation());
        plugin.messages().send(player, "spawn-definido");
    }

    public void teleportAllToSpawn(CommandSender sender) {
        Location spawn = plugin.state().getSpawn();
        if (spawn == null) {
            plugin.messages().send(sender, "spawn-nao-definido");
            return;
        }
        int count = 0;
        for (Player player : participants()) {
            player.teleport(spawn);
            count++;
        }
        plugin.messages().broadcast("teleportados-spawn", "quantidade", String.valueOf(count));
    }

    public void giveKit(CommandSender sender) {
        if (!plugin.kits().hasKit()) {
            plugin.messages().send(sender, "kit-nao-salvo");
            return;
        }
        boolean keepArmor = plugin.times().isActive();
        int count = 0;
        for (Player player : participants()) {
            plugin.kits().apply(player, keepArmor);
            count++;
        }
        plugin.messages().broadcast("kit-entregue", "quantidade", String.valueOf(count));
    }

    public void clearAll(CommandSender sender) {
        int count = 0;
        for (Player player : participants()) {
            player.getInventory().clear();
            plugin.times().reapplyArmor(player);
            count++;
        }
        plugin.messages().broadcast("inventarios-limpos", "quantidade", String.valueOf(count));
    }

    public void listAlive(CommandSender sender) {
        List<Player> alive = alive();
        String names = alive.isEmpty() ? "-" : alive.stream().map(Player::getName).collect(Collectors.joining(", "));
        plugin.messages().send(sender, "vivos-lista",
                "quantidade", String.valueOf(alive.size()),
                "eliminados", String.valueOf(eliminated.size()),
                "lista", names);
    }


    public void handleDeath(Player player) {
        if (!plugin.state().isEnabled(EventoOption.ELIMINACAO)) return;
        GameMode mode = player.getGameMode();
        if (mode != GameMode.SURVIVAL && mode != GameMode.ADVENTURE) return;

        eliminated.add(player.getUniqueId());
        plugin.messages().broadcast("jogador-eliminado",
                "jogador", player.getName(),
                "restantes", String.valueOf(alive().size()));
        checkWinner();
    }

    public void handleRespawn(Player player) {
        if (!eliminated.contains(player.getUniqueId())) return;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            player.setGameMode(GameMode.SPECTATOR);
            Location spawn = plugin.state().getSpawn();
            if (spawn != null) player.teleport(spawn);
        });
    }

    public void handleJoin(Player player) {
        if (!eliminated.contains(player.getUniqueId())) return;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) player.setGameMode(GameMode.SPECTATOR);
        });
    }

    @SuppressWarnings("deprecation")
    public void revive(Player player) {
        eliminated.remove(player.getUniqueId());
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        Location spawn = plugin.state().getSpawn();
        if (spawn != null) player.teleport(spawn);
        plugin.messages().send(player, "voce-revivido");
    }

    public int reviveAll() {
        int count = 0;
        for (UUID uuid : new ArrayList<>(eliminated)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                revive(player);
                count++;
            }
        }
        eliminated.clear();
        return count;
    }

    private void checkWinner() {
        List<Player> alive = alive();
        if (alive.isEmpty()) return;

        if (plugin.times().isActive()) {
            Set<EventoTeam> teams = alive.stream()
                    .map(plugin.times()::getTeam)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(EventoTeam.class)));
            if (teams.size() == 1) {
                EventoTeam winner = teams.iterator().next();
                plugin.messages().broadcast("vencedor-time", "time", plugin.times().getTeamName(winner));
            }
        } else if (alive.size() == 1) {
            plugin.messages().broadcast("vencedor-jogador", "jogador", alive.get(0).getName());
        }
    }
}
