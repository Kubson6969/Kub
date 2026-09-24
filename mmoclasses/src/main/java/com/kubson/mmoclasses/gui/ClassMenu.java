package com.kubson.mmoclasses.gui;

import com.kubson.mmoclasses.MMOClasses;
import com.kubson.mmoclasses.ability.Ability;
import com.kubson.mmoclasses.api.CoreAttribute;
import com.kubson.mmoclasses.api.PlayerClass;
import com.kubson.mmoclasses.data.ClassProgress;
import com.kubson.mmoclasses.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Menú para elegir o cambiar de clase. */
public final class ClassMenu extends Menu {

    private static final int[] SLOTS = {10, 12, 14, 16};

    private final MMOClasses plugin;

    public ClassMenu(MMOClasses plugin) {
        super(3, Component.text("Elige tu clase", NamedTextColor.DARK_GRAY));
        this.plugin = plugin;
    }

    @Override
    protected void draw(Player viewer) {
        fill();
        PlayerData data = plugin.data().get(viewer);
        PlayerClass[] classes = PlayerClass.values();
        for (int i = 0; i < classes.length && i < SLOTS.length; i++) {
            inventory.setItem(SLOTS[i], item(classes[i].icon(),
                    Component.text(classes[i].displayName(), classes[i].color()).decorate(TextDecoration.BOLD),
                    lore(classes[i], data)));
        }
    }

    private List<Component> lore(PlayerClass playerClass, PlayerData data) {
        List<Component> lore = new ArrayList<>();
        lore.add(line(playerClass.role(), NamedTextColor.GRAY));
        lore.add(Component.empty());
        lore.add(line("Recurso: ", NamedTextColor.GRAY).append(Component.text(playerClass.resource().displayName(), playerClass.resource().color())));
        lore.add(line("Armas: " + String.join(", ", plugin.settings().classSettings(playerClass).weapons()), NamedTextColor.GRAY));
        lore.add(line("Atributos clave: " + playerClass.mainAttributes().stream()
                .map(CoreAttribute::displayName).collect(Collectors.joining(", ")), NamedTextColor.GRAY));
        lore.add(Component.empty());
        lore.add(line("Pasiva: " + playerClass.passiveName(), NamedTextColor.LIGHT_PURPLE));
        lore.add(line("  " + playerClass.passiveDescription(), NamedTextColor.DARK_GRAY));
        lore.add(line("Habilidades:", NamedTextColor.AQUA));
        List<Ability> abilities = plugin.abilities().abilitiesOf(playerClass);
        for (int i = 0; i < abilities.size(); i++) {
            Ability ability = abilities.get(i);
            lore.add(line("  " + (i + 1) + ". " + ability.name() + " (Nv." + plugin.abilities().unlockLevel(ability) + ")",
                    ability.isUltimate() ? NamedTextColor.GOLD : NamedTextColor.WHITE));
        }
        lore.add(Component.empty());
        ClassProgress progress = data.existingProgress(playerClass);
        if (data.currentClass() == playerClass) {
            lore.add(line("✔ Tu clase actual (Nv." + data.level() + ")", NamedTextColor.GREEN));
        } else {
            if (progress != null) {
                lore.add(line("Tu progreso: Nv." + progress.level(), NamedTextColor.YELLOW));
            }
            lore.add(line("Click para elegir", NamedTextColor.YELLOW));
        }
        return lore;
    }

    @Override
    public void onClick(Player player, int slot, ClickType click) {
        PlayerClass[] classes = PlayerClass.values();
        for (int i = 0; i < SLOTS.length && i < classes.length; i++) {
            if (SLOTS[i] == slot) {
                PlayerData data = plugin.data().get(player);
                if (plugin.progression().chooseClass(player, data, classes[i])) {
                    player.closeInventory();
                }
                return;
            }
        }
    }
}
