package fun.mntale.midnightPatch;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import net.kyori.adventure.text.Component;

public class StartupJoinDelayPatch implements Listener {
    public static volatile long START_TIME = 0; // Set from MidnightPatch

    private static final long DELAY_MILLIS = 10000; // 10 seconds

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        long now = System.currentTimeMillis();
        if (START_TIME == 0 || now - START_TIME < DELAY_MILLIS) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("Server is still starting up. Please wait a few seconds and try again."));
        }
    }
} 