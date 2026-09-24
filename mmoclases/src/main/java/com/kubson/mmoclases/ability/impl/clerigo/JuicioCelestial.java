package com.kubson.mmoclases.ability.impl.clerigo;

import com.kubson.mmoclases.ability.Ability;
import com.kubson.mmoclases.ability.AbilityContext;
import com.kubson.mmoclases.combat.DamageType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;

/** Definitiva: rayos del cielo sobre los enemigos cercanos y curación a los aliados. */
public final class JuicioCelestial extends Ability {

    private static final long TICKS_BETWEEN_STRIKES = 3;

    public JuicioCelestial() {
        super("juicio_celestial", "Juicio Celestial", Material.LIGHTNING_ROD,
                List.of("DEFINITIVA. Rayos sagrados caen sobre los", "enemigos cercanos y todos tus aliados", "en la zona se curan."),
                20, 70, 50, true);
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player player = ctx.player();
        Location center = player.getLocation();
        double radius = ctx.num("radio", 12);
        int maxTargets = Math.max(1, (int) ctx.num("objetivos", 6));
        double damage = ctx.num("dano-base", 16);

        List<LivingEntity> targets = ctx.enemiesNear(center, radius).stream()
                .sorted(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(center)))
                .limit(maxTargets)
                .toList();
        for (int i = 0; i < targets.size(); i++) {
            LivingEntity target = targets.get(i);
            ctx.plugin().getServer().getScheduler().runTaskLater(ctx.plugin(), () -> {
                if (!player.isOnline() || !target.isValid()) {
                    return;
                }
                target.getWorld().strikeLightningEffect(target.getLocation());
                ctx.damage(target, damage, DamageType.SAGRADO);
            }, i * TICKS_BETWEEN_STRIKES);
        }

        for (Player ally : ctx.alliesNear(center, radius)) {
            ctx.heal(ally, ctx.num("curacion-base", 8));
        }
        center.getWorld().playSound(center, Sound.BLOCK_BELL_USE, 1f, 0.8f);
        return true;
    }
}
