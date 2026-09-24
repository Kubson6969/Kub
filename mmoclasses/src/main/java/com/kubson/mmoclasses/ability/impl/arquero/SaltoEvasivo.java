package com.kubson.mmoclasses.ability.impl.arquero;

import com.kubson.mmoclasses.ability.Ability;
import com.kubson.mmoclasses.ability.AbilityContext;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;

/** Salto hacia atrás + velocidad. Sin daño de caída. */
public final class SaltoEvasivo extends Ability {

    public SaltoEvasivo() {
        super("salto_evasivo", "Salto Evasivo", Material.RABBIT_FOOT,
                List.of("Saltas hacia atrás y ganas velocidad", "unos segundos. Sin daño de caída."),
                5, 30, 8, false);
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player player = ctx.player();
        Vector velocity = player.getLocation().getDirection().setY(0);
        if (velocity.lengthSquared() > 1.0E-4) {
            velocity.normalize().multiply(-ctx.num("fuerza", 1.6));
        }
        velocity.setY(ctx.num("altura", 0.6));
        player.setVelocity(velocity);
        ctx.data().setFallImmuneFor(3000);

        int ticks = (int) (ctx.num("velocidad-segundos", 3) * 20);
        int amplifier = Math.max(0, (int) ctx.num("velocidad-nivel", 2) - 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, amplifier, false, false, true));

        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, 0.3, 0.1, 0.3, 0.05);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.6f, 1.6f);
        return true;
    }
}
