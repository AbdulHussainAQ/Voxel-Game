package world.chunk;

import renderEngine.Loader;
import world.World;

import java.util.HashMap;
import java.util.Map;

public class ChunkPool {
    /*

    private static long expTime = 1000;//1 second
    public static HashMap<Chunk, Long> available = new HashMap<>();
    public static HashMap<Chunk, Long> inUse = new HashMap<>();

    private static Loader loader;

    public ChunkPool(Loader loader){
        ChunkPool.loader = loader;
    }

    public synchronized Chunk getObject(World world, int minX, int minZ) {
        long now = System.currentTimeMillis();
        if (!available.isEmpty()) {
            for (Map.Entry<Chunk, Long> entry : available.entrySet()) {
                if (now - entry.getValue() > expTime) { //object has expired
                    popElement(available);
                } else {
                    Chunk po = popElement(available, entry.getKey());
                    push(inUse, po, now);
                    return po;
                }
            }
        }

        // either no PooledObject is available or each has expired, so return a new one
        return createPooledObject(now, world,minX,minZ);
    }

    private synchronized Chunk createPooledObject(long now,World world, int minX, int minZ) {
        Chunk po = new Chunk(world,minX,minZ,loader);
        push(inUse, po, now);
        return po;
    }

    private synchronized void push(HashMap<Chunk, Long> map,
                                   Chunk po, long now) {
        map.put(po, now);
    }

    public void releaseObject(Chunk po) {
        cleanUp(po);
        available.put(po, System.currentTimeMillis());
        inUse.remove(po);
    }

    private Chunk popElement(HashMap<Chunk, Long> map) {
        Map.Entry<Chunk, Long> entry = map.entrySet().iterator().next();
        Chunk key= entry.getKey();
        //Long value=entry.getValue();
        map.remove(entry.getKey());
        return key;
    }

    private Chunk popElement(HashMap<Chunk, Long> map, Chunk key) {
        map.remove(key);
        return key;
    }

    public void cleanUp(Chunk chunk) {
       chunk.setBlockRenderer(null);

    }
    
     */
    
}
