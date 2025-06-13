package fun.mntale.midnightPatch.chunk;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.Plugin;

import io.github.retrooper.packetevents.util.folia.TaskWrapper;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;

import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class EnderPearlChunkManager implements Listener {
    private final Plugin plugin;
    private final Map<UUID, PearlData> pearlData = new ConcurrentHashMap<>();
    private static final int CHUNK_RADIUS = 1; // 3x3 area
    private static final long CHUNK_LOAD_INTERVAL = 50 * 20; // 30 second in ticks
    private static final String DATA_FILE = "pearl_data.dat"; // Changed to .dat extension

    private record PearlData(UUID playerUuid, Location pearlLocation, Set<ChunkLocation> chunks, Set<TaskWrapper> tasks) {}

    public EnderPearlChunkManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadPearlData();
    }

    public void shutdown() {
        savePearlData();
        pearlData.values().forEach(data -> data.tasks().forEach(TaskWrapper::cancel));
    }

    private void savePearlData() {
        File dataFile = new File(plugin.getDataFolder(), DATA_FILE);

        try {
            // Ensure plugin data folder exists
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(dataFile))) {
                dos.writeInt(pearlData.size()); // Write number of entries
                pearlData.forEach((pearlUuid, data) -> {
                    try {
                        dos.writeLong(data.playerUuid().getMostSignificantBits());
                        dos.writeLong(data.playerUuid().getLeastSignificantBits());
                        dos.writeLong(pearlUuid.getMostSignificantBits());
                        dos.writeLong(pearlUuid.getLeastSignificantBits());

                        // Write Location data
                        Location loc = data.pearlLocation();
                        dos.writeUTF(loc.getWorld().getName());
                        dos.writeDouble(loc.getX());
                        dos.writeDouble(loc.getY());
                        dos.writeDouble(loc.getZ());
                        dos.writeFloat(loc.getPitch());
                        dos.writeFloat(loc.getYaw());

                        // Write ChunkLocation data
                        dos.writeInt(data.chunks().size());
                        data.chunks().forEach(chunkLoc -> {
                            try {
                                dos.writeUTF(chunkLoc.world());
                                dos.writeInt(chunkLoc.x());
                                dos.writeInt(chunkLoc.z());
                            } catch (IOException e) {
                                throw new UncheckedIOException(e); // Propagate checked exception as unchecked
                            }
                        });
                    } catch (IOException e) {
                        throw new UncheckedIOException(e); // Propagate checked exception as unchecked
                    }
                });
            }
            // plugin.getLogger().info("Successfully saved pearl data to " + DATA_FILE);
        } catch (IOException | UncheckedIOException e) {
            plugin.getLogger().severe("Failed to save pearl data: " + e.getMessage());
        }
    }

    private void loadPearlData() {
        File dataFile = new File(plugin.getDataFolder(), DATA_FILE);
        if (!dataFile.exists()) {
            return;
        }

        try (DataInputStream dis = new DataInputStream(new FileInputStream(dataFile))) {
            int numEntries = dis.readInt();
            // plugin.getLogger().info("Loading " + numEntries + " saved pearl data entries from " + DATA_FILE);

            for (int i = 0; i < numEntries; i++) {
                try {
                    UUID playerUuid = new UUID(dis.readLong(), dis.readLong());
                    UUID pearlUuid = new UUID(dis.readLong(), dis.readLong());

                    // Read Location data
                    String worldName = dis.readUTF();
                    World world = plugin.getServer().getWorld(worldName);
                    if (world == null) {
                        plugin.getLogger().warning("World '" + worldName + "' for saved pearl " + pearlUuid + " is not loaded. Skipping chunk loading.");
                        // Skip remaining data for this entry if world is not found
                        int numChunks = dis.readInt();
                        for (int j = 0; j < numChunks; j++) {
                            dis.readUTF(); // chunk world
                            dis.readInt(); // chunk x
                            dis.readInt(); // chunk z
                        }
                        continue;
                    }
                    Location pearlLocation = new Location(world, dis.readDouble(), dis.readDouble(), dis.readDouble(), dis.readFloat(), dis.readFloat());

                    // Read ChunkLocation data
                    Set<ChunkLocation> chunks = ConcurrentHashMap.newKeySet();
                    int numChunks = dis.readInt();
                    for (int j = 0; j < numChunks; j++) {
                        String chunkWorldName = dis.readUTF();
                        int chunkX = dis.readInt();
                        int chunkZ = dis.readInt();
                        chunks.add(new ChunkLocation(chunkWorldName, chunkX, chunkZ));
                    }

                    resumePearlData(playerUuid, pearlLocation, chunks, pearlUuid);

                } catch (EOFException e) {
                    plugin.getLogger().warning("Reached end of file unexpectedly while loading pearl data. File might be corrupted or incomplete.");
                    break; // Stop reading if EOF is reached prematurely
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error reading pearl data entry", e);
                    break; // Stop reading on other IO errors
                }
            }
            // plugin.getLogger().info("Successfully loaded pearl data from " + DATA_FILE);
        } catch (FileNotFoundException e) {
            // File does not exist, which is fine on first run
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load pearl data: " + e.getMessage());
        }
    }

    private void resumePearlData(UUID playerUuid, Location pearlLocation, Set<ChunkLocation> chunksToLoad, UUID pearlUuidKey) {
        Set<TaskWrapper> tasks = ConcurrentHashMap.newKeySet();
        chunksToLoad.forEach(chunkLoc -> {
            World world = plugin.getServer().getWorld(chunkLoc.world());
            if (world != null) {
                // Schedule task on the specific chunk's region
                TaskWrapper task = FoliaScheduler.getRegionScheduler()
                    .runAtFixedRate(plugin, new Location(world, chunkLoc.x() * 16, 0, chunkLoc.z() * 16), t -> {
                        try {
                            world.getChunkAtAsync(chunkLoc.x(), chunkLoc.z()).thenAccept(chunk -> {
                                chunk.addPluginChunkTicket(plugin);
                                // plugin.getLogger().info("Kept chunk loaded for " + playerUuid + "'s pearl at " + chunkLoc);
                            });
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.SEVERE, "Error in chunk loading task for " + chunkLoc, e);
                        }
                    }, 0, CHUNK_LOAD_INTERVAL);
                tasks.add(task);
            }
        });
        // Re-add to pearlData map so tasks can be cancelled on shutdown
        pearlData.put(pearlUuidKey, new PearlData(playerUuid, pearlLocation, chunksToLoad, tasks));
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl)) {
            return;
        }

        EnderPearl pearl = (EnderPearl) event.getEntity();
        if (!(pearl.getShooter() instanceof Player)) {
            return;
        }

        Player player = (Player) pearl.getShooter();
        Location center = pearl.getLocation();
        World world = center.getWorld();

        if (world == null) {
            plugin.getLogger().warning("Cannot load chunks: World is null");
            return;
        }

        // Calculate chunk coordinates
        int centerX = (int) Math.floor(center.getX() / 16.0);
        int centerZ = (int) Math.floor(center.getZ() / 16.0);

        // Log the pearl launch
        // plugin.getLogger().info("Ender pearl launched by " + player.getName() + " at " + center);
        // plugin.getLogger().info("Loading chunks around chunk coordinates: " + centerX + ", " + centerZ);

        Set<ChunkLocation> chunks = ConcurrentHashMap.newKeySet();
        Set<TaskWrapper> tasks = ConcurrentHashMap.newKeySet();

        // Load chunks in a 3x3 area around the launch location
        for (int x = -CHUNK_RADIUS; x <= CHUNK_RADIUS; x++) {
            for (int z = -CHUNK_RADIUS; z <= CHUNK_RADIUS; z++) {
                int chunkX = centerX + x;
                int chunkZ = centerZ + z;
                ChunkLocation chunkLoc = new ChunkLocation(world.getName(), chunkX, chunkZ);
                chunks.add(chunkLoc);

                // Create a task for each chunk that keeps loading it
                TaskWrapper task = FoliaScheduler.getRegionScheduler().runAtFixedRate(plugin, new Location(world, chunkX * 16, 0, chunkZ * 16), t -> {
                    try {
                        world.getChunkAtAsync(chunkLoc.x(), chunkLoc.z()).thenAccept(chunk -> {
                            chunk.addPluginChunkTicket(plugin);
                            // plugin.getLogger().info("Kept chunk loaded for " + player.getName() + "'s pearl at " + chunkLoc);
                        }).exceptionally(throwable -> {
                            plugin.getLogger().log(Level.SEVERE, "Failed to keep chunk loaded for " + chunkLoc, throwable);
                            return null;
                        });
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.SEVERE, "Error in chunk loading task for " + chunkLoc, e);
                    }
                }, 0, CHUNK_LOAD_INTERVAL);

                tasks.add(task);
            }
        }

        pearlData.put(pearl.getUniqueId(), new PearlData(player.getUniqueId(), pearl.getLocation(), chunks, tasks));
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof EnderPearl)) {
            return;
        }

        EnderPearl pearl = (EnderPearl) event.getEntity();
        cleanupPearl(pearl.getUniqueId());
    }

    private void cleanupPearl(UUID pearlUuid) {
        PearlData data = pearlData.remove(pearlUuid);
        if (data != null) {
            // Cancel all tasks for this pearl
            data.tasks().forEach(TaskWrapper::cancel);
            // plugin.getLogger().info("Cancelled chunk loading tasks for pearl with UUID " + pearlUuid);

            // Remove chunks from tracking
            data.chunks().forEach(chunkLoc -> {
                World world = plugin.getServer().getWorld(chunkLoc.world());
                if (world != null) {
                    world.getChunkAtAsync(chunkLoc.x(), chunkLoc.z()).thenAccept(chunk -> {
                        chunk.removePluginChunkTicket(plugin);
                        // plugin.getLogger().info("Removed chunk ticket for " + chunkLoc);
                    }).exceptionally(throwable -> {
                        plugin.getLogger().log(Level.SEVERE, "Failed to remove chunk ticket for " + chunkLoc, throwable);
                        return null;
                    });
                } else {
                    plugin.getLogger().warning("World for chunk " + chunkLoc + " is not loaded. Cannot remove ticket.");
                }
            });
        }
    }

    private record ChunkLocation(String world, int x, int z) {}
} 