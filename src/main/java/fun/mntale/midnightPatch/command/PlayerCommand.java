package fun.mntale.midnightPatch.command;

import fun.mntale.midnightPatch.module.entity.player.fakeplayer.FakePlayerFactory;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlayerCommand implements BasicCommand {
    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        CommandSender sender = commandSourceStack.getSender();
        if (args.length < 2) {
            sender.sendMessage("Usage: /player <spawn|remove> <name>");
            return;
        }
        String sub = args[0].toLowerCase();
        String name = args[1];
        switch (sub) {
            case "spawn" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("This command can only be used by a player.");
                    return;
                }
                FakePlayerFactory.create(player.getLocation(), name);
                sender.sendMessage("Spawned fake player: " + name);
            }
            case "remove" -> {
                boolean removed = FakePlayerFactory.remove(name);
                if (removed) {
                    sender.sendMessage("Removed fake player: " + name);
                } else {
                    sender.sendMessage("No fake player found with name: " + name);
                }
            }
            default -> sender.sendMessage("Usage: /player <spawn|remove> <name>");
        }
    }

    @Override
    public @Nullable String permission() {
        return "midnightpatch.player";
    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 0) {
            suggestions.add("spawn");
            suggestions.add("remove");
        } else if (args.length == 1) {
            if ("spawn".startsWith(args[0].toLowerCase())) suggestions.add("spawn");
            if ("remove".startsWith(args[0].toLowerCase())) suggestions.add("remove");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            for (String name : FakePlayerFactory.getFakePlayerNames()) {
                if (name.toLowerCase().startsWith(args[1].toLowerCase())) {
                    suggestions.add(name);
                }
            }
        }
        return suggestions;
    }
} 