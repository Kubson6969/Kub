package com.kubson.mmoclasses.ability.impl.arquero;

import com.kubson.mmoclasses.MMOClasses;
import com.kubson.mmoclasses.ability.Ability;
import com.kubson.mmoclasses.ability.AbilityContext;
import com.kubson.mmoclasses.combat.DamageType;
import com.kubson.mmoclasses.data.PlayerData;
import com.kubson.mmoclasses.stats.PlayerStats;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/** Flecha que explota al impactar. La explosión la dispara el ProjectileListener. */
public final class FlechaExplosiva extends Ability {

    public FlechaExplosiva() {
        super("flecha_explosiva", "Flecha Explosiva", Material.TNT,
                List.of("Dispara una flecha que explota al", "impactar: daño en área y empuje.", "No rompe bloques."),
                10, 40, 10, false);
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player player = ctx.player();
        Arrow arrow = player.launchProjectile(Arrow.class,
                player.getEyeLocation().getDirection().multiply(ctx.num("velocidad", 3.0)));
        ctx.plugin().arrows().tagExplosive(arrow, ctx.num("dano-base", 10), ctx.num("radio", 4));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 0.7f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.5f, 1.5f);

        // Estela de humo mientras vuela.
        new BukkitRunnable() {
            private int ticks;

            @Override
            public void run() {
                if (!arrow.isValid() || arrow.isInBlock() || ticks++ > 100) {
                    cancel();
                    return;
                }
                arrow.getWorld().spawnParticle(Particle.SMOKE, arrow.getLocation(), 3, 0.05, 0.05, 0.05, 0);
                arrow.getWorld().spawnParticle(Particle.FLAME, arrow.getLocation(), 1, 0, 0, 0, 0);
            }
        }.runTaskTimer(ctx.plugin(), 1L, 1L);
        return true;
    }

    /** Llamado al impactar una flecha explosiva. */
    public static void explode(MMOClasses plugin, Player shooter, Location center, double baseDamage, double radius) {
        World world = center.getWorld();
        world.spawnParticle(Particle.EXPLOSION, center, 3, 0.5, 0.5, 0.5, 0);
        world.spawnParticle(Particle.FLAME, center, 30, 0.6, 0.6, 0.6, 0.05);
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.2f);
        PlayerData data = plugin.data().get(shooter);
        PlayerStats stats = plugin.stats().compute(shooter, data);
        for (LivingEntity target : plugin.combat().enemiesNear(shooter, center, radius)) {
            plugin.combat().damage(shooter, stats, target, baseDamage, DamageType.DISTANCIA);
            plugin.combat().knockback(target, center, 0.7, 0.4);
        }
    }
}
