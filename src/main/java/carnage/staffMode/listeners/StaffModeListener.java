package carnage.staffMode.listeners;

import carnage.staffMode.StaffMode;
import carnage.staffMode.commands.StaffChatCommand;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class StaffModeListener implements Listener {

    private final StaffMode plugin;
    private final StaffChatCommand staffChatCommand;

    public StaffModeListener(StaffMode plugin, StaffChatCommand staffChatCommand) {
        this.plugin = plugin;
        this.staffChatCommand = staffChatCommand;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    UUID uuid = p.getUniqueId();
                    if (plugin.getStaffModePlayers().contains(uuid)) {
                        boolean vanished = plugin.getVanishedPlayers().contains(uuid);
                        boolean staffChatEnabled = staffChatCommand.isStaffChatEnabled(uuid);

                        Component vanishStatus = Component.text("Vanish: ", NamedTextColor.YELLOW)
                                .append(Component.text(vanished ? "✅" : "❌", vanished ? NamedTextColor.GREEN : NamedTextColor.RED));

                        Component staffChatStatus = Component.text(" | StaffChat: ", NamedTextColor.YELLOW)
                                .append(Component.text(staffChatEnabled ? "✅" : "❌", staffChatEnabled ? NamedTextColor.GREEN : NamedTextColor.RED));

                        p.sendActionBar(vanishStatus.append(staffChatStatus));
                    } else {
                        p.sendActionBar(Component.empty());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);

        registerTabPlaceholders();
    }

    private void registerTabPlaceholders() {
        TabAPI tab = TabAPI.getInstance();
        PlaceholderManager pm = tab.getPlaceholderManager();

        pm.registerServerPlaceholder("%vanish_online%", 1000, () -> {
            long visiblePlayers = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !plugin.getVanishedPlayers().contains(p.getUniqueId()))
                    .count();
            return String.valueOf(visiblePlayers);
        });

        pm.registerServerPlaceholder("%vanish_staff_online%", 1000, () -> {
            long staffOnline = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("staffmode.use"))
                    .filter(p -> !plugin.getVanishedPlayers().contains(p.getUniqueId()))
                    .count();
            return String.valueOf(staffOnline);
        });

        pm.registerServerPlaceholder("%server_online%", 1000, () -> {
            long staffOnline = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("staffmode.use"))
                    .filter(p -> !plugin.getVanishedPlayers().contains(p.getUniqueId()))
                    .count();
            return String.valueOf(staffOnline);
        });

        pm.registerServerPlaceholder("%bungee_total%", 1000, () -> {
            long staffOnline = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("staffmode.use"))
                    .filter(p -> !plugin.getVanishedPlayers().contains(p.getUniqueId()))
                    .count();
            return String.valueOf(staffOnline);
        });
    }

    // Set of all interactable blocks you want to block for staffmode
    private static final Set<Material> BLOCKED_INTERACTABLES = EnumSet.of(
            // Doors - all types
            Material.OAK_DOOR, Material.BIRCH_DOOR, Material.SPRUCE_DOOR,
            Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR,
            Material.PALE_OAK_DOOR, Material.CRIMSON_DOOR, Material.WARPED_DOOR,

            // Trapdoors - all types
            Material.OAK_TRAPDOOR, Material.BIRCH_TRAPDOOR, Material.SPRUCE_TRAPDOOR,
            Material.JUNGLE_TRAPDOOR, Material.ACACIA_TRAPDOOR, Material.DARK_OAK_TRAPDOOR,
            Material.PALE_OAK_TRAPDOOR, Material.CRIMSON_TRAPDOOR, Material.WARPED_TRAPDOOR,

            // Fence gates - all types
            Material.OAK_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.SPRUCE_FENCE_GATE,
            Material.JUNGLE_FENCE_GATE, Material.ACACIA_FENCE_GATE, Material.DARK_OAK_FENCE_GATE, Material.PALE_OAK_FENCE_GATE,
            Material.CRIMSON_FENCE_GATE, Material.WARPED_FENCE_GATE,

            // Pressure plates - all types (wood, stone, weighted)
            Material.STONE_PRESSURE_PLATE, Material.OAK_PRESSURE_PLATE, Material.SPRUCE_PRESSURE_PLATE,
            Material.BIRCH_PRESSURE_PLATE, Material.JUNGLE_PRESSURE_PLATE, Material.ACACIA_PRESSURE_PLATE,
            Material.DARK_OAK_PRESSURE_PLATE, Material.CRIMSON_PRESSURE_PLATE, Material.WARPED_PRESSURE_PLATE,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE, Material.HEAVY_WEIGHTED_PRESSURE_PLATE, Material.PALE_OAK_PRESSURE_PLATE,

            // Buttons - wooden and stone
            Material.STONE_BUTTON, Material.OAK_BUTTON, Material.SPRUCE_BUTTON,
            Material.BIRCH_BUTTON, Material.JUNGLE_BUTTON, Material.ACACIA_BUTTON,
            Material.DARK_OAK_BUTTON, Material.CRIMSON_BUTTON, Material.WARPED_BUTTON, Material.PALE_OAK_BUTTON,

            // Levers
            Material.LEVER,

            Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL
    );

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!plugin.getStaffModePlayers().contains(uuid)) return;

        Block clickedBlock = e.getClickedBlock();
        if (clickedBlock != null) {
            Material mat = clickedBlock.getType();

            if (BLOCKED_INTERACTABLES.contains(mat)) {
                e.setCancelled(true);
                player.sendMessage(Component.text("You cannot interact with this while in staff mode.", NamedTextColor.RED));
                return;
            }
        }

        // Your existing vanish toggle logic with Ender Eye
        ItemStack item = e.getItem();
        if (item != null && item.getType() == Material.ENDER_EYE) {
            switch (e.getAction()) {
                case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> vanish(player);
                case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> unvanish(player);
            }
            e.setCancelled(true);
        }
    }

    private void vanish(Player player) {
        UUID uuid = player.getUniqueId();
        if (plugin.getVanishedPlayers().add(uuid)) {
            for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                if (!otherPlayer.hasPermission("staffmode.use")) {
                    otherPlayer.hidePlayer(plugin, player);
                }
            }
            player.setInvisible(true);
            player.sendMessage(Component.text("You are now vanished.", NamedTextColor.GREEN));
            Bukkit.broadcastMessage("§7[§c-§7] §c" + player.getName());
        }
    }

    private void unvanish(Player player) {
        UUID uuid = player.getUniqueId();
        if (plugin.getVanishedPlayers().remove(uuid)) {
            for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                otherPlayer.showPlayer(plugin, player);
            }
            player.setInvisible(false);
            player.sendMessage(Component.text("You are now un-vanished.", NamedTextColor.RED));
            Bukkit.broadcastMessage("§7[§a+§7] §a" + player.getName());
        }
    }
}
