package fun.mntale.midnightPatch.command;

/**
 * /task Command Usage
 *
 * Automate repeated attack (left-click) or interact (right-click) actions for your player at a specified interval (in ticks).
 *
 * Subcommands:
 *   /task attack start <interval>
 *     - Repeatedly swings your arm (left-click) every <interval> ticks.
 *     - If an entity is in your crosshair (within 4 blocks), it will be attacked.
 *     - The arm swing animation will always play, even if there is no target.
 *     - Uses EntityInteractUtil for entity attack logic.
 *   /task attack stop
 *     - Stops the repeated attack task.
 *
 *   /task interact start <interval>
 *     - Repeatedly simulates a right-click in the air (use item) every <interval> ticks.
 *     - Like holding down right-click with an item (e.g., eating, using a bow, using a shield).
 *     - This now uses direct NMS packets (InteractUtil) for best compatibility.
 *   /task interact stop
 *     - Stops the repeated interact task.
 *
 *   /task use start <interval>
 *     - Repeatedly simulates a right-click in the air (use item) every <interval> ticks.
 *     - Like holding down right-click with an item (e.g., eating, using a bow, using a shield).
 *     - This uses direct NMS packets (ItemInteractUtil) for best compatibility.
 *   /task use stop
 *     - Stops the repeated use task.
 *
 *   /task place start <interval>
 *     - Repeatedly places blocks every <interval> ticks.
 *   /task place stop
 *     - Stops the repeated place task.
 *
 *   /task break start <interval>
 *     - Repeatedly breaks blocks every <interval> ticks.
 *   /task break stop
 *     - Stops the repeated break task.
 *
 * Examples:
 *   /task attack start 20      (attack every second)
 *   /task attack stop
 *   /task interact start 10    (right-click every half second)
 *   /task interact stop
 *
 * Notes:
 *   - Only one attack or interact task can run per player at a time.
 *   - You must use /task attack stop or /task interact stop before starting a new task of the same type.
 *   - The interval is in ticks (20 ticks = 1 second).
 *   - You must be a player to use this command (not console).
 *
 * Usage Summary:
 *   /task <attack|interact|use|place|break> <start <interval>|stop>
 */
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fun.mntale.midnightPatch.module.entity.player.task.PlayerTaskManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TaskCommand implements BasicCommand {
    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /task <attack|interact|use|place|break> <start <interval>|stop>");
            return;
        }
        String sub = args[0].toLowerCase();
        String action = args[1].toLowerCase();
        switch (sub) {
            case "attack" -> handleAttack(player, action, args, sender);
            case "interact" -> handleInteract(player, action, args, sender);
            case "use" -> handleUse(player, action, args, sender);
            case "place" -> handlePlace(player, action, args, sender);
            case "break" -> handleBreak(player, action, args, sender);
            default -> sender.sendMessage("Usage: /task <attack|interact|use|place|break> <start <interval>|stop>");
        }
    }

    private void handleAttack(Player player, String action, String[] args, CommandSender sender) {
        if (action.equals("start")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /task attack start <interval>");
                return;
            }
            int interval;
            try {
                interval = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Interval must be a number (ticks). Example: /task attack start 20");
                return;
            }
            if (PlayerTaskManager.isAttackTaskRunning(player)) {
                sender.sendMessage("Attack task already running. Use /task attack stop first.");
                return;
            }
            PlayerTaskManager.startAttackTask(player, interval);
            sender.sendMessage("Started attack task every " + interval + " ticks.");
        } else if (action.equals("stop")) {
            if (PlayerTaskManager.isAttackTaskRunning(player)) {
                PlayerTaskManager.stopAttackTask(player);
                sender.sendMessage("Stopped attack task.");
            } else {
                sender.sendMessage("No attack task running.");
            }
        } else {
            sender.sendMessage("Usage: /task attack <start <interval>|stop>");
        }
    }

    private void handleInteract(Player player, String action, String[] args, CommandSender sender) {
        if (action.equals("start")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /task interact start <interval>");
                return;
            }
            int interval;
            try {
                interval = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Interval must be a number (ticks). Example: /task interact start 20");
                return;
            }
            if (PlayerTaskManager.isInteractTaskRunning(player)) {
                sender.sendMessage("Interact task already running. Use /task interact stop first.");
                return;
            }
            PlayerTaskManager.startInteractTask(player, interval);
            sender.sendMessage("Started interact task every " + interval + " ticks.");
        } else if (action.equals("stop")) {
            if (PlayerTaskManager.isInteractTaskRunning(player)) {
                PlayerTaskManager.stopInteractTask(player);
                sender.sendMessage("Stopped interact task.");
            } else {
                sender.sendMessage("No interact task running.");
            }
        } else {
            sender.sendMessage("Usage: /task interact <start <interval>|stop>");
        }
    }

    private void handleUse(Player player, String action, String[] args, CommandSender sender) {
        if (action.equals("start")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /task use start <interval>");
                return;
            }
            int interval;
            try {
                interval = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Interval must be a number (ticks). Example: /task use start 20");
                return;
            }
            if (PlayerTaskManager.isUseTaskRunning(player)) {
                sender.sendMessage("Use task already running. Use /task use stop first.");
                return;
            }
            PlayerTaskManager.startUseTask(player, interval);
            sender.sendMessage("Started use task every " + interval + " ticks.");
        } else if (action.equals("stop")) {
            if (PlayerTaskManager.isUseTaskRunning(player)) {
                PlayerTaskManager.stopUseTask(player);
                sender.sendMessage("Stopped use task.");
            } else {
                sender.sendMessage("No use task running.");
            }
        } else {
            sender.sendMessage("Usage: /task use <start <interval>|stop>");
        }
    }

    private void handlePlace(Player player, String action, String[] args, CommandSender sender) {
        if (action.equals("start")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /task place start <interval>");
                return;
            }
            int interval;
            try {
                interval = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Interval must be a number (ticks). Example: /task place start 20");
                return;
            }
            if (PlayerTaskManager.isPlaceTaskRunning(player)) {
                sender.sendMessage("Place task already running. Use /task place stop first.");
                return;
            }
            PlayerTaskManager.startPlaceTask(player, interval);
            sender.sendMessage("Started place task every " + interval + " ticks.");
        } else if (action.equals("stop")) {
            if (PlayerTaskManager.isPlaceTaskRunning(player)) {
                PlayerTaskManager.stopPlaceTask(player);
                sender.sendMessage("Stopped place task.");
            } else {
                sender.sendMessage("No place task running.");
            }
        } else {
            sender.sendMessage("Usage: /task place <start <interval>|stop>");
        }
    }

    private void handleBreak(Player player, String action, String[] args, CommandSender sender) {
        if (action.equals("start")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /task break start <interval>");
                return;
            }
            int interval;
            try {
                interval = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Interval must be a number (ticks). Example: /task break start 20");
                return;
            }
            if (PlayerTaskManager.isBreakTaskRunning(player)) {
                sender.sendMessage("Break task already running. Use /task break stop first.");
                return;
            }
            PlayerTaskManager.startBreakTask(player, interval);
            sender.sendMessage("Started break task every " + interval + " ticks.");
        } else if (action.equals("stop")) {
            if (PlayerTaskManager.isBreakTaskRunning(player)) {
                PlayerTaskManager.stopBreakTask(player);
                sender.sendMessage("Stopped break task.");
            } else {
                sender.sendMessage("No break task running.");
            }
        } else {
            sender.sendMessage("Usage: /task break <start <interval>|stop>");
        }
    }

    @Override
    public @Nullable String permission() {
        return "midnightpatch.task";
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 0) {
            suggestions.add("attack");
            suggestions.add("interact");
            suggestions.add("use");
            suggestions.add("place");
            suggestions.add("break");
        } else if (args.length == 1) {
            if ("attack".startsWith(args[0].toLowerCase())) suggestions.add("attack");
            if ("interact".startsWith(args[0].toLowerCase())) suggestions.add("interact");
            if ("use".startsWith(args[0].toLowerCase())) suggestions.add("use");
            if ("place".startsWith(args[0].toLowerCase())) suggestions.add("place");
            if ("break".startsWith(args[0].toLowerCase())) suggestions.add("break");
        } else if (args.length == 2) {
            if ("start".startsWith(args[1].toLowerCase())) suggestions.add("start");
            if ("stop".startsWith(args[1].toLowerCase())) suggestions.add("stop");
        } else if (args.length == 3 && args[1].equalsIgnoreCase("start")) {
            suggestions.add("20");
            suggestions.add("10");
            suggestions.add("5");
        }
        return suggestions;
    }
} 