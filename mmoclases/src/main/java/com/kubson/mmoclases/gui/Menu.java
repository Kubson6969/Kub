package com.kubson.mmoclases.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** Menú de inventario básico: el listener cancela todos los clics y llama a {@link #onClick}. */
public abstract class Menu implements InventoryHolder {

    protected final Inventory inventory;

    @SuppressWarnings("this-escape") // Patrón estándar de InventoryHolder: el inventario apunta a su menú.
    protected Menu(int rows, Component title) {
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
    }

    /** Rellena el inventario. Se llama al abrir y tras cada cambio. */
    protected abstract void draw(Player viewer);

    public abstract void onClick(Player player, int slot, ClickType click);

    public void open(Player player) {
        draw(player);
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    protected void fill() {
        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "), List.of());
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
    }

    protected static ItemStack item(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name.decoration(TextDecoration.ITALIC, false));
            List<Component> cleanLore = new ArrayList<>();
            for (Component line : lore) {
                cleanLore.add(line.decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(cleanLore);
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }
        return item;
    }

    protected static Component line(String text, NamedTextColor color) {
        return Component.text(text, color);
    }
}
