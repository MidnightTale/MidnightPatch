package fun.mntale.midnightPatch.chunk;

import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import io.github.retrooper.packetevents.util.folia.FoliaScheduler;

import java.io.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MinecartChunkManager implements Listener {
    private static final int CHUNK_RADIUS = 1; // Load chunks in 1-chunk radius around minecart
    private static final String DATA_FILE = "minecart_data.dat";
    private static final double MOVEMENT_THRESHOLD = 8.0; // Only update if moved more than 8 blocks
    
    private final Map<UUID, MinecartData> activeMinecarts = new ConcurrentHashMap<>();

    public MinecartChunkManager() {
        loadMinecartData();
    }

    public void shutdown() {
        saveMinecartData();
    }

    private void saveMinecartData() {
        File dataFile = new File(MidnightPatch.instance.getDataFolder(), DATA_FILE);

        try {
            if (!MidnightPatch.instance.getDataFolder().exists()) {
                MidnightPatch.instance.getDataFolder().mkdirs();
            }

            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(dataFile))) {
                dos.writeInt(activeMinecarts.size());
                activeMinecarts.forEach((minecartUuid, data) -> {
                    try {
                        dos.writeLong(minecartUuid.getMostSignificantBits());
                        dos.writeLong(minecartUuid.getLeastSignificantBits());
                        dos.writeUTF(data.worldName());
                        dos.writeDouble(data.x());
                        dos.writeDouble(data.y());
                        dos.writeDouble(data.z());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        } catch (IOException | UncheckedIOException e) {
            MidnightPatch.instance.getLogger().severe("Failed to save minecart data: " + e.getMessage());
        }
    }

    private void loadMinecartData() {
        File dataFile = new File(MidnightPatch.instance.getDataFolder(), DATA_FILE);
        if (!dataFile.exists()) {
            return;
        }

        try (DataInputStream dis = new DataInputStream(new FileInputStream(dataFile))) {
            int count = dis.readInt();
            for (int i = 0; i < count; i++) {
                String worldName = dis.readUTF();
                double x = dis.readDouble();
                double y = dis.readDouble();
                double z = dis.readDouble();
                
                // Load chunks around the saved location using region scheduler
                World world = MidnightPatch.instance.getServer().getWorld(worldName);
                if (world != null) {
                    loadChunksAroundLocation(world, x, y, z);
                }
            }
        } catch (IOException e) {
            MidnightPatch.instance.getLogger().severe("Failed to load minecart data: " + e.getMessage());
        }
    }

    private void loadChunksAroundLocation(World world, double x, double y, double z) {
        // Use region scheduler for chunk operations
        FoliaScheduler.getRegionScheduler().run(MidnightPatch.instance, new Location(world, x, y, z), task -> {
            int centerX = (int) Math.floor(x / 16.0);
            int centerZ = (int) Math.floor(z / 16.0);

            // Load chunks in a cross pattern instead of full square for better performance
            for (int offset = -CHUNK_RADIUS; offset <= CHUNK_RADIUS; offset++) {
                // Center row
                world.getChunkAtAsync(centerX + offset, centerZ);
                // Center column (skip center to avoid duplicate)
                if (offset != 0) {
                    world.getChunkAtAsync(centerX, centerZ + offset);
                }
            }
        });
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getVehicle() instanceof Minecart minecart)) {
            return;
        }

        // Stop tracking when someone enters (since we only want empty minecarts)
        activeMinecarts.remove(minecart.getUniqueId());
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (!(event.getVehicle() instanceof Minecart minecart)) {
            return;
        }

        // Start tracking when minecart becomes empty
        if (minecart.getPassengers().isEmpty()) {
            Location loc = minecart.getLocation();
            activeMinecarts.put(minecart.getUniqueId(), new MinecartData(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()));
        }
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Minecart minecart)) {
            return;
        }

        // Only process empty minecarts
        if (!minecart.getPassengers().isEmpty()) {
            return;
        }

        Location newLocation = minecart.getLocation();
        World world = newLocation.getWorld();

        if (world == null) {
            return;
        }

        // Check if minecart has moved significantly
        MinecartData currentData = activeMinecarts.get(minecart.getUniqueId());
        if (currentData != null) {
            double distance = Math.sqrt(
                Math.pow(newLocation.getX() - currentData.x(), 2) +
                Math.pow(newLocation.getZ() - currentData.z(), 2)
            );
            
            // Only update if moved more than threshold
            if (distance < MOVEMENT_THRESHOLD) {
                return;
            }
        }

        // Update tracked location
        activeMinecarts.put(minecart.getUniqueId(), new MinecartData(world.getName(), newLocation.getX(), newLocation.getY(), newLocation.getZ()));

        // Load chunks around the minecart
        loadChunksAroundLocation(world, newLocation.getX(), newLocation.getY(), newLocation.getZ());
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (!(event.getVehicle() instanceof Minecart minecart)) {
            return;
        }

        activeMinecarts.remove(minecart.getUniqueId());
    }

    private record MinecartData(String worldName, double x, double y, double z) {}
} 