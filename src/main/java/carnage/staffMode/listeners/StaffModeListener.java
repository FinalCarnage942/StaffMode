package carnage.staffMode.listeners;

import carnage.staffMode.StaffMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class StaffModeListener implements Listener {

    private final StaffMode plugin;
    private final Set<UUID> frozen = new HashSet<>();

    public StaffModeListener(StaffMode plugin) {
        this.plugin = plugin;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (plugin.getStaffModePlayers().contains(p.getUniqueId())) {
                        boolean vanished = plugin.getVanishedPlayers().contains(p.getUniqueId());
                        Component status = Component.text("Vanish: ", NamedTextColor.YELLOW)
                                .append(Component.text(vanished ? "✅" : "❌", vanished ? NamedTextColor.GREEN : NamedTextColor.RED));
                        p.sendActionBar(status);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;

        Player p = e.getPlayer();
        if (!plugin.getStaffModePlayers().contains(p.getUniqueId())) return;

        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.ENDER_EYE) return;

        switch (e.getAction()) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> vanish(p);
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> unvanish(p);
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getStaffModePlayers().contains(p.getUniqueId())) return;

        if (!(e.getRightClicked() instanceof Player target)) return;

        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.IRON_SHOVEL) return;

        e.setCancelled(true);
        freeze(target);
    }

    @EventHandler
    public void onLeftClick(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player staff)) return;
        if (!(e.getEntity() instanceof Player target)) return;
        if (!plugin.getStaffModePlayers().contains(staff.getUniqueId())) return;

        ItemStack item = staff.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.IRON_SHOVEL) return;

        e.setCancelled(true);
        unfreeze(target);
    }

    private void vanish(Player p) {
        UUID u = p.getUniqueId();
        if (plugin.getVanishedPlayers().add(u)) {
            for (Player o : Bukkit.getOnlinePlayers()) {
                if (!o.hasPermission("staffmode.use")) {
                    o.hidePlayer(plugin, p);
                }
            }
            p.setInvisible(true);
            p.sendMessage(Component.text("You are now vanished.", NamedTextColor.GREEN));
        }
    }

    private void unvanish(Player p) {
        UUID u = p.getUniqueId();
        if (plugin.getVanishedPlayers().remove(u)) {
            for (Player o : Bukkit.getOnlinePlayers()) {
                o.showPlayer(plugin, p);
            }
            p.setInvisible(false);
            p.sendMessage(Component.text("You are now visible.", NamedTextColor.RED));
        }
    }

    private void freeze(Player target) {
        UUID uuid = target.getUniqueId();
        if (frozen.add(uuid)) {
            target.getInventory().setHelmet(new ItemStack(Material.BLUE_ICE));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 255, false, false, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false, false));
            target.sendMessage(Component.text("You have been frozen by staff.", NamedTextColor.RED));
        }
    }

    private void unfreeze(Player target) {
        UUID uuid = target.getUniqueId();
        if (frozen.remove(uuid)) {
            target.getInventory().setHelmet(null);
            target.removePotionEffect(PotionEffectType.SLOWNESS);
            target.removePotionEffect(PotionEffectType.BLINDNESS);
            target.sendMessage(Component.text("You have been unfrozen.", NamedTextColor.GREEN));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (frozen.contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && frozen.contains(p.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player p && frozen.contains(p.getUniqueId())) {
            e.setCancelled(true);
        }
    }
}
