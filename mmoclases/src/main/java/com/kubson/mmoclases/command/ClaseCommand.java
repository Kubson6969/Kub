package com.kubson.mmoclases.command;

import com.kubson.mmoclases.MMOClases;
import com.kubson.mmoclases.ability.Ability;
import com.kubson.mmoclases.api.CoreAttribute;
import com.kubson.mmoclases.api.PlayerClass;
import com.kubson.mmoclases.api.Stat;
import com.kubson.mmoclases.data.ClassProgress;
import com.kubson.mmoclases.data.PlayerData;
import com.kubson.mmoclases.gui.ClassMenu;
import com.kubson.mmoclases.gui.StatsMenu;
import com.kubson.mmoclases.stats.PlayerStats;
import com.kubson.mmoclases.util.Format;
import com.kubson.mmoclases.util.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** /clase [menu|elegir|info|stats|asignar|habilidades|lanzar|modo|admin] */
public final class ClaseCommand implements TabExecutor {

    private static final List<String> SUBCOMMANDS = List.of("menu", "elegir", "info", "stats", "asignar", "habilidades", "lanzar", "modo");
    private static final List<String> ADMIN_SUBCOMMANDS = List.of("setnivel", "darxp", "reset", "recargar");

    private final MMOClases plugin;

    public ClaseCommand(MMOClases plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String sub = args.length == 0 ? "menu" : args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("admin")) {
            admin(sender, args);
            return true;
        }
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "solo-jugadores");
            return true;
        }
        if (!player.hasPermission("mmoclases.use")) {
            plugin.messages().send(player, "sin-permiso");
            return true;
        }
        PlayerData data = plugin.data().get(player);
        switch (sub) {
            case "menu" -> {
                if (data.hasClass()) {
                    new StatsMenu(plugin).open(player);
                } else {
                    new ClassMenu(plugin).open(player);
                }
            }
            case "elegir" -> choose(player, data, args);
            case "info" -> info(player, data);
            case "stats" -> {
                if (requireClass(player, data)) {
                    new StatsMenu(plugin).open(player);
                }
            }
            case "asignar" -> allocate(player, data, args);
            case "habilidades" -> abilities(player, data);
            case "lanzar" -> {
                Integer slot = args.length > 1 ? parseInt(args[1]) : null;
                if (slot == null || slot < 1 || slot > 4) {
                    player.sendMessage(Component.text("Uso: /" + label + " lanzar <1-4>", NamedTextColor.RED));
                } else {
                    plugin.abilities().cast(player, slot - 1);
                }
            }
            case "modo" -> {
                if (requireClass(player, data)) {
                    plugin.castMode().setCastMode(player, data, !data.castMode());
                }
            }
            default -> help(player, label);
        }
        return true;
    }

    private void choose(Player player, PlayerData data, String[] args) {
        if (args.length < 2) {
            new ClassMenu(plugin).open(player);
            return;
        }
        PlayerClass playerClass = PlayerClass.fromId(args[1]);
        if (playerClass == null) {
            plugin.messages().send(player, "clase-desconocida", Messages.ph("opciones", classIds()));
            return;
        }
        plugin.progression().chooseClass(player, data, playerClass);
    }

    private void info(Player player, PlayerData data) {
        if (!requireClass(player, data)) {
            return;
        }
        PlayerClass playerClass = data.currentClass();
        ClassProgress progress = data.current();
        PlayerStats stats = plugin.stats().compute(player, data);
        player.sendMessage(Component.text("━━━ ", NamedTextColor.DARK_GRAY)
                .append(Messages.className(playerClass))
                .append(Component.text(" Nv." + progress.level() + " ━━━", NamedTextColor.DARK_GRAY)));
        if (progress.level() < plugin.settings().maxLevel) {
            double needed = plugin.settings().xpToNext(progress.level());
            player.sendMessage(Component.text("XP: " + Format.number(progress.xp()) + "/" + Format.number(needed)
                    + "  " + Format.bar(progress.xp() / needed, 20), NamedTextColor.GREEN));
        }
        player.sendMessage(Component.text("Puntos sin asignar: " + progress.unspentPoints(), NamedTextColor.GOLD));
        StringBuilder attributes = new StringBuilder();
        for (CoreAttribute attribute : CoreAttribute.values()) {
            attributes.append(attribute.displayName()).append(' ').append(progress.attribute(attribute)).append("  ");
        }
        player.sendMessage(Component.text(attributes.toString().trim(), NamedTextColor.AQUA));
        for (Map.Entry<Stat, Double> entry : stats.asMap().entrySet()) {
            if (entry.getValue() != 0) {
                player.sendMessage(Component.text(" • " + entry.getKey().displayName() + ": ", NamedTextColor.GRAY)
                        .append(Component.text(Format.stat(entry.getKey(), entry.getValue()), NamedTextColor.WHITE)));
            }
        }
    }

    private void allocate(Player player, PlayerData data, String[] args) {
        if (!requireClass(player, data)) {
            return;
        }
        if (args.length < 2) {
            new StatsMenu(plugin).open(player);
            return;
        }
        CoreAttribute attribute = CoreAttribute.fromId(args[1]);
        if (attribute == null) {
            plugin.messages().send(player, "atributo-desconocido", Messages.ph("opciones",
                    Arrays.stream(CoreAttribute.values()).map(CoreAttribute::id).collect(Collectors.joining(", "))));
            return;
        }
        Integer amount = args.length > 2 ? parseInt(args[2]) : Integer.valueOf(1);
        if (amount == null || amount <= 0) {
            plugin.messages().send(player, "numero-invalido");
            return;
        }
        int assigned = plugin.progression().allocate(data, attribute, amount);
        if (assigned == 0) {
            plugin.messages().send(player, "sin-puntos");
            return;
        }
        plugin.messages().send(player, "puntos-asignados", Messages.ph("cantidad", assigned),
                Messages.ph("atributo", attribute.displayName()), Messages.ph("restantes", data.current().unspentPoints()));
    }

    private void abilities(Player player, PlayerData data) {
        if (!requireClass(player, data)) {
            return;
        }
        PlayerClass playerClass = data.currentClass();
        PlayerStats stats = plugin.stats().compute(player, data);
        player.sendMessage(Component.text("━━━ Habilidades de ", NamedTextColor.DARK_GRAY)
                .append(Messages.className(playerClass)).append(Component.text(" ━━━", NamedTextColor.DARK_GRAY)));
        List<Ability> abilities = plugin.abilities().abilitiesOf(playerClass);
        for (int i = 0; i < abilities.size(); i++) {
            Ability ability = abilities.get(i);
            int unlock = plugin.abilities().unlockLevel(ability);
            boolean unlocked = data.level() >= unlock;
            player.sendMessage(Component.text("[" + (i + 1) + "] ", NamedTextColor.GRAY)
                    .append(Component.text(ability.name(), unlocked ? NamedTextColor.WHITE : NamedTextColor.DARK_GRAY))
                    .append(Component.text(unlocked ? "" : " (nivel " + unlock + ")", NamedTextColor.RED))
                    .append(Component.text(" · " + Format.number(plugin.abilities().cost(ability)) + " " + playerClass.resource().displayName()
                            + " · " + Format.number(plugin.abilities().cooldownSeconds(ability, stats)) + "s", NamedTextColor.DARK_GRAY)));
            player.sendMessage(Component.text("    " + String.join(" ", ability.description()), NamedTextColor.GRAY));
        }
        player.sendMessage(Component.text("Pasiva - " + playerClass.passiveName() + ": " + playerClass.passiveDescription(), NamedTextColor.LIGHT_PURPLE));
        player.sendMessage(Component.text("Pulsa F con tu arma para el modo habilidades y usa las teclas 1-4.", NamedTextColor.YELLOW));
    }

    private void admin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmoclases.admin")) {
            plugin.messages().send(sender, "sin-permiso");
            return;
        }
        String action = args.length > 1 ? args[1].toLowerCase(Locale.ROOT) : "";
        if (action.equals("recargar")) {
            plugin.reloadSettings();
            plugin.messages().send(sender, "recargado");
            return;
        }
        if (!ADMIN_SUBCOMMANDS.contains(action) || args.length < 3) {
            sender.sendMessage(Component.text("Uso: /clase admin <setnivel|darxp|reset|recargar> <jugador> [valor]", NamedTextColor.RED));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            plugin.messages().send(sender, "jugador-no-encontrado");
            return;
        }
        PlayerData data = plugin.data().get(target);
        switch (action) {
            case "setnivel" -> {
                Integer level = args.length > 3 ? parseInt(args[3]) : null;
                PlayerClass playerClass = args.length > 4 ? PlayerClass.fromId(args[4]) : data.currentClass();
                if (level == null || playerClass == null) {
                    sender.sendMessage(Component.text("Uso: /clase admin setnivel <jugador> <nivel> [clase]", NamedTextColor.RED));
                    return;
                }
                plugin.progression().setLevel(target, data, playerClass, level);
                plugin.messages().send(sender, "admin-nivel", Messages.ph("jugador", target.getName()),
                        Messages.ph("nivel", data.progress(playerClass).level()));
            }
            case "darxp" -> {
                Integer xp = args.length > 3 ? parseInt(args[3]) : null;
                if (xp == null || xp <= 0) {
                    plugin.messages().send(sender, "numero-invalido");
                    return;
                }
                plugin.progression().giveXp(target, data, xp);
                plugin.messages().send(sender, "admin-xp", Messages.ph("jugador", target.getName()), Messages.ph("xp", xp));
            }
            case "reset" -> {
                data.clearProgress();
                data.setCastMode(false);
                data.setResource(0);
                plugin.stats().applyHealth(target, data, PlayerStats.EMPTY);
                plugin.data().saveAsync(data);
                plugin.messages().send(sender, "admin-reset", Messages.ph("jugador", target.getName()));
            }
            default -> {
            }
        }
    }

    private boolean requireClass(Player player, PlayerData data) {
        if (!data.hasClass()) {
            plugin.messages().send(player, "sin-clase");
            return false;
        }
        return true;
    }

    private void help(Player player, String label) {
        player.sendMessage(Component.text("/" + label + " - menú de clase / personaje", NamedTextColor.GOLD));
        player.sendMessage(Component.text("/" + label + " elegir <clase> · info · stats · habilidades", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/" + label + " asignar <atributo> [cantidad] · lanzar <1-4> · modo", NamedTextColor.YELLOW));
    }

    private static String classIds() {
        return Arrays.stream(PlayerClass.values()).map(PlayerClass::id).collect(Collectors.joining(", "));
    }

    private static @Nullable Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.addAll(SUBCOMMANDS);
            if (sender.hasPermission("mmoclases.admin")) {
                options.add("admin");
            }
        } else if (args.length == 2) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "elegir" -> Arrays.stream(PlayerClass.values()).map(PlayerClass::id).forEach(options::add);
                case "asignar" -> Arrays.stream(CoreAttribute.values()).map(CoreAttribute::id).forEach(options::add);
                case "lanzar" -> options.addAll(List.of("1", "2", "3", "4"));
                case "admin" -> {
                    if (sender.hasPermission("mmoclases.admin")) {
                        options.addAll(ADMIN_SUBCOMMANDS);
                    }
                }
                default -> {
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("admin") && sender.hasPermission("mmoclases.admin")) {
            Bukkit.getOnlinePlayers().forEach(player -> options.add(player.getName()));
        } else if (args.length == 5 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("setnivel")) {
            Arrays.stream(PlayerClass.values()).map(PlayerClass::id).forEach(options::add);
        }
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        return options.stream().filter(option -> option.toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
    }
}
