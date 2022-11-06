package toolbox;

import world.chunk.Chunk;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ChunkHandler implements Serializable {

    public static void saveChunk(Chunk chunk){
        byte[] compressedChunk = compressChunk(chunk);
        try{
            String fileName = String.valueOf(chunk.MIN_X);
            fileName = fileName.concat(String.valueOf(chunk.MIN_Z));
            File directory = new File("world");
            if(!directory.exists()){
                directory.mkdirs();
            }

            File file = new File("world/"+fileName+".txt");
            String filePath = fileName+".txt";
            Path path = file.toPath();
            System.out.println(path);


            AsynchronousFileChannel asyncFile = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            asyncFile.write(ByteBuffer.wrap(compressedChunk), 0);
            asyncFile.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] compressChunk(Chunk chunk){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOut = null;
        try {
            gzipOut = new GZIPOutputStream(baos);
            ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut);
            objectOut.writeObject(chunk);
            objectOut.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }




    public static Chunk unCompressChunk(byte[] bytes){
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        GZIPInputStream gzipIn = null;
        Chunk chunk;
        try {
            gzipIn = new GZIPInputStream(bais);
            ObjectInputStream objectIn = new ObjectInputStream(gzipIn);
            chunk = (Chunk) objectIn.readObject();
            objectIn.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return chunk;
    }



}
