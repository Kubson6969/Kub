package com.kubson.mmoclases.data;

import com.kubson.mmoclases.MMOClases;
import com.kubson.mmoclases.api.CoreAttribute;
import com.kubson.mmoclases.api.PlayerClass;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/** Carga y guarda los datos en plugins/MMOClases/jugadores/&lt;uuid&gt;.yml */
public final class PlayerDataManager {

    private final MMOClases plugin;
    private final File folder;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    // Un solo hilo de escritura: los guardados de un mismo jugador nunca se desordenan.
    private final ExecutorService writer = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "MMOClases-guardado");
        thread.setDaemon(true);
        return thread;
    });

    public PlayerDataManager(MMOClases plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "jugadores");
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("No se pudo crear la carpeta " + folder);
        }
    }

    public PlayerData get(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), this::load);
    }

    public @Nullable PlayerData getIfLoaded(UUID uuid) {
        return cache.get(uuid);
    }

    public PlayerData load(UUID uuid) {
        PlayerData data = new PlayerData(uuid);
        File file = file(uuid);
        if (!file.exists()) {
            return data;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection classes = yaml.getConfigurationSection("clases");
        if (classes != null) {
            for (String key : classes.getKeys(false)) {
                PlayerClass playerClass = PlayerClass.fromId(key);
                ConfigurationSection section = classes.getConfigurationSection(key);
                if (playerClass == null || section == null) {
                    continue;
                }
                ClassProgress progress = data.progress(playerClass);
                progress.setLevel(section.getInt("nivel", 1));
                progress.setXp(section.getDouble("xp", 0));
                progress.setUnspentPoints(section.getInt("puntos", 0));
                ConfigurationSection attributes = section.getConfigurationSection("atributos");
                if (attributes != null) {
                    for (String attributeKey : attributes.getKeys(false)) {
                        CoreAttribute attribute = CoreAttribute.fromId(attributeKey);
                        if (attribute != null) {
                            progress.setAttribute(attribute, attributes.getInt(attributeKey));
                        }
                    }
                }
            }
        }
        String current = yaml.getString("clase");
        data.setCurrentClass(current == null ? null : PlayerClass.fromId(current));
        return data;
    }

    private String serialize(PlayerData data) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("clase", data.currentClass() == null ? null : data.currentClass().id());
        for (Map.Entry<PlayerClass, ClassProgress> entry : data.allProgress().entrySet()) {
            String base = "clases." + entry.getKey().id() + ".";
            ClassProgress progress = entry.getValue();
            yaml.set(base + "nivel", progress.level());
            yaml.set(base + "xp", progress.xp());
            yaml.set(base + "puntos", progress.unspentPoints());
            for (CoreAttribute attribute : CoreAttribute.values()) {
                int value = progress.attribute(attribute);
                if (value > 0) {
                    yaml.set(base + "atributos." + attribute.id(), value);
                }
            }
        }
        return yaml.saveToString();
    }

    /** Serializa en el hilo principal y escribe el archivo en otro hilo. */
    public void saveAsync(PlayerData data) {
        String content = serialize(data);
        File target = file(data.uuid());
        if (writer.isShutdown()) {
            write(target, content);
            return;
        }
        writer.execute(() -> write(target, content));
    }

    public void saveSync(PlayerData data) {
        write(file(data.uuid()), serialize(data));
    }

    public void saveAllAsync() {
        for (PlayerData data : cache.values()) {
            saveAsync(data);
        }
    }

    private void saveAllSync() {
        for (PlayerData data : cache.values()) {
            saveSync(data);
        }
    }

    /** Espera a que terminen las escrituras pendientes y guarda todo en el hilo actual. */
    public void shutdown() {
        writer.shutdown();
        try {
            if (!writer.awaitTermination(5, TimeUnit.SECONDS)) {
                plugin.getLogger().warning("Algunos guardados pendientes no terminaron a tiempo.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        saveAllSync();
    }

    public void unload(UUID uuid) {
        PlayerData data = cache.remove(uuid);
        if (data != null) {
            saveAsync(data);
        }
    }

    private void write(File target, String content) {
        try {
            Files.writeString(target.toPath(), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudieron guardar los datos en " + target, e);
        }
    }

    private File file(UUID uuid) {
        return new File(folder, uuid + ".yml");
    }
}
