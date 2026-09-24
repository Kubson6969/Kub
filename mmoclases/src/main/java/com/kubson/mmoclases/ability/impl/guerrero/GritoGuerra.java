package com.kubson.mmoclases.ability.impl.guerrero;

import com.kubson.mmoclases.ability.Ability;
import com.kubson.mmoclases.ability.AbilityContext;
import com.kubson.mmoclases.util.Fx;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/** Provoca a los monstruos cercanos, te protege y da fuerza a tus aliados. */
public final class GritoGuerra extends Ability {

    public GritoGuerra() {
        super("grito_guerra", "Grito de Guerra", Material.GOAT_HORN,
                List.of("Provocas a los monstruos cercanos para que", "te ataquen a ti. Ganas resistencia y", "absorción; tus aliados ganan fuerza."),
                10, 25, 20, false);
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player player = ctx.player();
        Location center = player.getLocation();
        double radius = ctx.num("radio", 10);
        int ticks = (int) (ctx.num("duracion-segundos", 6) * 20);
        int absorption = Math.max(0, (int) ctx.num("absorcion-nivel", 2) - 1);

        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof Mob mob && ctx.combat().isEnemy(player, mob)) {
                mob.setTarget(player);
                mob.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, mob.getEyeLocation().add(0, 0.5, 0), 1, 0, 0, 0, 0);
            }
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, ticks, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, ticks, absorption));
        for (Player ally : ctx.alliesNear(center, radius)) {
            if (!ally.equals(player)) {
                ally.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, ticks, 0));
            }
        }

        Fx.dustRing(center.clone().add(0, 0.3, 0), 2.5, Color.fromRGB(0xE0533D), 40);
        center.getWorld().playSound(center, Sound.ENTITY_RAVAGER_ROAR, 1f, 1.1f);
        return true;
    }
}
