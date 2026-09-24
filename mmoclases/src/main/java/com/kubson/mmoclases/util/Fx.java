package com.kubson.mmoclases.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

/** Efectos de partículas reutilizables. */
public final class Fx {

    private Fx() {
    }

    public static void ring(Location center, double radius, Particle particle, int points) {
        World world = center.getWorld();
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            Location point = center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            world.spawnParticle(particle, point, 1, 0, 0, 0, 0);
        }
    }

    public static void dustRing(Location center, double radius, Color color, int points) {
        World world = center.getWorld();
        Particle.DustOptions dust = new Particle.DustOptions(color, 1.4f);
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            Location point = center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, dust);
        }
    }

    public static void line(Location from, Location to, Particle particle, double spacing) {
        World world = from.getWorld();
        Vector delta = to.toVector().subtract(from.toVector());
        double length = delta.length();
        if (length < 1.0E-3) {
            return;
        }
        Vector step = delta.multiply(spacing / length);
        Location point = from.clone();
        for (double travelled = 0; travelled < length; travelled += spacing) {
            world.spawnParticle(particle, point, 1, 0, 0, 0, 0);
            point.add(step);
        }
    }
}
