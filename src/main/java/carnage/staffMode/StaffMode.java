package carnage.staffMode;

import carnage.staffMode.commands.StaffChatCommand;
import carnage.staffMode.commands.StaffModeCommand;
import carnage.staffMode.listeners.*;
import me.neznamy.tab.api.TabAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class StaffMode extends JavaPlugin implements Listener {

    private final Set<UUID> staffModePlayers = new HashSet<>();
    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();
    private final Map<UUID, GameMode> savedGameModes = new HashMap<>();
    private StaffChatCommand staffChatCommand;

    @Override
    public void onEnable() {
        this.staffChatCommand = new StaffChatCommand(this);
        this.getCommand("staffmode").setExecutor(new StaffModeCommand(this));
        this.getCommand("staffchat").setExecutor(this.staffChatCommand);

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new StaffModeListener(this, this.staffChatCommand), this);
        getServer().getPluginManager().registerEvents(new VanishListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new RandomPlayerTpaListener(), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(this), this);
        getServer().getPluginManager().registerEvents(new StaffModePickupListener(this), this);

        registerPlaceholders();
        getLogger().info("StaffMode Enabled!");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (this.staffChatCommand.isStaffChatEnabled(playerUUID)) {
            e.setCancelled(true);
            String message = String.format("§7[§aStaffChat§7] §e%s§7: §f%s", player.getName(), e.getMessage());
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("staffmode.use")) {
                    p.sendMessage(message);
                }
            }
        }
    }

    private void registerPlaceholders() {
        TabAPI api = TabAPI.getInstance();
        api.getPlaceholderManager().registerServerPlaceholder("%online%", 1000, () ->
                String.valueOf((int) Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !vanishedPlayers.contains(p.getUniqueId()))
                        .count())
        );

        api.getPlaceholderManager().registerServerPlaceholder("%staffonline%", 1000, () ->
                String.valueOf((int) Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !vanishedPlayers.contains(p.getUniqueId()))
                        .filter(p -> p.hasPermission("tab.staff"))
                        .count())
        );
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("staffmode.use")) {
            enableStaffMode(player);
        } else if (player.hasPermission("staffmode.novanish")) {
            enableStaffModeWithoutVanish(player);
        }
    }

    public void enableStaffMode(Player player) {
        UUID uuid = player.getUniqueId();
        if (!staffModePlayers.contains(uuid)) {
            savedInventories.put(uuid, player.getInventory().getContents());
            savedArmor.put(uuid, player.getInventory().getArmorContents());
            savedGameModes.put(uuid, player.getGameMode());

            player.getInventory().clear();
            player.setGameMode(GameMode.CREATIVE);

            StaffModeCommand staffModeCommand = new StaffModeCommand(this);
            staffModeCommand.setupStaffInventory(player);

            staffModePlayers.add(uuid);
            vanishedPlayers.add(uuid);

            for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                if (!otherPlayer.hasPermission("staffmode.use")) {
                    otherPlayer.hidePlayer(this, player);
                }
            }
            player.setInvisible(true);
            player.sendMessage(Component.text("Staff Mode enabled and vanished."));
        }
    }

    public void enableStaffModeWithoutVanish(Player player) {
        UUID uuid = player.getUniqueId();
        if (!staffModePlayers.contains(uuid)) {
            savedInventories.put(uuid, player.getInventory().getContents());
            savedArmor.put(uuid, player.getInventory().getArmorContents());
            savedGameModes.put(uuid, player.getGameMode());

            player.getInventory().clear();
            player.setGameMode(GameMode.CREATIVE);

            StaffModeCommand staffModeCommand = new StaffModeCommand(this);
            staffModeCommand.setupStaffInventory(player);

            staffModePlayers.add(uuid);

            player.sendMessage(Component.text("Staff Mode enabled without vanish."));
        }
    }

    public Set<UUID> getStaffModePlayers() { return staffModePlayers; }
    public Set<UUID> getVanishedPlayers() { return vanishedPlayers; }
    public Map<UUID, ItemStack[]> getSavedInventories() { return savedInventories; }
    public Map<UUID, ItemStack[]> getSavedArmor() { return savedArmor; }
    public Map<UUID, GameMode> getSavedGameModes() { return savedGameModes; }
}
