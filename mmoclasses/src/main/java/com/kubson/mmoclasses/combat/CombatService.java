package com.kubson.mmoclasses.combat;

import com.kubson.mmoclasses.MMOClasses;
import com.kubson.mmoclasses.Settings;
import com.kubson.mmoclasses.api.PlayerClass;
import com.kubson.mmoclasses.api.Stat;
import com.kubson.mmoclasses.stats.PlayerStats;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Zoglin;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Utilidades de combate compartidas por habilidades y listeners. */
public final class CombatService {

    private final MMOClasses plugin;
    private boolean applyingAbilityDamage;

    public CombatService(MMOClasses plugin) {
        this.plugin = plugin;
    }

    /** true mientras se aplica daño de habilidad (el listener no debe volver a escalarlo). */
    public boolean isApplyingAbilityDamage() {
        return applyingAbilityDamage;
    }

    public boolean rollCrit(PlayerStats stats) {
        return ThreadLocalRandom.current().nextDouble(100) < stats.get(Stat.CRITICO_PROB);
    }

    public double critMultiplier(PlayerStats stats) {
        return stats.get(Stat.CRITICO_DANO) / 100.0;
    }

    public void critEffect(Player attacker, LivingEntity victim) {
        victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, victim.getHeight() / 2, 0), 14, 0.3, 0.4, 0.3, 0.15);
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8f, 1.1f);
    }

    /**
     * Daño de habilidad: base * (1 + stat%/100), crítico y bonus sagrado.
     * @return el daño aplicado
     */
    public double damage(Player caster, PlayerStats stats, LivingEntity target, double base, DamageType type) {
        if (target.isDead()) {
            return 0;
        }
        double amount = base * stats.multiplier(type.scalingStat());
        if (rollCrit(stats)) {
            amount *= critMultiplier(stats);
            critEffect(caster, target);
        }
        if (type == DamageType.SAGRADO && isUndead(target)) {
            amount *= 1 + plugin.settings().passive(PlayerClass.CLERIGO, "bono-no-muertos", 50) / 100.0;
            target.getWorld().spawnParticle(Particle.END_ROD, target.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.02);
        }
        target.setNoDamageTicks(0);
        applyingAbilityDamage = true;
        try {
            target.damage(amount, caster);
        } finally {
            applyingAbilityDamage = false;
        }
        return amount;
    }

    /**
     * Curación: base * (1 + poder_curacion%/100), sin pasar de la vida máxima.
     * @return la vida realmente curada
     */
    public double heal(PlayerStats stats, LivingEntity target, double base) {
        if (target.isDead()) {
            return 0;
        }
        double amount = base * stats.multiplier(Stat.PODER_CURACION);
        double max = plugin.stats().maxHealth(target);
        double healed = Math.min(amount, max - target.getHealth());
        if (healed > 0) {
            target.setHealth(target.getHealth() + healed);
            target.getWorld().spawnParticle(Particle.HEART, target.getLocation().add(0, target.getHeight() + 0.3, 0), 2, 0.3, 0.2, 0.3, 0);
        }
        return Math.max(0, healed);
    }

    public boolean isEnemy(Player caster, Entity entity) {
        if (!(entity instanceof LivingEntity living) || entity.equals(caster) || living.isDead()
                || entity instanceof ArmorStand || entity.isInvulnerable()) {
            return false;
        }
        Settings settings = plugin.settings();
        if (entity instanceof Player target) {
            if (!settings.pvpAbilities || !target.getWorld().getPVP()) {
                return false;
            }
            GameMode mode = target.getGameMode();
            return mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE;
        }
        if (entity instanceof Tameable tameable && tameable.isTamed() && caster.equals(tameable.getOwner())) {
            return false;
        }
        if (entity instanceof Enemy) {
            return true;
        }
        if (entity instanceof Mob mob && caster.equals(mob.getTarget())) {
            return true;
        }
        return settings.aoeHitsAnimals && !(entity instanceof AbstractVillager);
    }

    public boolean isAlly(Player caster, Entity entity) {
        if (!(entity instanceof Player player) || player.isDead() || player.getGameMode() == GameMode.SPECTATOR) {
            return false;
        }
        return player.equals(caster) || !isEnemy(caster, player);
    }

    public List<LivingEntity> enemiesNear(Player caster, Location center, double radius) {
        List<LivingEntity> result = new ArrayList<>();
        double radiusSquared = radius * radius;
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (isEnemy(caster, entity) && entity.getLocation().distanceSquared(center) <= radiusSquared) {
                result.add((LivingEntity) entity);
            }
        }
        return result;
    }

    /** Aliados cerca (incluye al propio lanzador si está dentro del radio). */
    public List<Player> alliesNear(Player caster, Location center, double radius) {
        List<Player> result = new ArrayList<>();
        double radiusSquared = radius * radius;
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (isAlly(caster, entity) && entity.getLocation().distanceSquared(center) <= radiusSquared) {
                result.add((Player) entity);
            }
        }
        return result;
    }

    /** Punto del suelo al que mira el jugador (máximo {@code range} bloques). */
    public Location targetGround(Player player, double range) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection();
        World world = player.getWorld();
        RayTraceResult hit = world.rayTraceBlocks(eye, direction, range, FluidCollisionMode.NEVER, true);
        Location point = hit != null
                ? hit.getHitPosition().toLocation(world).subtract(direction.clone().multiply(0.3))
                : eye.clone().add(direction.clone().multiply(range));
        for (int i = 0; i < 16 && point.getY() > world.getMinHeight(); i++) {
            if (!point.clone().subtract(0, 0.5, 0).getBlock().isPassable()) {
                break;
            }
            point.subtract(0, 1, 0);
        }
        point.setY(Math.floor(point.getY()) + 0.1);
        return point;
    }

    /** Empuja a {@code target} alejándolo de {@code from}. */
    public void knockback(Entity target, Location from, double strength, double up) {
        Vector push = target.getLocation().toVector().subtract(from.toVector()).setY(0);
        if (push.lengthSquared() < 1.0E-4) {
            push = new Vector(0, 0, 0);
        } else {
            push.normalize().multiply(strength);
        }
        push.setY(up);
        target.setVelocity(push);
    }

    public static boolean isUndead(Entity entity) {
        return entity instanceof Zombie || entity instanceof AbstractSkeleton || entity instanceof Phantom
                || entity instanceof Wither || entity instanceof Zoglin
                || entity instanceof SkeletonHorse || entity instanceof ZombieHorse;
    }
}
