package com.kubson.mmoclasses.ability;

import com.kubson.mmoclasses.MMOClasses;
import com.kubson.mmoclasses.ability.impl.arquero.DisparoTriple;
import com.kubson.mmoclasses.ability.impl.arquero.FlechaExplosiva;
import com.kubson.mmoclasses.ability.impl.arquero.LluviaFlechas;
import com.kubson.mmoclasses.ability.impl.arquero.SaltoEvasivo;
import com.kubson.mmoclasses.ability.impl.clerigo.CirculoSanacion;
import com.kubson.mmoclasses.ability.impl.clerigo.EscudoDivino;
import com.kubson.mmoclasses.ability.impl.clerigo.JuicioCelestial;
import com.kubson.mmoclasses.ability.impl.clerigo.LuzSagrada;
import com.kubson.mmoclasses.ability.impl.guerrero.Carga;
import com.kubson.mmoclasses.ability.impl.guerrero.GolpeTitan;
import com.kubson.mmoclasses.ability.impl.guerrero.GritoGuerra;
import com.kubson.mmoclasses.ability.impl.guerrero.TajoGiratorio;
import com.kubson.mmoclasses.ability.impl.mago.BolaFuego;
import com.kubson.mmoclasses.ability.impl.mago.Meteoro;
import com.kubson.mmoclasses.ability.impl.mago.NovaEscarcha;
import com.kubson.mmoclasses.ability.impl.mago.Parpadeo;
import com.kubson.mmoclasses.api.PlayerClass;
import com.kubson.mmoclasses.api.Stat;
import com.kubson.mmoclasses.api.event.AbilityCastEvent;
import com.kubson.mmoclasses.data.PlayerData;
import com.kubson.mmoclasses.stats.PlayerStats;
import com.kubson.mmoclasses.util.Format;
import com.kubson.mmoclasses.util.Messages;
import com.kubson.mmoclasses.weapon.WeaponBridge;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/** Registro de habilidades y lógica común de lanzamiento (nivel, arma, enfriamiento, recurso). */
public final class AbilityManager {

    private final MMOClasses plugin;
    private final Map<String, Ability> registry = new LinkedHashMap<>();

    public AbilityManager(MMOClasses plugin) {
        this.plugin = plugin;
        register(new BolaFuego(), new Parpadeo(), new NovaEscarcha(), new Meteoro());
        register(new DisparoTriple(), new SaltoEvasivo(), new FlechaExplosiva(), new LluviaFlechas());
        register(new TajoGiratorio(), new Carga(), new GritoGuerra(), new GolpeTitan());
        register(new LuzSagrada(), new CirculoSanacion(), new EscudoDivino(), new JuicioCelestial());
    }

    private void register(Ability... abilities) {
        for (Ability ability : abilities) {
            registry.put(ability.id(), ability);
        }
    }

    public @Nullable Ability get(String id) {
        return registry.get(id);
    }

    public List<Ability> abilitiesOf(PlayerClass playerClass) {
        List<Ability> result = new ArrayList<>();
        for (String id : playerClass.abilityIds()) {
            Ability ability = registry.get(id);
            if (ability != null) {
                result.add(ability);
            }
        }
        return result;
    }

    private @Nullable ConfigurationSection section(Ability ability) {
        return plugin.settings().abilitySection(ability.id());
    }

    public int unlockLevel(Ability ability) {
        ConfigurationSection section = section(ability);
        return section == null ? ability.defaultLevel() : section.getInt("nivel", ability.defaultLevel());
    }

    public double cost(Ability ability) {
        ConfigurationSection section = section(ability);
        return section == null ? ability.defaultCost() : section.getDouble("coste", ability.defaultCost());
    }

    public double cooldownSeconds(Ability ability, PlayerStats stats) {
        ConfigurationSection section = section(ability);
        double base = section == null ? ability.defaultCooldown() : section.getDouble("enfriamiento", ability.defaultCooldown());
        return base * (1 - stats.get(Stat.REDUCCION_ENFRIAMIENTO) / 100.0);
    }

    /** Segundos restantes de enfriamiento (0 si está lista). */
    public double remainingCooldown(PlayerData data, Ability ability) {
        long remaining = data.cooldownEnd(ability.id()) - System.currentTimeMillis();
        return remaining <= 0 ? 0 : remaining / 1000.0;
    }

