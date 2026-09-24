package com.kubson.mmoclasses.ability.impl.arquero;

import com.kubson.mmoclasses.ability.Ability;
import com.kubson.mmoclasses.ability.AbilityContext;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

/** Dispara varias flechas en abanico. */
public final class DisparoTriple extends Ability {

    public DisparoTriple() {
        super("disparo_triple", "Disparo Triple", Material.ARROW,
                List.of("Dispara 3 flechas en abanico.", "Perfecto para grupos de enemigos."),
                1, 25, 3, false);
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player player = ctx.player();
        int arrows = Math.max(1, (int) ctx.num("flechas", 3));
        double spread = ctx.num("apertura", 12);
        double speed = ctx.num("velocidad", 3.0);
        double damage = ctx.num("dano-base", 5);
        Vector direction = player.getEyeLocation().getDirection();

        for (int i = 0; i < arrows; i++) {
            double angle = arrows == 1 ? 0 : -spread + (2 * spread) * i / (arrows - 1);
            Vector velocity = direction.clone().rotateAroundY(Math.toRadians(angle)).multiply(speed);
            Arrow arrow = player.launchProjectile(Arrow.class, velocity);
            ctx.plugin().arrows().tag(arrow, damage);
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 1.2f);
        return true;
    }
}
