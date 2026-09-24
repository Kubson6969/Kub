package com.kubson.mmoclasses.ability.impl.clerigo;

import com.kubson.mmoclasses.ability.Ability;
import com.kubson.mmoclasses.ability.AbilityContext;
import com.kubson.mmoclasses.api.Stat;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/** Escudo de absorción para ti y tus aliados; más fuerte con más poder de curación. */
public final class EscudoDivino extends Ability {

    public EscudoDivino() {
        super("escudo_divino", "Escudo Divino", Material.SHIELD,
                List.of("Tú y los aliados cercanos recibís un escudo", "de absorción y resistencia breve.", "Escala con el poder de curación."),
                10, 30, 18, false);
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player player = ctx.player();
        Location center = player.getLocation();
        int ticks = (int) (ctx.num("duracion-segundos", 10) * 20);
        // Nivel de absorción: I + 1 por cada 25% de poder de curación (máx. V).
        int amplifier = Math.min(4, (int) (ctx.stats().get(Stat.PODER_CURACION) / 25));

        for (Player ally : ctx.alliesNear(center, ctx.num("radio", 8))) {
            ally.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, ticks, amplifier));
            ally.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 0));
            ally.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, ally.getLocation().add(0, 1, 0), 25, 0.4, 0.6, 0.4, 0.2);
        }
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1.4f);
        return true;
    }
}
