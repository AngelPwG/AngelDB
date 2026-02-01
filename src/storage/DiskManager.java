package storage;

import btree.LeafNode;
import models.Row;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DiskManager {
     private RandomAccessFile file;

     public DiskManager(String fileName){
         try {
             file = new RandomAccessFile(new File(fileName), "rw");
         }catch (IOException e){
             e.printStackTrace();
         }
     }

     private void writeFixedString(ByteBuffer buffer, String value, int maxLength){
         byte[] stringBytes = value.getBytes(StandardCharsets.UTF_8);
         byte[] finalBytes = new byte[maxLength];

         int bytesToCopy = Math.min(stringBytes.length, maxLength);

         System.arraycopy(stringBytes, 0, finalBytes, 0, bytesToCopy);
         buffer.put(finalBytes);
     }
     private String readFixedString(ByteBuffer buffer, int length){
         byte[] rawBytes = new byte[length];
         buffer.get(rawBytes);

         int validLength = 0;
         while (validLength < length && rawBytes[validLength] != 0) {
             validLength++;
         }

         return new String(rawBytes, 0, validLength, StandardCharsets.UTF_8);
     }

     public byte[] serializeLeaf(LeafNode leaf){
         ByteBuffer buffer = ByteBuffer.allocate(4096);

         buffer.put((byte)1);
         buffer.putInt(leaf.keys.size());
         buffer.putLong((leaf.nextPointer != null)? leaf.nextPointer : 0L);
         buffer.putLong(0L);

         for(int i = 0; i < 77; i++){
             if(i < leaf.keys.size())
                 buffer.putLong(leaf.keys.get(i));
             else
                 buffer.putLong(0L);
         }

         for(int i = 0; i < 77; i++){
             if(i < leaf.data.size()){
                 buffer.putLong(leaf.data.get(i).id());
                 writeFixedString(buffer, leaf.data.get(i).name(), 32);
                 buffer.putInt(leaf.data.get(i).age());
             }
             else {
                 buffer.putLong(0L);
                 buffer.put(new byte[32]);
                 buffer.putInt(0);
             }
         }

         return buffer.array();
     }

     public LeafNode deserializeLeaf(byte[] data){
         LeafNode leaf = new LeafNode(39);

         ByteBuffer buffer = ByteBuffer.wrap(data);

         buffer.position(1);
         int keyCount = buffer.getInt();
         leaf.nextPointer = buffer.getLong();
         buffer.getLong();

         for(int i = 0; i < keyCount; i ++){
             leaf.keys.add(buffer.getLong());
         }
         buffer.position(637);
         for(int i = 0; i < keyCount; i ++){
             long id = buffer.getLong();
             String name = readFixedString(buffer, 32);
             int age = buffer.getInt();
             Row record = new Row(
                     id,
                     name,
                     age
             );
             leaf.data.add(record);
         }


         return leaf;
     }

     public void writePage(long pageId, byte[] data){
         try{
             long offset = pageId * 4096;
             file.seek(offset);
             file.write(data);
         }catch (IOException e){
             e.printStackTrace();
         }
     }

     public byte[] readPage(long pageId){
         byte[] data = new byte[4096];
         try{
             long offset = pageId * 4096;
             file.seek(offset);
             file.read(data);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         return data;
     }
}
