package com.kubson.mmoclases.ability.impl.mago;

import com.kubson.mmoclases.ability.Ability;
import com.kubson.mmoclases.ability.AbilityContext;
import com.kubson.mmoclases.combat.DamageType;
import com.kubson.mmoclases.util.Fx;
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

/** Definitiva: un meteoro cae donde miras tras una breve señal. */
public final class Meteoro extends Ability {

    public Meteoro() {
        super("meteoro", "Meteoro", Material.MAGMA_BLOCK,
                List.of("DEFINITIVA. Invoca un meteoro que cae", "donde miras: gran daño en área,", "empuje y quemadura."),
                20, 60, 45, true);
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player player = ctx.player();
        Location target = ctx.combat().targetGround(player, ctx.num("alcance", 30));
        double radius = ctx.num("radio", 5);
        int delay = Math.max(5, (int) ctx.num("retraso-ticks", 30));

        Vector back = player.getLocation().getDirection().setY(0);
        if (back.lengthSquared() > 1.0E-4) {
            back.normalize().multiply(-6);
        }
        Location position = target.clone().add(back).add(0, 16, 0);
        Vector step = target.toVector().subtract(position.toVector()).multiply(1.0 / delay);
        World world = target.getWorld();
        world.playSound(target, Sound.ENTITY_WITHER_SHOOT, 1f, 0.6f);

        new BukkitRunnable() {
            private int tick;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                tick++;
                position.add(step);
                world.spawnParticle(Particle.FLAME, position, 20, 0.4, 0.4, 0.4, 0.02);
                world.spawnParticle(Particle.LARGE_SMOKE, position, 4, 0.3, 0.3, 0.3, 0.01);
                world.spawnParticle(Particle.LAVA, position, 2, 0.2, 0.2, 0.2, 0);
                if (tick % 5 == 0) {
                    Fx.ring(target, radius, Particle.FLAME, 36);
                }
                if (tick >= delay) {
                    impact(ctx, target, radius);
                    cancel();
                }
            }
        }.runTaskTimer(ctx.plugin(), 0L, 1L);
        return true;
    }

    private void impact(AbilityContext ctx, Location center, double radius) {
        World world = center.getWorld();
        world.spawnParticle(Particle.EXPLOSION_EMITTER, center, 1, 0, 0, 0, 0);
        world.spawnParticle(Particle.FLAME, center, 120, radius / 2, 0.5, radius / 2, 0.1);
        world.spawnParticle(Particle.LAVA, center, 30, radius / 2, 0.3, radius / 2, 0);
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.6f);
        int fireTicks = (int) (ctx.num("fuego-segundos", 5) * 20);
        for (LivingEntity target : ctx.enemiesNear(center, radius)) {
            ctx.damage(target, ctx.num("dano-base", 20), DamageType.MAGICO);
            ctx.combat().knockback(target, center, 0.6, 0.5);
            target.setFireTicks(Math.max(target.getFireTicks(), fireTicks));
        }
    }
}
