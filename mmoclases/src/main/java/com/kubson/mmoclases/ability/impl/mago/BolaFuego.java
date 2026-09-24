package com.kubson.mmoclases.ability.impl.mago;

import com.kubson.mmoclases.ability.Ability;
import com.kubson.mmoclases.ability.AbilityContext;
import com.kubson.mmoclases.combat.DamageType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

/** Proyectil de fuego que explota al impactar: daño en área + quemadura. */
public final class BolaFuego extends Ability {

    private static final double SPEED = 1.2;

    public BolaFuego() {
        super("bola_fuego", "Bola de Fuego", Material.FIRE_CHARGE,
                List.of("Lanza una bola de fuego que explota al", "impactar: daño mágico en área y quemadura."),
                1, 20, 4, false);
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player player = ctx.player();
        double range = ctx.num("alcance", 30);
        Location position = player.getEyeLocation();
        Vector step = position.getDirection().multiply(SPEED);
        player.getWorld().playSound(position, Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);

        new BukkitRunnable() {
            private double travelled;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                position.add(step);
                travelled += SPEED;
                World world = position.getWorld();
                world.spawnParticle(Particle.FLAME, position, 8, 0.15, 0.15, 0.15, 0.02);
                world.spawnParticle(Particle.SMOKE, position, 2, 0.1, 0.1, 0.1, 0.01);
                boolean hitBlock = !position.getBlock().isPassable();
                boolean hitEntity = !ctx.enemiesNear(position, 1.2).isEmpty();
                if (hitBlock || hitEntity || travelled >= range) {
                    explode(ctx, position);
                    cancel();
                }
            }
        }.runTaskTimer(ctx.plugin(), 0L, 1L);
        return true;
    }

    private void explode(AbilityContext ctx, Location center) {
        World world = center.getWorld();
        world.spawnParticle(Particle.EXPLOSION, center, 1, 0, 0, 0, 0);
        world.spawnParticle(Particle.FLAME, center, 40, 0.8, 0.8, 0.8, 0.08);
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.3f);
        double damage = ctx.num("dano-base", 8);
        int fireTicks = (int) (ctx.num("fuego-segundos", 3) * 20);
        for (LivingEntity target : ctx.enemiesNear(center, ctx.num("radio", 3))) {
            ctx.damage(target, damage, DamageType.MAGICO);
            target.setFireTicks(Math.max(target.getFireTicks(), fireTicks));
        }
    }
}
