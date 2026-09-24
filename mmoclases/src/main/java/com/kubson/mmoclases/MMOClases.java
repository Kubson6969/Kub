package com.kubson.mmoclases;

import com.kubson.mmoclases.ability.AbilityArrows;
import com.kubson.mmoclases.ability.AbilityManager;
import com.kubson.mmoclases.combat.CombatService;
import com.kubson.mmoclases.command.ClaseCommand;
import com.kubson.mmoclases.data.PlayerDataManager;
import com.kubson.mmoclases.data.ProgressionService;
import com.kubson.mmoclases.hud.HudTask;
import com.kubson.mmoclases.listener.CastModeListener;
import com.kubson.mmoclases.listener.CombatListener;
import com.kubson.mmoclases.listener.MenuListener;
import com.kubson.mmoclases.listener.PlayerListener;
import com.kubson.mmoclases.stats.StatService;
import com.kubson.mmoclases.util.Messages;
import com.kubson.mmoclases.weapon.WeaponBridge;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class MMOClases extends JavaPlugin {

    private static MMOClases instance;

    private Settings settings;
    private final Messages messages = new Messages();
    private PlayerDataManager dataManager;
    private WeaponBridge weapons;
    private StatService stats;
    private ProgressionService progression;
    private CombatService combat;
    private AbilityManager abilities;
    private AbilityArrows arrows;
    private HudTask hud;
    private CastModeListener castMode;
    private BukkitTask autosaveTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        dataManager = new PlayerDataManager(this);
        weapons = new WeaponBridge(this);
        stats = new StatService(this);
        progression = new ProgressionService(this);
        combat = new CombatService(this);
        abilities = new AbilityManager(this);
        arrows = new AbilityArrows(this);
        hud = new HudTask(this);
        castMode = new CastModeListener(this);
        reloadSettings();

        PlayerListener playerListener = new PlayerListener(this);
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(playerListener, this);
        pluginManager.registerEvents(castMode, this);
        pluginManager.registerEvents(new CombatListener(this), this);
        pluginManager.registerEvents(new MenuListener(), this);

        PluginCommand command = getCommand("clase");
        if (command != null) {
            ClaseCommand executor = new ClaseCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        getServer().getScheduler().runTaskTimer(this, hud, 20L, 10L);

        // Jugadores ya conectados (por ejemplo, tras un /reload).
        for (Player player : getServer().getOnlinePlayers()) {
            playerListener.setup(player, dataManager.get(player));
        }

        if (getServer().getPluginManager().getPlugin("MMOWeaponary") != null) {
            getLogger().info("MMOWeaponary detectado. Registra un WeaponProvider o usa las claves PDC de config.yml.");
        }
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.shutdown();
        }
        instance = null;
    }

    /** Relee config.yml. */
    public void reloadSettings() {
        reloadConfig();
        settings = new Settings(getConfig(), getLogger());
        messages.reload(getConfig());
        weapons.reload(getConfig());

        if (autosaveTask != null) {
            autosaveTask.cancel();
        }
        long period = Math.max(1, settings.autosaveMinutes) * 60L * 20L;
        autosaveTask = getServer().getScheduler().runTaskTimer(this, dataManager::saveAllAsync, period, period);
    }

    public static MMOClases get() {
        if (instance == null) {
            throw new IllegalStateException("MMOClases no está activado");
        }
        return instance;
    }

    public Settings settings() {
        return settings;
    }

    public Messages messages() {
        return messages;
    }

    public PlayerDataManager data() {
        return dataManager;
    }

    public WeaponBridge weapons() {
        return weapons;
    }

    public StatService stats() {
        return stats;
    }

    public ProgressionService progression() {
        return progression;
    }

    public CombatService combat() {
        return combat;
    }

    public AbilityManager abilities() {
        return abilities;
    }

    public AbilityArrows arrows() {
        return arrows;
    }

    public HudTask hud() {
        return hud;
    }

    public CastModeListener castMode() {
        return castMode;
    }
}
