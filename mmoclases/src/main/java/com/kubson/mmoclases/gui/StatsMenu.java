package com.kubson.mmoclases.gui;

import com.kubson.mmoclases.MMOClases;
import com.kubson.mmoclases.ability.Ability;
import com.kubson.mmoclases.api.CoreAttribute;
import com.kubson.mmoclases.api.PlayerClass;
import com.kubson.mmoclases.api.Stat;
import com.kubson.mmoclases.data.ClassProgress;
import com.kubson.mmoclases.data.PlayerData;
import com.kubson.mmoclases.stats.PlayerStats;
import com.kubson.mmoclases.util.Format;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Menú de personaje: repartir puntos de atributo y ver stats y habilidades. */
public final class StatsMenu extends Menu {

    private static final int SUMMARY_SLOT = 4;
    private static final int FIRST_ATTRIBUTE_SLOT = 11;
    private static final int ABILITIES_SLOT = 21;
    private static final int STATS_SLOT = 23;
    private static final int CLASS_MENU_SLOT = 26;

    private final MMOClases plugin;

    public StatsMenu(MMOClases plugin) {
        super(3, Component.text("Tu personaje", NamedTextColor.DARK_GRAY));
        this.plugin = plugin;
    }

    @Override
    protected void draw(Player viewer) {
        fill();
        PlayerData data = plugin.data().get(viewer);
        PlayerClass playerClass = data.currentClass();
        if (playerClass == null) {
            return;
        }
        ClassProgress progress = data.current();
        PlayerStats stats = plugin.stats().compute(viewer, data);

        List<Component> summary = new ArrayList<>();
        summary.add(line("Nivel " + progress.level() + " / " + plugin.settings().maxLevel, NamedTextColor.GRAY));
        if (progress.level() < plugin.settings().maxLevel) {
            double needed = plugin.settings().xpToNext(progress.level());
            summary.add(line(Format.bar(progress.xp() / needed, 20) + " " + Format.number(progress.xp()) + "/" + Format.number(needed) + " XP", NamedTextColor.GREEN));
        }
        summary.add(Component.empty());
        summary.add(line("Puntos sin asignar: " + progress.unspentPoints(), progress.unspentPoints() > 0 ? NamedTextColor.GOLD : NamedTextColor.GRAY));
        inventory.setItem(SUMMARY_SLOT, item(playerClass.icon(),
                Component.text(playerClass.displayName(), playerClass.color()).decorate(TextDecoration.BOLD), summary));

        CoreAttribute[] attributes = CoreAttribute.values();
        for (int i = 0; i < attributes.length; i++) {
            inventory.setItem(FIRST_ATTRIBUTE_SLOT + i, attributeItem(attributes[i], progress, playerClass));
        }

        List<Component> abilityLore = new ArrayList<>();
        List<Ability> abilities = plugin.abilities().abilitiesOf(playerClass);
        for (int i = 0; i < abilities.size(); i++) {
            Ability ability = abilities.get(i);
            boolean unlocked = progress.level() >= plugin.abilities().unlockLevel(ability);
            abilityLore.add(line("[" + (i + 1) + "] " + ability.name() + (unlocked ? "" : " (Nv." + plugin.abilities().unlockLevel(ability) + ")"),
                    unlocked ? (ability.isUltimate() ? NamedTextColor.GOLD : NamedTextColor.WHITE) : NamedTextColor.DARK_GRAY));
            for (String description : ability.description()) {
                abilityLore.add(line("    " + description, NamedTextColor.GRAY));
            }
            abilityLore.add(line("    Coste: " + Format.number(plugin.abilities().cost(ability)) + " " + playerClass.resource().displayName()
                    + " · Enfriamiento: " + Format.number(plugin.abilities().cooldownSeconds(ability, stats)) + "s", NamedTextColor.DARK_GRAY));
        }
        abilityLore.add(Component.empty());
        abilityLore.add(line("Pasiva: " + playerClass.passiveName() + " - " + playerClass.passiveDescription(), NamedTextColor.LIGHT_PURPLE));
        inventory.setItem(ABILITIES_SLOT, item(Material.ENCHANTED_BOOK, Component.text("Habilidades", NamedTextColor.AQUA), abilityLore));

        List<Component> statLore = new ArrayList<>();
        for (Map.Entry<Stat, Double> entry : stats.asMap().entrySet()) {
            if (entry.getValue() != 0) {
                statLore.add(line(entry.getKey().displayName() + ": ", NamedTextColor.GRAY)
                        .append(Component.text(Format.stat(entry.getKey(), entry.getValue()), NamedTextColor.WHITE)));
            }
        }
        inventory.setItem(STATS_SLOT, item(Material.BOOK, Component.text("Estadísticas", NamedTextColor.YELLOW), statLore));

        inventory.setItem(CLASS_MENU_SLOT, item(Material.COMPASS, Component.text("Cambiar de clase", NamedTextColor.YELLOW),
                List.of(line("Cada clase guarda su propio nivel.", NamedTextColor.GRAY))));
    }

    private ItemStack attributeItem(CoreAttribute attribute, ClassProgress progress, PlayerClass playerClass) {
        List<Component> lore = new ArrayList<>();
        lore.add(line("Puntos: " + progress.attribute(attribute), NamedTextColor.WHITE));
        lore.add(Component.empty());
        lore.add(line("Cada punto da:", NamedTextColor.GRAY));
        plugin.settings().attributeEffects(attribute).forEach((stat, value) ->
                lore.add(line("  +" + Format.stat(stat, value) + " " + stat.displayName(), NamedTextColor.DARK_AQUA)));
        if (playerClass.mainAttributes().contains(attribute)) {
            lore.add(Component.empty());
            lore.add(line("★ Recomendado para tu clase", NamedTextColor.GOLD));
        }
        lore.add(Component.empty());
        lore.add(line("Click: +1  ·  Shift+Click: +5", NamedTextColor.YELLOW));
        return item(attribute.icon(), Component.text(attribute.displayName(), NamedTextColor.GREEN), lore);
    }

    @Override
    public void onClick(Player player, int slot, ClickType click) {
        if (slot == CLASS_MENU_SLOT) {
            new ClassMenu(plugin).open(player);
            return;
        }
        int index = slot - FIRST_ATTRIBUTE_SLOT;
        if (index < 0 || index >= CoreAttribute.values().length) {
            return;
        }
        PlayerData data = plugin.data().get(player);
        int assigned = plugin.progression().allocate(data, CoreAttribute.values()[index], click.isShiftClick() ? 5 : 1);
        if (assigned == 0) {
            plugin.messages().send(player, "sin-puntos");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1f);
            return;
        }
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.4f);
        draw(player);
    }
}
