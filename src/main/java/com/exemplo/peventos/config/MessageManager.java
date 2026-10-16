package com.exemplo.peventos.config;

import com.exemplo.peventos.PEventos;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageManager {

    private static final Pattern HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private final PEventos plugin;

    public MessageManager(PEventos plugin) {
        this.plugin = plugin;
    }

    public String color(String text) {
        if (text == null) return "";
        Matcher matcher = HEX.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(hex));
        }
        matcher.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    public String text(String path, String... pairs) {
        String raw = plugin.getConfig().getString(path, "&cTexto ausente: " + path);
        return apply(raw, pairs);
    }

    public List<String> list(String path, String... pairs) {
        List<String> result = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList(path)) {
            result.add(apply(line, pairs));
        }
        return result;
    }

    public String msg(String key, String... pairs) {
        return text("mensagens." + key, pairs);
    }

    public void send(CommandSender target, String key, String... pairs) {
        target.sendMessage(msg(key, pairs));
    }

    public void broadcast(String key, String... pairs) {
        String message = msg(key, pairs);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
        Bukkit.getConsoleSender().sendMessage(message);
    }

    private String apply(String raw, String... pairs) {
        String result = raw.replace("{prefixo}", plugin.getConfig().getString("mensagens.prefixo", ""));
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            result = result.replace("{" + pairs[i] + "}", pairs[i + 1]);
        }
        return color(result);
    }
}