    /** Lanza la habilidad número {@code index} (0-3) de la clase del jugador. */
    public void cast(Player player, int index) {
        PlayerData data = plugin.data().get(player);
        PlayerClass playerClass = data.currentClass();
        if (playerClass == null) {
            plugin.messages().send(player, "sin-clase");
            return;
        }
        List<Ability> abilities = abilitiesOf(playerClass);
        if (index < 0 || index >= abilities.size()) {
            return;
        }
        Ability ability = abilities.get(index);

        int unlock = unlockLevel(ability);
        if (data.level() < unlock) {
            plugin.messages().send(player, "habilidad-bloqueada",
                    Messages.ph("habilidad", ability.name()), Messages.ph("nivel", unlock));
            return;
        }

        if (plugin.settings().castRequiresWeapon && !checkWeapon(player, data, playerClass)) {
            return;
        }

        double remaining = remainingCooldown(data, ability);
        if (remaining > 0) {
            plugin.messages().send(player, "enfriamiento",
                    Messages.ph("habilidad", ability.name()), Messages.ph("segundos", Format.number(Math.ceil(remaining * 10) / 10)));
            return;
        }

        PlayerStats stats = plugin.stats().compute(player, data);
        double cost = cost(ability);
        boolean overload = isArcaneOverload(playerClass, data);
        if (overload) {
            cost = 0;
        }
        if (data.resource() < cost) {
            plugin.messages().send(player, "recurso-insuficiente",
                    Messages.ph("recurso", playerClass.resource().displayName()), Messages.ph("coste", cost));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 0.6f);
            return;
        }

        AbilityCastEvent event = new AbilityCastEvent(player, playerClass, ability.id(), cost);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        cost = event.getCost();
        if (data.resource() < cost) {
            plugin.messages().send(player, "recurso-insuficiente",
                    Messages.ph("recurso", playerClass.resource().displayName()), Messages.ph("coste", cost));
            return;
        }

        boolean success;
        try {
            success = ability.cast(new AbilityContext(plugin, player, data, stats, section(ability)));
        } catch (RuntimeException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al lanzar " + ability.id(), e);
            success = false;
        }
        if (!success) {
            plugin.messages().send(player, "habilidad-fallo", Messages.ph("habilidad", ability.name()));
            return;
        }

        data.setResource(data.resource() - cost);
        data.setCooldownEnd(ability.id(), System.currentTimeMillis() + (long) (cooldownSeconds(ability, stats) * 1000));
        data.setSpellCounter(data.spellCounter() + 1);
        data.markCombat();
        if (overload) {
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1f, 1.6f);
            player.getWorld().spawnParticle(Particle.WITCH, player.getLocation().add(0, 1, 0), 25, 0.4, 0.6, 0.4, 0.1);
        }
    }

    /** Pasiva del mago: cada N hechizos, uno es gratis. */
    private boolean isArcaneOverload(PlayerClass playerClass, PlayerData data) {
        if (playerClass != PlayerClass.MAGO) {
            return false;
        }
        int every = (int) plugin.settings().passive(PlayerClass.MAGO, "cada-n-hechizos", 4);
        return every > 0 && (data.spellCounter() + 1) % every == 0;
    }

    private boolean checkWeapon(Player player, PlayerData data, PlayerClass playerClass) {
        WeaponBridge.Check check = plugin.weapons().check(player, data);
        switch (check.status()) {
            case VALID -> {
                return true;
            }
            case NONE -> plugin.messages().send(player, "arma-requerida",
                    Messages.ph("armas", String.join(", ", plugin.settings().classSettings(playerClass).weapons())));
            case WRONG_CLASS -> {
                PlayerClass owner = check.weapon() == null ? null : plugin.weapons().classFor(check.weapon());
                plugin.messages().send(player, "arma-clase-incorrecta",
                        Messages.ph("clase", owner == null ? Component.text("?") : Messages.className(owner)));
            }
            case LEVEL_TOO_LOW -> plugin.messages().send(player, "arma-nivel-bajo",
                    Messages.ph("nivel", check.weapon() == null ? 0 : check.weapon().requiredLevel()));
        }
        return false;
    }
}
