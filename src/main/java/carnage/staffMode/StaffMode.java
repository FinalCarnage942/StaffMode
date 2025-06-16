package carnage.staffMode;

import carnage.staffMode.commands.StaffModeCommand;
import carnage.staffMode.listeners.CombatListener;
import carnage.staffMode.listeners.StaffModeListener;
import carnage.staffMode.listeners.VanishListener;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class StaffMode extends JavaPlugin {

    private final Set<UUID> staffModePlayers = new HashSet<>();
    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();
    private final Map<UUID, GameMode> savedGameModes = new HashMap<>();

    @Override
    public void onEnable() {
        this.getCommand("staffmode").setExecutor(new StaffModeCommand(this));

        getServer().getPluginManager().registerEvents(new StaffModeListener(this), this);
        getServer().getPluginManager().registerEvents(new VanishListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
    }

    public Set<UUID> getStaffModePlayers() { return staffModePlayers; }
    public Set<UUID> getVanishedPlayers() { return vanishedPlayers; }
    public Map<UUID, ItemStack[]> getSavedInventories() { return savedInventories; }
    public Map<UUID, ItemStack[]> getSavedArmor() { return savedArmor; }
    public Map<UUID, GameMode> getSavedGameModes() { return savedGameModes; }
}
