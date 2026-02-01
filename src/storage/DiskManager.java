package storage;

import btree.BPlusNode;
import btree.InternalNode;
import btree.LeafNode;
import models.Row;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DiskManager {
     private RandomAccessFile file;
     public long rootPageId;
     long totalPages;

     public DiskManager(String fileName){
         try {
             file = new RandomAccessFile(new File(fileName), "rw");
             if(file.length() == 0){
                 ByteBuffer buffer = ByteBuffer.allocate(4096);
                 buffer.putLong(1L);
                 buffer.putLong(2L);
                 writePage(0, buffer.array());
                 rootPageId = 1L;
                 totalPages = 2L;

                 LeafNode emptyRoot = new LeafNode(39);
                 byte[] rootData = serializeLeaf(emptyRoot);
                 writePage(1, rootData);
             }else{
                 byte[] data = readPage(0);
                 ByteBuffer buffer = ByteBuffer.wrap(data);
                 rootPageId = buffer.getLong();
                 totalPages = buffer.getLong();
             }
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

     public byte[] serializeInternal(InternalNode node){
         ByteBuffer buffer = ByteBuffer.allocate(4096);

         buffer.put((byte)0);
         buffer.putInt(node.keys.size());
         for(int i = 0; i < 77; i++){
             if(i < node.keys.size())
                 buffer.putLong(node.keys.get(i));
             else
                 buffer.putLong(0L);
         }

         for(int i = 0; i < 78; i++){
             if(i < node.childrenIDs.size())
                 buffer.putLong(node.childrenIDs.get(i));
             else
                 buffer.putLong(0L);
         }

         return buffer.array();
     }

     public InternalNode deserializeInternal(byte[] data){
         InternalNode node = new InternalNode(39);

         ByteBuffer buffer = ByteBuffer.wrap(data);

         buffer.position(1);
         int keyCount = buffer.getInt();

         for(int i = 0; i < keyCount; i++){
             node.keys.add(buffer.getLong());
         }

         buffer.position(621);
         for(int i = 0; i < keyCount + 1; i++){
             node.childrenIDs.add(buffer.getLong());
         }

         return node;
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
         buffer.position(636);
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

     public BPlusNode readNode(long pageId){
         byte[] data = readPage(pageId);
         ByteBuffer buffer = ByteBuffer.wrap(data);

         byte isLeaf = buffer.get(0);
         if(isLeaf == 1){
             LeafNode leaf = deserializeLeaf(data);
             leaf.pageId = pageId;
             return leaf;
         }else{
             InternalNode internal = deserializeInternal(data);
             internal.pageId = pageId;
             return  internal;
         }
     }

     public long allocatePage(){
         long oldTotalPages = totalPages;
         totalPages++;

         ByteBuffer buffer = ByteBuffer.allocate(4096);
         buffer.putLong(rootPageId);
         buffer.putLong(totalPages);
         writePage(0, buffer.array());

         return oldTotalPages;
     }

    public void updateRoot(long newRootId) {
        this.rootPageId = newRootId;

        ByteBuffer buffer = ByteBuffer.allocate(4096);
        buffer.putLong(this.rootPageId);
        buffer.putLong(this.totalPages);

        writePage(0, buffer.array());
    }
}
