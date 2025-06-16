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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FreezeListener implements Listener {

    private final StaffMode plugin;
    private final Set<UUID> frozen = new HashSet<>();
    private final Map<UUID, UUID> frozenBy = new HashMap<>();

    public FreezeListener(StaffMode plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        Player staff = e.getPlayer();
        if (!plugin.getStaffModePlayers().contains(staff.getUniqueId())) return;

        if (!(e.getRightClicked() instanceof Player target)) return;

        ItemStack item = staff.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.IRON_SHOVEL) return;

        e.setCancelled(true);
        freeze(target, staff);
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

    private void freeze(Player target, Player staff) {
        UUID targetUUID = target.getUniqueId();
        if (frozen.add(targetUUID)) {
            frozenBy.put(targetUUID, staff.getUniqueId());
            target.getInventory().setHelmet(new ItemStack(Material.BLUE_ICE));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 255, false, false, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false, false));
            target.sendMessage(Component.text("You have been frozen by staff.", NamedTextColor.RED));
            staff.sendMessage(Component.text("You have frozen " + target.getName(), NamedTextColor.GREEN));
        }
    }

    private void unfreeze(Player target) {
        UUID targetUUID = target.getUniqueId();
        if (frozen.remove(targetUUID)) {
            frozenBy.remove(targetUUID);
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
            Player staff = Bukkit.getPlayer(frozenBy.get(playerUUID));
            if (staff != null) {
                staff.sendMessage(Component.text(player.getName() + ": " + e.getMessage(), NamedTextColor.BLUE));
            }
        } else if (plugin.getStaffModePlayers().contains(playerUUID)) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (plugin.getStaffModePlayers().contains(p.getUniqueId()) || frozenBy.containsValue(p.getUniqueId())) {
                    p.sendMessage(Component.text(player.getName() + ": " + e.getMessage(), NamedTextColor.RED));
                }
            }
            e.setCancelled(true);
        }
    }
}
