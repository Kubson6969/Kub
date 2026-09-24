package com.kubson.mmoclases.ability.impl.clerigo;

import com.kubson.mmoclases.ability.Ability;
import com.kubson.mmoclases.ability.AbilityContext;
import com.kubson.mmoclases.util.Fx;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/** Zona en el suelo que cura cada segundo a los aliados que estén dentro. */
public final class CirculoSanacion extends Ability {

    private static final int PERIOD = 5;

    public CirculoSanacion() {
        super("circulo_sanacion", "Círculo de Sanación", Material.GLISTERING_MELON_SLICE,
                List.of("Crea un círculo sagrado bajo tus pies", "que cura cada segundo a los aliados", "que estén dentro."),
                5, 35, 15, false);
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player player = ctx.player();
        Location center = player.getLocation().add(0, 0.2, 0);
        double radius = ctx.num("radio", 5);
        int duration = (int) (ctx.num("duracion-segundos", 5) * 20);
        double healing = ctx.num("curacion-base", 2.5);
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);

        new BukkitRunnable() {
            private int tick;

            @Override
            public void run() {
                if (!player.isOnline() || tick >= duration) {
                    cancel();
                    return;
                }
                Fx.dustRing(center, radius, Color.fromRGB(0xF2D15C), 48);
                center.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, center, 6, radius / 2, 0.2, radius / 2, 0);
                if (tick % 20 == 0) {
                    for (Player ally : ctx.alliesNear(center, radius)) {
                        ctx.heal(ally, healing);
                    }
                }
                tick += PERIOD;
            }
        }.runTaskTimer(ctx.plugin(), 0L, PERIOD);
        return true;
    }
}
