package com.kubson.mmoclases.ability.impl.guerrero;

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

import java.util.List;

/** Giro con el arma que golpea y empuja a todos los enemigos alrededor. */
public final class TajoGiratorio extends Ability {

    public TajoGiratorio() {
        super("tajo_giratorio", "Tajo Giratorio", Material.IRON_AXE,
                List.of("Giras con tu arma golpeando a todos", "los enemigos a tu alrededor y los empujas."),
                1, 20, 5, false);
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player player = ctx.player();
        Location center = player.getLocation();
        double radius = ctx.num("radio", 3.5);
        double push = ctx.num("empuje", 0.8);

        Fx.ring(center.clone().add(0, 1, 0), radius * 0.7, Particle.SWEEP_ATTACK, 8);
        center.getWorld().spawnParticle(Particle.CRIT, center.clone().add(0, 1, 0), 30, radius / 2, 0.3, radius / 2, 0.1);
        center.getWorld().playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.8f);

        for (LivingEntity target : ctx.enemiesNear(center, radius)) {
            ctx.damage(target, ctx.num("dano-base", 8), DamageType.FISICO);
            ctx.combat().knockback(target, center, push, 0.35);
        }
        return true;
    }
}
