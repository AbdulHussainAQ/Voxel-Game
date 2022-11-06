package models;

/* TODO:
- Create pool of 100 RawModel which will be used for chunks
- Reassign objects when not used
- Save da memory
 */

import renderEngine.Loader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public class RawModelPool {


    private static long expTime = 1000;//1 second
    public static HashMap<RawModel, Long> available = new HashMap<>();
    public static HashMap<RawModel, Long> inUse = new HashMap<>();

    private static Loader loader;

    public RawModelPool(Loader loader){
        RawModelPool.loader = loader;
    }

    public synchronized RawModel getObject(int vaoID, int vertexCount) {
        long now = System.currentTimeMillis();
        if (!available.isEmpty()) {
            for (Map.Entry<RawModel, Long> entry : available.entrySet()) {
                if (now - entry.getValue() > expTime) { //object has expired
                    popElement(available);
                } else {
                    RawModel po = popElement(available, entry.getKey());
                    push(inUse, po, now);
                    return po;
                }
            }
        }

        // either no PooledObject is available or each has expired, so return a new one
        return createPooledObject(now, vaoID, vertexCount);
    }

    private synchronized RawModel createPooledObject(long now,int vaoID, int vertexCount) {
        RawModel po = new RawModel(vaoID, vertexCount);
        push(inUse, po, now);
        return po;
    }

    private synchronized void push(HashMap<RawModel, Long> map,
                                          RawModel po, long now) {
        map.put(po, now);
    }

    public void releaseObject(RawModel po) {
        cleanUp(po);
        available.put(po, System.currentTimeMillis());
        inUse.remove(po);
    }

    private RawModel popElement(HashMap<RawModel, Long> map) {
        Map.Entry<RawModel, Long> entry = map.entrySet().iterator().next();
        RawModel key= entry.getKey();
        //Long value=entry.getValue();
        map.remove(entry.getKey());
        return key;
    }

    private RawModel popElement(HashMap<RawModel, Long> map, RawModel key) {
        map.remove(key);
        return key;
    }

    public void cleanUp(RawModel rawModel) {
        rawModel.setVaoID(null);
        rawModel.setVertexCount(null);
    }

}
