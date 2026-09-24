package com.kubson.mmoclasses.ability.impl.guerrero;

import com.kubson.mmoclasses.ability.Ability;
import com.kubson.mmoclasses.ability.AbilityContext;
import com.kubson.mmoclasses.combat.DamageType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Embestida hacia delante que daña y aturde a los enemigos en el camino. */
public final class Carga extends Ability {

    private static final int DURATION_TICKS = 12;

    public Carga() {
        super("carga", "Carga", Material.IRON_BOOTS,
                List.of("Embistes hacia delante: dañas y aturdes", "a los enemigos que atropellas."),
                5, 15, 10, false);
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player player = ctx.player();
        Vector direction = player.getLocation().getDirection().setY(0);
        if (direction.lengthSquared() < 1.0E-4) {
            return false;
        }
        direction.normalize();
        player.setVelocity(direction.clone().multiply(ctx.num("fuerza", 1.8)).setY(0.25));
        ctx.data().setFallImmuneFor(2000);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1f, 0.8f);

        double damage = ctx.num("dano-base", 6);
        int stunTicks = (int) (ctx.num("aturdir-segundos", 1.5) * 20);
        Set<UUID> alreadyHit = new HashSet<>();

        new BukkitRunnable() {
            private int tick;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead() || tick++ >= DURATION_TICKS) {
                    cancel();
                    return;
                }
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 4, 0.2, 0.05, 0.2, 0.01);
                for (LivingEntity target : ctx.enemiesNear(player.getLocation(), 1.8)) {
                    if (!alreadyHit.add(target.getUniqueId())) {
                        continue;
                    }
                    ctx.damage(target, damage, DamageType.FISICO);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, stunTicks, 3));
                    target.setVelocity(direction.clone().multiply(0.8).setY(0.3));
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1f, 0.9f);
                }
            }
        }.runTaskTimer(ctx.plugin(), 1L, 1L);
        return true;
    }
}
