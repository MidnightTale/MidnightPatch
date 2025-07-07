package fun.mntale.midnightPatch.module.entity.player;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PhantomIsolation {
    private static WrappedTask task;

    public static void start(Plugin plugin) {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        task = MidnightPatch.instance.foliaLib.getScheduler().runTimer(
            () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!shouldIsolate(player)) {
                        MidnightPatch.instance.foliaLib.getScheduler().runAtEntity(player, (entityTask) -> {
                            player.setStatistic(Statistic.TIME_SINCE_REST, 0);
                        });
                    }
                }
            },
            1L,
            120L
        );
    }

    // Placeholder for isolation logic, always true for now
    private static boolean shouldIsolate(Player player) {
        return fun.mntale.midnightPatch.command.TogglePhantomIsolationCommand.isPhantomIsolationEnabled(player);
    }
} 