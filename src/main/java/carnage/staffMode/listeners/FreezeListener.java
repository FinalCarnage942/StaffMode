package carnage.staffMode.listeners;

import carnage.staffMode.StaffMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class FreezeListener implements Listener {

    private final StaffMode plugin;
    private final Set<UUID> frozen = new HashSet<>();
    private final Map<UUID, UUID> frozenBy = new HashMap<>();

    public FreezeListener(StaffMode plugin) {
        this.plugin = plugin;
    }

    public boolean isFrozen(UUID uuid) {
        return frozen.contains(uuid);
    }

    public Set<UUID> getFrozenPlayers() {
        return Collections.unmodifiableSet(frozen);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        Player staff = e.getPlayer();
        if (!plugin.getStaffModePlayers().contains(staff.getUniqueId())) return;
        if (!(e.getRightClicked() instanceof Player target)) return;

        ItemStack item = staff.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.IRON_SHOVEL) return;

        e.setCancelled(true);
        if (frozen.contains(target.getUniqueId())) {
            unfreeze(target);
        } else {
            freeze(target, staff);
        }
    }

    @EventHandler
    public void onLeftClick(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player staff)) return;
        if (!(e.getEntity() instanceof Player target)) return;
        if (!plugin.getStaffModePlayers().contains(staff.getUniqueId())) return;

        ItemStack item = staff.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.IRON_SHOVEL) return;

        e.setCancelled(true);
        if (frozen.contains(target.getUniqueId())) {
            unfreeze(target);
        } else {
            freeze(target, staff);
        }
    }

    private void freeze(Player target, Player staff) {
        UUID targetUUID = target.getUniqueId();

        if (frozen.add(targetUUID)) {
            frozenBy.put(targetUUID, staff.getUniqueId());

            // Clear any existing helmet
            target.getInventory().setHelmet(null);
            target.getInventory().setHelmet(new ItemStack(Material.BLUE_ICE));

            // Use reasonable potion amplifier
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 10, false, false, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false, false));

            target.sendMessage(Component.text("You have been frozen by staff.", NamedTextColor.RED));
            staff.sendMessage(Component.text("You have frozen " + target.getName(), NamedTextColor.GREEN));

            Bukkit.getLogger().info("[StaffMode] " + staff.getName() + " froze " + target.getName());
        }
    }

    private void unfreeze(Player target) {
        UUID targetUUID = target.getUniqueId();

        if (frozen.remove(targetUUID)) {
            UUID staffUUID = frozenBy.remove(targetUUID);

            target.getInventory().setHelmet(null);
            target.removePotionEffect(PotionEffectType.SLOWNESS);
            target.removePotionEffect(PotionEffectType.BLINDNESS);
            target.sendMessage(Component.text("You have been unfrozen.", NamedTextColor.GREEN));

            if (staffUUID != null) {
                Player staff = Bukkit.getPlayer(staffUUID);
                if (staff != null) {
                    staff.sendMessage(Component.text("You have unfrozen " + target.getName(), NamedTextColor.GREEN));
                }
            }

            Bukkit.getLogger().info("[StaffMode] " + target.getName() + " was unfrozen.");
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
        if (e.getEntity() instanceof Player player && frozen.contains(player.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player player && frozen.contains(player.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (frozen.contains(playerUUID)) {
            e.setCancelled(true);
            UUID staffUUID = frozenBy.get(playerUUID);
            Player staff = staffUUID != null ? Bukkit.getPlayer(staffUUID) : null;

            if (staff != null) {
                staff.sendMessage(Component.text(player.getName() + ": " + e.getMessage(), NamedTextColor.GRAY));
            }

            player.sendMessage(Component.text("You: " + e.getMessage(), NamedTextColor.DARK_GRAY));
        }
    }
}
