package fun.mntale.midnightPatch.module.world.loot;

import fun.mntale.midnightPatch.MidnightPatch;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.bukkit.Location;
import org.bukkit.World;
import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChestLootGenDataManager {
    private final Map<String, Set<String>> generatedChests = new ConcurrentHashMap<>();
    private final File dataDir;

    public ChestLootGenDataManager() {
        File pluginDir = MidnightPatch.instance.getDataFolder();
        dataDir = new File(pluginDir, "lootgen");
        if (!dataDir.exists()) dataDir.mkdirs();
    }

    private String getRegionKey(World world, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 32);
        int regionZ = Math.floorDiv(chunkZ, 32);
        return world.getName() + ":" + regionX + "," + regionZ;
    }

    private File getRegionFile(World world, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 32);
        int regionZ = Math.floorDiv(chunkZ, 32);
        File worldDir = new File(dataDir, world.getName());
        if (!worldDir.exists()) worldDir.mkdirs();
        return new File(worldDir, regionX + "_" + regionZ + ".mnt");
    }

    private String blockKey(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    private Set<String> loadRegion(World world, int chunkX, int chunkZ) {
        String regionKey = getRegionKey(world, chunkX, chunkZ);
        if (!generatedChests.containsKey(regionKey)) {
            File file = getRegionFile(world, chunkX, chunkZ);
            Set<String> regionData = ConcurrentHashMap.newKeySet();
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        regionData.add(line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            generatedChests.put(regionKey, regionData);
        }
        return generatedChests.get(regionKey);
    }

    private void saveRegionAsync(World world, int chunkX, int chunkZ) {
        String regionKey = getRegionKey(world, chunkX, chunkZ);
        Set<String> regionData = generatedChests.get(regionKey);
        if (regionData != null) {
            File file = getRegionFile(world, chunkX, chunkZ);
            Set<String> regionDataCopy = ConcurrentHashMap.newKeySet();
            regionDataCopy.addAll(regionData);
            FoliaScheduler.getAsyncScheduler().runNow(MidnightPatch.instance, (io) -> {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    for (String key : regionDataCopy) {
                        writer.write(key);
                        writer.newLine();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public boolean isChestGenerated(Location loc) {
        World world = loc.getWorld();
        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;
        final boolean[] result = {false};
        FoliaScheduler.getRegionScheduler().run(MidnightPatch.instance, loc, (task) -> {
            Set<String> region = loadRegion(world, chunkX, chunkZ);
            String chestKey = blockKey(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            result[0] = region.contains(chestKey);
        });
        return result[0];
    }

    public void markChestGenerated(Location loc) {
        World world = loc.getWorld();
        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;
        FoliaScheduler.getRegionScheduler().run(MidnightPatch.instance, loc, (task) -> {
            Set<String> region = loadRegion(world, chunkX, chunkZ);
            String chestKey = blockKey(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            if (region.add(chestKey)) {
                saveRegionAsync(world, chunkX, chunkZ);
            }
        });
    }

    public void removeChest(Location loc) {
        World world = loc.getWorld();
        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;
        FoliaScheduler.getRegionScheduler().run(MidnightPatch.instance, loc, (task) -> {
            Set<String> region = loadRegion(world, chunkX, chunkZ);
            String chestKey = blockKey(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            if (region.remove(chestKey)) {
                saveRegionAsync(world, chunkX, chunkZ);
            }
        });
    }
} 