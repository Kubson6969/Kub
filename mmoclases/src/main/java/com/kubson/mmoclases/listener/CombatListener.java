package com.kubson.mmoclases.listener;

import com.kubson.mmoclases.MMOClases;
import com.kubson.mmoclases.ability.AbilityArrows;
import com.kubson.mmoclases.ability.impl.arquero.FlechaExplosiva;
import com.kubson.mmoclases.api.PlayerClass;
import com.kubson.mmoclases.api.ResourceType;
import com.kubson.mmoclases.api.Stat;
import com.kubson.mmoclases.combat.CombatService;
import com.kubson.mmoclases.data.PlayerData;
import com.kubson.mmoclases.stats.PlayerStats;
import com.kubson.mmoclases.weapon.WeaponBridge;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;

/** Aplica stats al combate normal: daño, críticos, defensa, robo de vida, furia y pasivas. */
public final class CombatListener implements Listener {

    private final MMOClases plugin;

    public CombatListener(MMOClases plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        CombatService combat = plugin.combat();
        Entity damager = event.getDamager();
        AbilityArrows arrows = plugin.arrows();

        if (damager instanceof Projectile projectile && arrows.baseDamage(projectile) != null) {
            // Flechas de habilidad: nunca a aliados, y las explosivas solo dañan con la explosión.
            boolean friendly = !(projectile.getShooter() instanceof Player shooter) || !combat.isEnemy(shooter, event.getEntity());
            if (friendly || arrows.explosiveRadius(projectile) != null) {
                event.setCancelled(true);
                return;
            }
        }

        if (!combat.isApplyingAbilityDamage() && event.getEntity() instanceof LivingEntity victim) {
            if (damager instanceof Player attacker
                    && (event.getCause() == DamageCause.ENTITY_ATTACK || event.getCause() == DamageCause.ENTITY_SWEEP_ATTACK)) {
                onMeleeHit(event, attacker, victim);
            } else if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player attacker) {
                onProjectileHit(event, attacker, projectile, victim);
            }
        }

        if (event.getEntity() instanceof Player victim) {
            onPlayerDamaged(event, victim);
        }
    }

    private void onMeleeHit(EntityDamageByEntityEvent event, Player attacker, LivingEntity victim) {
        PlayerData data = plugin.data().get(attacker);
        if (!data.hasClass()) {
            return;
        }
        PlayerStats stats = plugin.stats().compute(attacker, data);
        double damage = event.getDamage() * stats.multiplier(Stat.DANO_FISICO);
        WeaponBridge.Status weapon = plugin.weapons().check(attacker, data).status();
        if (weapon == WeaponBridge.Status.WRONG_CLASS || weapon == WeaponBridge.Status.LEVEL_TOO_LOW) {
            damage *= plugin.settings().wrongWeaponMultiplier;
        }
        if (plugin.combat().rollCrit(stats)) {
            damage *= plugin.combat().critMultiplier(stats);
            plugin.combat().critEffect(attacker, victim);
        }
        event.setDamage(damage);

        double lifesteal = stats.get(Stat.ROBO_VIDA);
        if (lifesteal > 0) {
            double max = plugin.stats().maxHealth(attacker);
            attacker.setHealth(Math.min(max, attacker.getHealth() + damage * lifesteal / 100.0));
        }
        data.markCombat();
        if (data.currentClass() != null && data.currentClass().resource() == ResourceType.FURIA
                && event.getCause() == DamageCause.ENTITY_ATTACK) {
            addRage(data, stats, plugin.settings().rageOnHit);
        }
    }

    private void onProjectileHit(EntityDamageByEntityEvent event, Player attacker, Projectile projectile, LivingEntity victim) {
        PlayerData data = plugin.data().get(attacker);
        if (!data.hasClass()) {
            return;
        }
        PlayerStats stats = plugin.stats().compute(attacker, data);
        Double abilityDamage = plugin.arrows().baseDamage(projectile);
        double damage = (abilityDamage != null ? abilityDamage : event.getDamage()) * stats.multiplier(Stat.DANO_DISTANCIA);

        // Pasiva del arquero: Ojo de Halcón.
        if (data.currentClass() == PlayerClass.ARQUERO && attacker.getWorld().equals(victim.getWorld())) {
            double perBlock = plugin.settings().passive(PlayerClass.ARQUERO, "bono-por-bloque", 1.5);
            double maxBonus = plugin.settings().passive(PlayerClass.ARQUERO, "bono-maximo", 45);
            double bonus = Math.min(maxBonus, perBlock * attacker.getLocation().distance(victim.getLocation()));
            damage *= 1 + bonus / 100.0;
        }
        if (plugin.combat().rollCrit(stats)) {
            damage *= plugin.combat().critMultiplier(stats);
            plugin.combat().critEffect(attacker, victim);
        }
        event.setDamage(damage);
        data.markCombat();
    }

    private void onPlayerDamaged(EntityDamageByEntityEvent event, Player victim) {
        PlayerData data = plugin.data().get(victim);
        PlayerClass playerClass = data.currentClass();
        if (playerClass == null) {
            return;
        }
        PlayerStats stats = plugin.stats().compute(victim, data);
        double damage = event.getDamage();
        double defense = stats.get(Stat.DEFENSA);
        if (defense > 0) {
            damage *= 1 - defense / (defense + plugin.settings().defenseConstant);
        }
        // Pasiva del guerrero: Voluntad de Hierro.
        if (playerClass == PlayerClass.GUERRERO) {
            double threshold = plugin.settings().passive(PlayerClass.GUERRERO, "umbral-vida", 35);
            double healthPercent = victim.getHealth() / plugin.stats().maxHealth(victim) * 100;
            if (healthPercent <= threshold) {
                damage *= 1 - plugin.settings().passive(PlayerClass.GUERRERO, "reduccion", 25) / 100.0;
            }
        }
        event.setDamage(damage);
        data.markCombat();
        if (playerClass.resource() == ResourceType.FURIA) {
            addRage(data, stats, plugin.settings().rageOnDamaged);
        }
    }

    private static void addRage(PlayerData data, PlayerStats stats, double amount) {
        data.setResource(Math.min(stats.get(Stat.RECURSO_MAXIMO), data.resource() + amount));
    }

    @EventHandler
    public void onProjectileLand(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        AbilityArrows arrows = plugin.arrows();
        if (arrows.baseDamage(projectile) == null) {
            return;
        }
        // Sin frames de invulnerabilidad para que la lluvia de flechas pueda golpear seguido.
        if (event.getHitEntity() instanceof LivingEntity living) {
            living.setNoDamageTicks(0);
        }
        Double radius = arrows.explosiveRadius(projectile);
        if (radius != null && projectile.getShooter() instanceof Player shooter && shooter.isOnline()
                && arrows.markExploded(projectile)) {
            Double base = arrows.baseDamage(projectile);
            FlechaExplosiva.explode(plugin, shooter, projectile.getLocation(), base == null ? 0 : base, radius);
        }
        if (arrows.isTemporary(projectile)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, projectile::remove, 1L);
        }
    }
}
