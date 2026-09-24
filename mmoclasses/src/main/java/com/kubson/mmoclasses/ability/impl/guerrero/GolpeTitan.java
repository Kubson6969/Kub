package com.kubson.mmoclasses.ability.impl.guerrero;

import com.kubson.mmoclasses.ability.Ability;
import com.kubson.mmoclasses.ability.AbilityContext;
import com.kubson.mmoclasses.combat.DamageType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

/** Definitiva: saltas y caes con fuerza, lanzando por los aires a los enemigos. */
public final class GolpeTitan extends Ability {

    private static final int SLAM_DOWN_TICK = 12;
    private static final int MAX_TICKS = 60;

    public GolpeTitan() {
        super("golpe_titan", "Golpe del Titán", Material.ANVIL,
                List.of("DEFINITIVA. Saltas y golpeas el suelo:", "gran daño en área, lanzas a los enemigos", "por los aires y los ralentizas."),
                20, 50, 35, true);
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player player = ctx.player();
        Vector jump = player.getLocation().getDirection().setY(0);
        if (jump.lengthSquared() > 1.0E-4) {
            jump.normalize().multiply(0.5);
        }
        jump.setY(ctx.num("salto", 1.1));
        player.setVelocity(jump);
        ctx.data().setFallImmuneFor(5000);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 0.7f);

        new BukkitRunnable() {
            private int tick;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    cancel();
                    return;
                }
                tick++;
                if (tick == SLAM_DOWN_TICK) {
                    player.setVelocity(player.getVelocity().setY(-1.6));
                }
                if (tick > 6 && (onGround(player) || tick >= MAX_TICKS)) {
                    slam(ctx, player.getLocation());
                    cancel();
                }
            }
        }.runTaskTimer(ctx.plugin(), 1L, 1L);
        return true;
    }

    private static boolean onGround(Player player) {
        return !player.getLocation().subtract(0, 0.1, 0).getBlock().isPassable();
    }

    private void slam(AbilityContext ctx, Location center) {
        World world = center.getWorld();
        double radius = ctx.num("radio", 6);
        world.spawnParticle(Particle.EXPLOSION, center, 6, radius / 3, 0.2, radius / 3, 0);
        world.spawnParticle(Particle.CLOUD, center, 60, radius / 2, 0.1, radius / 2, 0.15);
        world.spawnParticle(Particle.CRIT, center, 40, radius / 2, 0.5, radius / 2, 0.2);
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.6f);
        world.playSound(center, Sound.BLOCK_ANVIL_LAND, 0.8f, 0.6f);
        for (LivingEntity target : ctx.enemiesNear(center, radius)) {
            ctx.damage(target, ctx.num("dano-base", 18), DamageType.FISICO);
            ctx.combat().knockback(target, center, 0.6, 0.9);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
        }
    }
}
