package com.kubson.mmoclases.ability.impl.mago;

import com.kubson.mmoclases.ability.Ability;
import com.kubson.mmoclases.ability.AbilityContext;
import com.kubson.mmoclases.combat.DamageType;
import com.kubson.mmoclases.util.Fx;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/** Explosión de hielo a tu alrededor: daño + ralentización fuerte. */
public final class NovaEscarcha extends Ability {

    public NovaEscarcha() {
        super("nova_escarcha", "Nova de Escarcha", Material.BLUE_ICE,
                List.of("Congela a los enemigos cercanos:", "daño mágico y lentitud. Ideal para escapar."),
                10, 35, 12, false);
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player player = ctx.player();
        Location center = player.getLocation();
        double radius = ctx.num("radio", 5);
        int slowTicks = (int) (ctx.num("lentitud-segundos", 3) * 20);
        int slowAmplifier = Math.max(0, (int) ctx.num("lentitud-nivel", 3) - 1);

        center.getWorld().playSound(center, Sound.BLOCK_GLASS_BREAK, 1f, 0.7f);
        center.getWorld().playSound(center, Sound.ENTITY_PLAYER_HURT_FREEZE, 1f, 1f);
        for (LivingEntity target : ctx.enemiesNear(center, radius)) {
            ctx.damage(target, ctx.num("dano-base", 6), DamageType.MAGICO);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slowTicks, slowAmplifier));
            // Por debajo de 140 ticks da el efecto visual de congelado sin daño extra.
            target.setFreezeTicks(Math.max(target.getFreezeTicks(), 120));
        }

        // Anillo que se expande.
        new BukkitRunnable() {
            private int step;

            @Override
            public void run() {
                step++;
                double r = radius * step / 5.0;
                Fx.ring(center.clone().add(0, 0.2, 0), r, Particle.SNOWFLAKE, (int) (12 + r * 6));
                if (step >= 5) {
                    cancel();
                }
            }
        }.runTaskTimer(ctx.plugin(), 0L, 1L);
        return true;
    }
}
