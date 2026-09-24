package com.kubson.mmoclases.ability.impl.arquero;

import com.kubson.mmoclases.ability.Ability;
import com.kubson.mmoclases.ability.AbilityContext;
import com.kubson.mmoclases.util.Fx;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Definitiva: una lluvia de flechas cae sobre la zona durante unos segundos. */
public final class LluviaFlechas extends Ability {

    private static final int PERIOD = 2;

    public LluviaFlechas() {
        super("lluvia_flechas", "Lluvia de Flechas", Material.SPECTRAL_ARROW,
                List.of("DEFINITIVA. Durante unos segundos caen", "flechas del cielo sobre la zona", "a la que apuntas."),
                20, 60, 40, true);
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player player = ctx.player();
        Location target = ctx.combat().targetGround(player, ctx.num("alcance", 30));
        double radius = ctx.num("radio", 4);
        int duration = (int) (ctx.num("duracion-segundos", 3) * 20);
        int perWave = Math.max(1, (int) ctx.num("flechas-por-oleada", 4));
        double damage = ctx.num("dano-base", 4);
        World world = target.getWorld();
        world.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 0.5f);

        new BukkitRunnable() {
            private int tick;

            @Override
            public void run() {
                if (!player.isOnline() || tick >= duration) {
                    cancel();
                    return;
                }
                if (tick % 4 == 0) {
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    for (int i = 0; i < perWave; i++) {
                        double distance = radius * Math.sqrt(random.nextDouble());
                        double angle = random.nextDouble(2 * Math.PI);
                        Location spawn = target.clone().add(Math.cos(angle) * distance, 12, Math.sin(angle) * distance);
                        Arrow arrow = world.spawnArrow(spawn, new Vector(0, -1, 0), 1.8f, 0f);
                        arrow.setShooter(player);
                        ctx.plugin().arrows().tag(arrow, damage);
                    }
                }
                if (tick % 10 == 0) {
                    Fx.ring(target, radius, Particle.CRIT, 30);
                }
                tick += PERIOD;
            }
        }.runTaskTimer(ctx.plugin(), 0L, PERIOD);
        return true;
    }
}
