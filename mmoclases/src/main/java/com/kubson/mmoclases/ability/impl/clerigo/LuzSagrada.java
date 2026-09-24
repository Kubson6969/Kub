package com.kubson.mmoclases.ability.impl.clerigo;

import com.kubson.mmoclases.ability.Ability;
import com.kubson.mmoclases.ability.AbilityContext;
import com.kubson.mmoclases.combat.DamageType;
import com.kubson.mmoclases.util.Fx;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.List;

/** Rayo de luz: cura al aliado o daña al enemigo al que apuntas. */
public final class LuzSagrada extends Ability {

    public LuzSagrada() {
        super("luz_sagrada", "Luz Sagrada", Material.GLOWSTONE_DUST,
                List.of("Rayo de luz: cura al aliado al que apuntas", "o hace daño sagrado al enemigo.", "Sin objetivo, te cura a ti a la mitad."),
                1, 15, 3, false);
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player player = ctx.player();
        World world = player.getWorld();
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection();
        double range = ctx.num("alcance", 20);

        RayTraceResult hit = world.rayTrace(eye, direction, range, FluidCollisionMode.NEVER, true, 0.4,
                entity -> !entity.equals(player) && entity instanceof LivingEntity && !(entity instanceof ArmorStand));
        Location end = hit != null
                ? hit.getHitPosition().toLocation(world)
                : eye.clone().add(direction.clone().multiply(range));
        Fx.line(eye.clone().subtract(0, 0.3, 0), end, Particle.END_ROD, 0.4);
        world.playSound(eye, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.5f);

        double healing = ctx.num("curacion-base", 5);
        Entity target = hit == null ? null : hit.getHitEntity();
        if (target instanceof LivingEntity living) {
            if (ctx.combat().isAlly(player, living)) {
                ctx.heal(living, healing);
            } else if (ctx.combat().isEnemy(player, living)) {
                ctx.damage(living, ctx.num("dano-base", 7), DamageType.SAGRADO);
            }
        } else {
            ctx.heal(player, healing / 2);
        }
        return true;
    }
}
