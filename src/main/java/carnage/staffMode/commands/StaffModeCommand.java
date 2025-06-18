package carnage.staffMode.commands;

import carnage.staffMode.StaffMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

public class StaffModeCommand implements CommandExecutor {

    private final StaffMode plugin;

    public StaffModeCommand(StaffMode plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        UUID uuid = player.getUniqueId();

        if (!player.hasPermission("staffmode.use")) {
            player.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (!plugin.getStaffModePlayers().contains(uuid)) {
            enableStaffMode(player);
        } else {
            disableStaffMode(player);
        }

        return true;
    }

    public void setupStaffInventory(Player player) {
        if (player.hasPermission("staffmode.vanish")) {
            ItemStack vanishTool = new ItemStack(Material.ENDER_EYE);
            ItemMeta vanishMeta = vanishTool.getItemMeta();
            vanishMeta.displayName(Component.text("Toggle Vanish", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            vanishTool.setItemMeta(vanishMeta);
            player.getInventory().setItem(1, vanishTool);
        }

        if (player.hasPermission("staffmode.freeze")) {
            ItemStack freezeTool = new ItemStack(Material.IRON_SHOVEL);
            ItemMeta freezeMeta = freezeTool.getItemMeta();
            freezeMeta.displayName(Component.text("Freeze Player", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            freezeTool.setItemMeta(freezeMeta);
            player.getInventory().setItem(3, freezeTool);
        }

        if (player.hasPermission("staffmode.randomteleport")) {
            ItemStack randomTeleportTool = new ItemStack(Material.NETHER_STAR);
            ItemMeta teleportMeta = randomTeleportTool.getItemMeta();
            teleportMeta.displayName(Component.text("Random Teleport").decoration(TextDecoration.ITALIC, false));
            teleportMeta.lore(Arrays.asList(
                    Component.text("Right click to teleport", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                    Component.text("to a random player", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
            ));
            randomTeleportTool.setItemMeta(teleportMeta);
            player.getInventory().setItem(5, randomTeleportTool);
        }
    }

    private void enableStaffMode(Player player) {
        UUID uuid = player.getUniqueId();
        plugin.getSavedInventories().put(uuid, player.getInventory().getContents());
        plugin.getSavedArmor().put(uuid, player.getInventory().getArmorContents());
        plugin.getSavedGameModes().put(uuid, player.getGameMode());

        player.getInventory().clear();
        player.setGameMode(GameMode.CREATIVE);

        setupStaffInventory(player);

        plugin.getStaffModePlayers().add(uuid);

        if (player.hasPermission("staffmode.vanish")) {
            plugin.getVanishedPlayers().add(uuid);
            player.setInvisible(true);
            for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                if (!otherPlayer.hasPermission("staffmode.use")) {
                    otherPlayer.hidePlayer(plugin, player);
                }
            }
        }

        player.sendMessage(Component.text("Staff Mode enabled.", NamedTextColor.GREEN));
    }

    private void disableStaffMode(Player player) {
        UUID uuid = player.getUniqueId();
        player.getInventory().clear();
        if (plugin.getSavedInventories().containsKey(uuid)) {
            player.getInventory().setContents(plugin.getSavedInventories().remove(uuid));
        }
        if (plugin.getSavedArmor().containsKey(uuid)) {
            player.getInventory().setArmorContents(plugin.getSavedArmor().remove(uuid));
        }
        if (plugin.getSavedGameModes().containsKey(uuid)) {
            player.setGameMode(plugin.getSavedGameModes().remove(uuid));
        }

        plugin.getStaffModePlayers().remove(uuid);
        plugin.getVanishedPlayers().remove(uuid);
        player.setInvisible(false);
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            otherPlayer.showPlayer(plugin, player);
        }

        player.sendMessage(Component.text("Staff Mode disabled.", NamedTextColor.RED));
    }
}
