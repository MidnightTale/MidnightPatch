package fun.mntale.midnightPatch.module.world.desirepath;

import fun.mntale.midnightPatch.MidnightPatch;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.bukkit.World;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DesirePathDataManager {
    private final Map<String, Map<String, Map<String, Integer>>> regionMap = new ConcurrentHashMap<>();
    private final Set<String> dirtyRegions = ConcurrentHashMap.newKeySet();
    private final File dataDir;

    public DesirePathDataManager() {
        File pluginDir = MidnightPatch.instance.getDataFolder();
        dataDir = new File(pluginDir, "desirepaths");
        if (!dataDir.exists()) dataDir.mkdirs();
    }

    public String getRegionKey(World world, int chunkX, int chunkZ) {
        int regionX = DesirePathRegionIO.getRegionCoord(chunkX);
        int regionZ = DesirePathRegionIO.getRegionCoord(chunkZ);
        return world.getName() + ":" + regionX + "," + regionZ;
    }

    public File getRegionFile(World world, int chunkX, int chunkZ) {
        int regionX = DesirePathRegionIO.getRegionCoord(chunkX);
        int regionZ = DesirePathRegionIO.getRegionCoord(chunkZ);
        File worldDir = new File(dataDir, world.getName());
        if (!worldDir.exists()) worldDir.mkdirs();
        return new File(worldDir, regionX + "_" + regionZ + ".mnt");
    }

    public Map<String, Map<String, Integer>> loadRegion(World world, int chunkX, int chunkZ) {
        String regionKey = getRegionKey(world, chunkX, chunkZ);
        if (!regionMap.containsKey(regionKey)) {
            File file = getRegionFile(world, chunkX, chunkZ);
            Map<String, Map<String, Integer>> regionData = DesirePathRegionIO.loadRegion(file);
            regionMap.put(regionKey, regionData);
        }
        return regionMap.get(regionKey);
    }

    public void saveRegionAsync(World world, int chunkX, int chunkZ) {
        String regionKey = getRegionKey(world, chunkX, chunkZ);
        Map<String, Map<String, Integer>> regionData = regionMap.get(regionKey);
        if (regionData != null) {
            Map<String, Map<String, Integer>> regionDataCopy = new ConcurrentHashMap<>();
            for (Map.Entry<String, Map<String, Integer>> entry : regionData.entrySet()) {
                regionDataCopy.put(entry.getKey(), new ConcurrentHashMap<>(entry.getValue()));
            }
            File file = getRegionFile(world, chunkX, chunkZ);
            FoliaScheduler.getAsyncScheduler().runNow(MidnightPatch.instance, (io) -> DesirePathRegionIO.saveRegion(file, regionDataCopy));
            dirtyRegions.remove(regionKey);
        }
    }

    public void markDirty(String regionKey) {
        dirtyRegions.add(regionKey);
    }

    public Set<String> getDirtyRegions() {
        return new HashSet<>(dirtyRegions);
    }

    public Map<String, Map<String, Map<String, Integer>>> getRegionMap() {
        return regionMap;
    }
} 