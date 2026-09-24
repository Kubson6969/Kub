package com.kubson.mmoclasses.ability.impl.mago;

import com.kubson.mmoclasses.ability.Ability;
import com.kubson.mmoclasses.ability.AbilityContext;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.List;

/** Teletransporte corto hacia donde miras, sin atravesar paredes. */
public final class Parpadeo extends Ability {

    public Parpadeo() {
        super("parpadeo", "Parpadeo", Material.ENDER_PEARL,
                List.of("Te teletransportas unos bloques hacia", "donde miras. No atraviesa paredes."),
                5, 25, 10, false);
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player player = ctx.player();
        World world = player.getWorld();
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection();
        double maxDistance = ctx.num("distancia", 8);

        RayTraceResult hit = world.rayTraceBlocks(eye, direction, maxDistance, FluidCollisionMode.NEVER, true);
        double distance = hit == null ? maxDistance : hit.getHitPosition().distance(eye.toVector()) - 0.8;

        for (double d = distance; d >= 1; d -= 0.5) {
            Location feet = eye.clone().add(direction.clone().multiply(d)).subtract(0, player.getEyeHeight(), 0);
            if (isSafe(feet)) {
                Location origin = player.getLocation();
                world.spawnParticle(Particle.PORTAL, origin.clone().add(0, 1, 0), 40, 0.3, 0.6, 0.3, 0.5);
                player.teleport(feet);
                player.setFallDistance(0);
                world.spawnParticle(Particle.REVERSE_PORTAL, feet.clone().add(0, 1, 0), 40, 0.3, 0.6, 0.3, 0.05);
                world.playSound(feet, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
                return true;
            }
        }
        return false;
    }

    private static boolean isSafe(Location feet) {
        Block body = feet.getBlock();
        Block head = body.getRelative(0, 1, 0);
        return body.isPassable() && head.isPassable()
                && body.getType() != Material.LAVA && head.getType() != Material.LAVA;
    }
}
