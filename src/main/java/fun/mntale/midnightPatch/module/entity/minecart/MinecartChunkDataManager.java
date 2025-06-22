package fun.mntale.midnightPatch.module.entity.minecart;

import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.World;
import org.bukkit.Location;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import java.io.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MinecartChunkDataManager {
    private final Map<UUID, MinecartChunkData> activeMinecarts = new ConcurrentHashMap<>();

    public Map<UUID, MinecartChunkData> getActiveMinecarts() {
        return activeMinecarts;
    }

    public void saveMinecartData() {
        File dataFile = new File(MidnightPatch.instance.getDataFolder(), MinecartChunkConfig.DATA_FILE);
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

    public void loadMinecartData() {
        File dataFile = new File(MidnightPatch.instance.getDataFolder(), MinecartChunkConfig.DATA_FILE);
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
                World world = MidnightPatch.instance.getServer().getWorld(worldName);
                if (world != null) {
                    loadChunksAroundLocation(world, x, y, z);
                }
            }
        } catch (IOException e) {
            MidnightPatch.instance.getLogger().severe("Failed to load minecart data: " + e.getMessage());
        }
    }

    public void loadChunksAroundLocation(World world, double x, double y, double z) {
        FoliaScheduler.getRegionScheduler().run(MidnightPatch.instance, new Location(world, x, y, z), task -> {
            int centerX = (int) Math.floor(x / 16.0);
            int centerZ = (int) Math.floor(z / 16.0);
            for (int offset = -MinecartChunkConfig.CHUNK_RADIUS; offset <= MinecartChunkConfig.CHUNK_RADIUS; offset++) {
                world.getChunkAtAsync(centerX + offset, centerZ);
                if (offset != 0) {
                    world.getChunkAtAsync(centerX, centerZ + offset);
                }
            }
        });
    }
} 