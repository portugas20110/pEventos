package com.exemplo.peventos.command;

import com.exemplo.peventos.PEventos;
import com.exemplo.peventos.gui.MainMenu;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class EventoCommand implements TabExecutor {

    private final PEventos plugin;

    public EventoCommand(PEventos plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            openMenu(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "menu" -> openMenu(sender);
            case "times" -> handleTimes(sender, args);
            case "cooldown" -> handleCooldown(sender, args);
            case "entrada" -> handleEntrada(sender, args);
            case "spawn" -> handleSpawn(sender, args);
            case "kit" -> handleKit(sender, args);
            case "curar" -> {
                if (has(sender, "eventos.gerir")) plugin.players().healAll(sender);
            }
            case "tpall" -> {
                if (!has(sender, "eventos.gerir")) return true;
                if (sender instanceof Player player) plugin.players().teleportAllTo(player);
                else plugin.messages().send(sender, "apenas-jogadores");
            }
            case "limpar" -> {
                if (has(sender, "eventos.gerir")) plugin.players().clearAll(sender);
            }
            case "vivos" -> {
                if (has(sender, "eventos.gerir")) plugin.players().listAlive(sender);
            }
            case "revive" -> handleRevive(sender, args);
            case "resetar" -> {
                if (has(sender, "eventos.gerir")) plugin.resetEvent(sender);
            }
            case "reload" -> {
                if (!has(sender, "eventos.reload")) return true;
                plugin.reloadConfig();
                plugin.messages().send(sender, "recarregado");
            }
            default -> plugin.messages().send(sender, "uso");
        }
        plugin.refreshMenus();
        return true;
    }

    private void openMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "apenas-jogadores");
            return;
        }
        if (!has(sender, "eventos.menu")) return;
        new MainMenu(plugin).open(player);
    }

    private void handleTimes(CommandSender sender, String[] args) {
        if (!has(sender, "eventos.times")) return;
        if (args.length < 2) {
            plugin.messages().send(sender, "uso");
            return;
        }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "ativar", "on" -> plugin.times().activate(sender);
            case "desativar", "off" -> plugin.times().deactivate(sender);
            case "sortear", "reshuffle" -> plugin.times().reshuffle(sender);
            default -> plugin.messages().send(sender, "uso");
        }
    }

    private void handleCooldown(CommandSender sender, String[] args) {
        if (!has(sender, "eventos.cooldown")) return;
        if (args.length < 2) {
            plugin.messages().send(sender, "uso");
            return;
        }
        String arg = args[1].toLowerCase(Locale.ROOT);
        if (arg.equals("parar") || arg.equals("stop") || arg.equals("cancelar")) {
            plugin.cooldown().cancel(sender);
            return;
        }
        try {
            plugin.cooldown().start(sender, Integer.parseInt(arg));
        } catch (NumberFormatException e) {
            plugin.messages().send(sender, "cooldown-invalido",
                    "max", String.valueOf(plugin.getConfig().getInt("cooldown.max-segundos", 86400)));
        }
    }

    private void handleEntrada(CommandSender sender, String[] args) {
        if (!has(sender, "eventos.entrada")) return;
        if (args.length < 2) {
            plugin.messages().send(sender, "uso");
            return;
        }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "abrir", "open" -> plugin.entrada().open(sender);
            case "fechar", "close" -> plugin.entrada().close(sender);
            default -> plugin.messages().send(sender, "uso");
        }
    }

    private void handleSpawn(CommandSender sender, String[] args) {
        if (!has(sender, "eventos.gerir")) return;
        if (args.length < 2) {
            plugin.messages().send(sender, "uso");
            return;
        }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "definir", "set" -> {
                if (sender instanceof Player player) plugin.players().setSpawn(player);
                else plugin.messages().send(sender, "apenas-jogadores");
            }
            case "ir", "tp" -> plugin.players().teleportAllToSpawn(sender);
            default -> plugin.messages().send(sender, "uso");
        }
    }

    private void handleKit(CommandSender sender, String[] args) {
        if (!has(sender, "eventos.gerir")) return;
        if (args.length < 2) {
            plugin.messages().send(sender, "uso");
            return;
        }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "salvar", "save" -> {
                if (sender instanceof Player player) plugin.kits().save(player);
                else plugin.messages().send(sender, "apenas-jogadores");
            }
            case "dar", "give" -> plugin.players().giveKit(sender);
            default -> plugin.messages().send(sender, "uso");
        }
    }

    private void handleRevive(CommandSender sender, String[] args) {
        if (!has(sender, "eventos.gerir")) return;
        if (args.length < 2) {
            plugin.messages().send(sender, "uso");
            return;
        }
        if (args[1].equalsIgnoreCase("todos") || args[1].equalsIgnoreCase("all")) {
            plugin.messages().send(sender, "todos-revividos",
                    "quantidade", String.valueOf(plugin.players().reviveAll()));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.messages().send(sender, "jogador-nao-encontrado", "jogador", args[1]);
            return;
        }
        plugin.players().revive(target);
    }

    private boolean has(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) return true;
        plugin.messages().send(sender, "sem-permissao");
        return false;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("menu", "times", "cooldown", "entrada", "spawn", "kit", "curar",
                    "tpall", "limpar", "revive", "vivos", "resetar", "reload"), args[0]);
        }
        if (args.length == 2) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "times":
                    return filter(List.of("ativar", "desativar", "sortear"), args[1]);
                case "cooldown":
                    return filter(List.of("parar", "10", "30", "60", "120", "300"), args[1]);
                case "entrada":
                    return filter(List.of("abrir", "fechar"), args[1]);
                case "spawn":
                    return filter(List.of("definir", "ir"), args[1]);
                case "kit":
                    return filter(List.of("salvar", "dar"), args[1]);
                case "revive":
                    List<String> names = new ArrayList<>();
                    names.add("todos");
                    for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
                    return filter(names, args[1]);
                default:
                    break;
            }
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String typed) {
        String lower = typed.toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.toLowerCase(Locale.ROOT).startsWith(lower)).toList();
    }
}
