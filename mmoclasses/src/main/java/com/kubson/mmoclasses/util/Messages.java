package com.kubson.mmoclasses.util;

import com.kubson.mmoclasses.api.PlayerClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

/** Mensajes de config.yml (formato MiniMessage). */
public final class Messages {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private FileConfiguration config;
    private Component prefix = Component.empty();

    public void reload(FileConfiguration config) {
        this.config = config;
        this.prefix = MINI.deserialize(config.getString("mensajes.prefijo", ""));
    }

    public Component get(String key, TagResolver... placeholders) {
        String raw = config.getString("mensajes." + key, "<red>Falta el mensaje '" + key + "' en config.yml");
        return MINI.deserialize(raw, placeholders);
    }

    public void send(CommandSender sender, String key, TagResolver... placeholders) {
        sender.sendMessage(prefix.append(get(key, placeholders)));
    }

    public static TagResolver ph(String key, String value) {
        return Placeholder.unparsed(key, value);
    }

    public static TagResolver ph(String key, Component value) {
        return Placeholder.component(key, value);
    }

    public static TagResolver ph(String key, Number value) {
        return Placeholder.unparsed(key, Format.number(value.doubleValue()));
    }

    public static Component className(PlayerClass playerClass) {
        return Component.text(playerClass.displayName(), playerClass.color());
    }
}
