package com.exemplo.peventos.manager;

import com.exemplo.peventos.PEventos;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Locale;

public final class CooldownManager {

    private final PEventos plugin;
    private BossBar bar;
    private BukkitTask task;
    private int total;
    private int remaining;

    public CooldownManager(PEventos plugin) {
        this.plugin = plugin;
    }

    public boolean isRunning() {
        return task != null;
    }

    public int getRemaining() {
        return remaining;
    }

    public void start(CommandSender sender, int seconds) {
        int max = plugin.getConfig().getInt("cooldown.max-segundos", 86400);
        if (seconds < 1 || seconds > max) {
            plugin.messages().send(sender, "cooldown-invalido", "max", String.valueOf(max));
            return;
        }
        cleanup(true);

        total = seconds;
        remaining = seconds;
        bar = Bukkit.createBossBar(title(), color(), style());
        bar.setProgress(1.0);
        for (Player player : Bukkit.getOnlinePlayers()) {
            bar.addPlayer(player);
        }
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);

        plugin.messages().broadcast("cooldown-iniciado", "tempo", format(seconds));
    }

    public void cancel(CommandSender sender) {
        if (!isRunning()) {
            plugin.messages().send(sender, "cooldown-nao-ativo");
            return;
        }
        cleanup(true);
        plugin.messages().broadcast("cooldown-parado");
    }

    public void shutdown() {
        cleanup(true);
    }

    public void handleJoin(Player player) {
        if (bar != null && isRunning()) bar.addPlayer(player);
    }

    private void tick() {
        remaining--;
        if (remaining <= 0) {
            finish();
            return;
        }
        bar.setTitle(title());
        bar.setProgress(Math.max(0.0, Math.min(1.0, (double) remaining / total)));
    }

    private void finish() {
        final BossBar finished = bar;
        remaining = 0;
        finished.setTitle(title());
        finished.setProgress(0.0);
        task.cancel();
        task = null;
        bar = null;
        plugin.getServer().getScheduler().runTaskLater(plugin, finished::removeAll, 40L);
        plugin.messages().broadcast("cooldown-fim");
    }

    private void cleanup(boolean removeBar) {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (bar != null && removeBar) {
            bar.removeAll();
            bar = null;
        }
    }

    private String title() {
        return plugin.messages().msg("bossbar-titulo", "tempo", format(remaining));
    }

    private BarColor color() {
        try {
            return BarColor.valueOf(plugin.getConfig().getString("cooldown.bossbar.cor", "YELLOW")
                    .toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BarColor.YELLOW;
        }
    }

    private BarStyle style() {
        try {
            return BarStyle.valueOf(plugin.getConfig().getString("cooldown.bossbar.estilo", "SOLID")
                    .toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BarStyle.SOLID;
        }
    }

    public static String format(int totalSeconds) {
        int h = totalSeconds / 3600;
        int m = (totalSeconds % 3600) / 60;
        int s = totalSeconds % 60;
        return h > 0 ? String.format("%d:%02d:%02d", h, m, s) : String.format("%02d:%02d", m, s);
    }
}
