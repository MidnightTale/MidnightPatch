package fun.mntale.midnightPatch.chunk;

import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import io.github.retrooper.packetevents.util.folia.TaskWrapper;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;

import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class EnderPearlChunkManager implements Listener {
    private final Map<UUID, PearlData> pearlData = new ConcurrentHashMap<>();
    private static final int CHUNK_RADIUS = 1;
    private static final long CHUNK_LOAD_INTERVAL = 50 * 20;
    private static final String DATA_FILE = "pearl_data.dat";

    private record PearlData(UUID playerUuid, Location pearlLocation, Set<ChunkLocation> chunks, Set<TaskWrapper> tasks) {}

    public void shutdown() {
        savePearlData();
        pearlData.values().forEach(data -> data.tasks().forEach(TaskWrapper::cancel));
    }

    private void savePearlData() {
        File dataFile = new File(MidnightPatch.instance.getDataFolder(), DATA_FILE);

        try {
            if (!MidnightPatch.instance.getDataFolder().exists()) {
                MidnightPatch.instance.getDataFolder().mkdirs();
            }

            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(dataFile))) {
                dos.writeInt(pearlData.size());
                pearlData.forEach((pearlUuid, data) -> {
                    try {
                        dos.writeLong(data.playerUuid().getMostSignificantBits());
                        dos.writeLong(data.playerUuid().getLeastSignificantBits());
                        dos.writeLong(pearlUuid.getMostSignificantBits());
                        dos.writeLong(pearlUuid.getLeastSignificantBits());

                        Location loc = data.pearlLocation();
                        dos.writeUTF(loc.getWorld().getName());
                        dos.writeDouble(loc.getX());
                        dos.writeDouble(loc.getY());
                        dos.writeDouble(loc.getZ());
                        dos.writeFloat(loc.getPitch());
                        dos.writeFloat(loc.getYaw());

                        dos.writeInt(data.chunks().size());
                        data.chunks().forEach(chunkLoc -> {
                            try {
                                dos.writeUTF(chunkLoc.world());
                                dos.writeInt(chunkLoc.x());
                                dos.writeInt(chunkLoc.z());
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        } catch (IOException | UncheckedIOException e) {
            MidnightPatch.instance.getLogger().severe("Failed to save data: " + e.getMessage());
        }
    }
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl pearl)) {
            return;
        }

        if (!(pearl.getShooter() instanceof Player player)) {
            return;
        }

        Location center = pearl.getLocation();
        World world = center.getWorld();

        if (world == null) {
            MidnightPatch.instance.getLogger().warning("World is null");
            return;
        }

        int centerX = (int) Math.floor(center.getX() / 16.0);
        int centerZ = (int) Math.floor(center.getZ() / 16.0);

        Set<ChunkLocation> chunks = ConcurrentHashMap.newKeySet();
        Set<TaskWrapper> tasks = ConcurrentHashMap.newKeySet();

        for (int x = -CHUNK_RADIUS; x <= CHUNK_RADIUS; x++) {
            for (int z = -CHUNK_RADIUS; z <= CHUNK_RADIUS; z++) {
                int chunkX = centerX + x;
                int chunkZ = centerZ + z;
                ChunkLocation chunkLoc = new ChunkLocation(world.getName(), chunkX, chunkZ);
                chunks.add(chunkLoc);

                TaskWrapper task = FoliaScheduler.getRegionScheduler().runAtFixedRate(MidnightPatch.instance, new Location(world, chunkX * 16, 0, chunkZ * 16), t -> {
                    try {
                        world.getChunkAtAsync(chunkLoc.x(), chunkLoc.z()).thenAccept(chunk -> {
                            chunk.addPluginChunkTicket(MidnightPatch.instance);
                            // plugin.getLogger().info("Kept chunk loaded for " + player.getName() + "'s pearl at " + chunkLoc);
                        }).exceptionally(throwable -> {
                            MidnightPatch.instance.getLogger().log(Level.SEVERE, "Failed to keep chunk loaded for " + chunkLoc, throwable);
                            return null;
                        });
                    } catch (Exception e) {
                        MidnightPatch.instance.getLogger().log(Level.SEVERE, "Error in chunk loading task for " + chunkLoc, e);
                    }
                }, 0, CHUNK_LOAD_INTERVAL);

                tasks.add(task);
            }
        }

        pearlData.put(pearl.getUniqueId(), new PearlData(player.getUniqueId(), pearl.getLocation(), chunks, tasks));
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof EnderPearl pearl)) {
            return;
        }

        cleanupPearl(pearl.getUniqueId());
    }

    private void cleanupPearl(UUID pearlUuid) {
        PearlData data = pearlData.remove(pearlUuid);
        if (data != null) {
            data.tasks().forEach(TaskWrapper::cancel);

            data.chunks().forEach(chunkLoc -> {
                World world = MidnightPatch.instance.getServer().getWorld(chunkLoc.world());
                if (world != null) {
                    world.getChunkAtAsync(chunkLoc.x(), chunkLoc.z()).thenAccept(chunk -> {
                        chunk.removePluginChunkTicket(MidnightPatch.instance);
                        // plugin.getLogger().info("Removed chunk ticket for " + chunkLoc);
                    }).exceptionally(throwable -> {
                        MidnightPatch.instance.getLogger().log(Level.SEVERE, "Failed to remove chunk ticket for " + chunkLoc, throwable);
                        return null;
                    });
                } else {
                    MidnightPatch.instance.getLogger().warning("World for chunk " + chunkLoc + " is not loaded. Cannot remove ticket.");
                }
            });
        }
    }

    private record ChunkLocation(String world, int x, int z) {}
} 