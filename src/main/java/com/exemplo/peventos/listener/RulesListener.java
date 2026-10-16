package com.exemplo.peventos.listener;

import com.exemplo.peventos.PEventos;
import com.exemplo.peventos.model.EventoOption;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RulesListener implements Listener {

    private static final String BYPASS = "eventos.bypass";
    private static final long MESSAGE_THROTTLE_MS = 1500L;

    private final PEventos plugin;
    private final Map<UUID, Long> lastNotice = new ConcurrentHashMap<>();

    public RulesListener(PEventos plugin) {
        this.plugin = plugin;
    }

    private boolean denied(Player player, EventoOption option) {
        return !plugin.state().isEnabled(option) && !player.hasPermission(BYPASS);
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPvp(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Player attacker = resolveAttacker(event);
        if (attacker == null || attacker.equals(victim)) return;

        if (!plugin.state().isEnabled(EventoOption.PVP)) {
            event.setCancelled(true);
            notice(attacker, "pvp-desativado");
            return;
        }
        if (plugin.times().isSameTeam(attacker, victim)) {
            event.setCancelled(true);
            notice(attacker, "fogo-amigo");
        }
    }

    private Player resolveAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) return player;
        if (event.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof Player shooter) {
            return shooter;
        }
        return null;
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!denied(event.getPlayer(), EventoOption.QUEBRAR)) return;
        event.setCancelled(true);
        notice(event.getPlayer(), "quebrar-desativado");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!denied(event.getPlayer(), EventoOption.COLOCAR)) return;
        event.setCancelled(true);
        notice(event.getPlayer(), "colocar-desativado");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!denied(event.getPlayer(), EventoOption.COLOCAR)) return;
        event.setCancelled(true);
        notice(event.getPlayer(), "colocar-desativado");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (!denied(event.getPlayer(), EventoOption.QUEBRAR)) return;
        event.setCancelled(true);
        notice(event.getPlayer(), "quebrar-desativado");
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!denied(player, EventoOption.INTERACAO)) return;

        switch (event.getAction()) {
            case PHYSICAL -> event.setCancelled(true);
            case RIGHT_CLICK_BLOCK -> {
                event.setUseInteractedBlock(Event.Result.DENY);
                ItemStack item = event.getItem();
                if (item != null && !item.getType().isBlock()) {
                    event.setUseItemInHand(Event.Result.DENY);
                }
                notice(player, "interacao-desativada");
            }
            case RIGHT_CLICK_AIR -> {
                event.setUseItemInHand(Event.Result.DENY);
                notice(player, "interacao-desativada");
            }
            default -> { }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!denied(event.getPlayer(), EventoOption.INTERACAO)) return;
        event.setCancelled(true);
        notice(event.getPlayer(), "interacao-desativada");
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFall(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL
                && event.getEntity() instanceof Player
                && !plugin.state().isEnabled(EventoOption.QUEDA)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHunger(FoodLevelChangeEvent event) {
        if (!plugin.state().isEnabled(EventoOption.FOME)
                && event.getFoodLevel() < event.getEntity().getFoodLevel()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (!denied(event.getPlayer(), EventoOption.DROPAR)) return;
        event.setCancelled(true);
        notice(event.getPlayer(), "dropar-desativado");
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!denied(player, EventoOption.CHAT)) return;
        event.setCancelled(true);
        plugin.getServer().getScheduler().runTask(plugin, () -> notice(player, "chat-desativado"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (plugin.state().isEnabled(EventoOption.MOVIMENTO)) return;
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) return;

        Player player = event.getPlayer();
        if (player.hasPermission(BYPASS) || player.getGameMode() == GameMode.SPECTATOR) return;

        Location back = from.clone();
        back.setYaw(to.getYaw());
        back.setPitch(to.getPitch());
        event.setTo(back);
        notice(player, "movimento-desativado");
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastNotice.remove(event.getPlayer().getUniqueId());
    }

    private void notice(Player player, String key) {
        long now = System.currentTimeMillis();
        Long last = lastNotice.get(player.getUniqueId());
        if (last != null && now - last < MESSAGE_THROTTLE_MS) return;
        lastNotice.put(player.getUniqueId(), now);
        plugin.messages().send(player, key);
    }
}
