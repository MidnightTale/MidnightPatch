package fun.mntale.midnightPatch.module.world.desirepath;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import net.jpountz.lz4.LZ4FrameOutputStream;
import net.jpountz.lz4.LZ4FrameInputStream;

/**
 * Handles reading and writing .mnt region files for desire path data.
 * Each file stores path wear data for a 32x32 chunk region.
 */
public class DesirePathRegionIO {
    public static final int REGION_SIZE = 32; // 32x32 chunks per region

    /**
     * Loads all path data for a region from a .mnt file.
     * @param file The region file
     * @return Map of chunk coords to block wear data
     */
    public static Map<String, Map<String, Integer>> loadRegion(File file) {
        Map<String, Map<String, Integer>> regionData = new HashMap<>();
        if (!file.exists()) return regionData;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new LZ4FrameInputStream(new FileInputStream(file))))) {
            int chunkCount = in.readInt();
            for (int i = 0; i < chunkCount; i++) {
                String chunkKey = in.readUTF();
                int blockCount = in.readInt();
                Map<String, Integer> blockMap = new HashMap<>();
                for (int j = 0; j < blockCount; j++) {
                    String blockKey = in.readUTF();
                    int wear = in.readInt();
                    blockMap.put(blockKey, wear);
                }
                regionData.put(chunkKey, blockMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return regionData;
    }

    /**
     * Saves all path data for a region to a .mnt file.
     * @param file The region file
     * @param regionData Map of chunk coords to block wear data
     */
    public static void saveRegion(File file, Map<String, Map<String, Integer>> regionData) {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new LZ4FrameOutputStream(new FileOutputStream(file))))) {
            out.writeInt(regionData.size());
            for (Map.Entry<String, Map<String, Integer>> chunkEntry : regionData.entrySet()) {
                out.writeUTF(chunkEntry.getKey());
                Map<String, Integer> blockMap = chunkEntry.getValue();
                out.writeInt(blockMap.size());
                for (Map.Entry<String, Integer> blockEntry : blockMap.entrySet()) {
                    out.writeUTF(blockEntry.getKey());
                    out.writeInt(blockEntry.getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Utility: Get region coords from chunk coords
    public static int getRegionCoord(int chunkCoord) {
        return Math.floorDiv(chunkCoord, REGION_SIZE);
    }

    // Utility: Get chunk key ("chunkX,chunkZ")
    public static String chunkKey(int chunkX, int chunkZ) {
        return chunkX + "," + chunkZ;
    }

    // Utility: Get block key ("x,y,z")
    public static String blockKey(int x, int y, int z) {
        return x + "," + y + "," + z;
    }
} 