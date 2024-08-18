package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;

public class LSMTree {

    private Map<String, String> memTable;
    private Map<String, LSMNode> diskTable;

    private File segmentFile;
    private FileOutputStream writeAheadLog;
    public LSMTree() {
        this.memTable = new TreeMap<>();
        this.diskTable = new TreeMap<>();
        List<Character> segmentFileVersions = getSegmentFileVersions();
        char segmentFileVersion = !segmentFileVersions.isEmpty() ? segmentFileVersions.getLast() : 'a';

        try {
            this.segmentFile = new File(Helper.DATA_DIRECTORY +"/segment_"+segmentFileVersion+".txt");
            this.writeAheadLog =  new FileOutputStream(Helper.DATA_DIRECTORY +"/writeAheadLog.txt");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void put(String key, String value) {
        if (key == null) throw new IllegalStateException("Key cannot be null");
        if (value == null) throw new IllegalStateException("Value cannot be null");
        memTable.put(key, value);
        try {
            updateWriteAheadLog(key, value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (memTable.size() >= Helper.MEMTABLE_THRESHOLD) {
            flushAllToDisk();
        }
    }

    public String get(String key) {
        if (memTable.containsKey(key)) {
            System.out.println(MessageFormat.format("Fetched value for key {0} from memtable", key));
            return memTable.get(key);
        }
        return getValueFromSegmentFile(key);
    }

    private String getValueFromSegmentFile(String key) {
        if (!diskTable.containsKey(key)) return null;
        File currentSegmentFile = diskTable.get(key).getSegmentFile();
        long offset = diskTable.get(key).getOffset();
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(currentSegmentFile, "r")) {
            randomAccessFile.seek(offset);
            String line = randomAccessFile.readLine();
            System.out.println(MessageFormat.format("Fetched value for key {0} from segment {1}", key, currentSegmentFile.getName()));
            return line.split(":")[1].split("\n")[0];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateWriteAheadLog(String key, String value) throws IOException {
        try {
            writeAheadLog.write(getRecord(key, value));
        } catch (IOException e) {
            writeAheadLog.close();
            throw new RuntimeException(e);
        }
    }

    public void flushAllToDisk() {
        for (Map.Entry<String, String> entry : memTable.entrySet()) {
            String key = entry.getKey();
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(segmentFile, "rw")) {
                randomAccessFile.seek(randomAccessFile.length());
                long offset = randomAccessFile.getFilePointer();
                byte[] record = getRecord(key, entry.getValue());
                randomAccessFile.write(record);
                diskTable.put(key, new LSMNode(segmentFile, offset));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        updateSegmentFile();
        this.memTable.clear();
    }

    private void updateSegmentFile() {

        List<Character> fileVersions = getSegmentFileVersions();
        if (!fileVersions.isEmpty()) {
            char latestFileOffset = fileVersions.getLast();
            this.segmentFile = new File(Helper.DATA_DIRECTORY+"/segment_"+(char)(latestFileOffset + 1)+".txt");
        } else {
            throw new IllegalStateException("No segment files present in data directory");
        }
    }

    private static List<Character> getSegmentFileVersions() {
        File dir = new File(Helper.DATA_DIRECTORY);
        return Arrays
                .stream(Objects.requireNonNull(dir.listFiles((dir1, name) -> name.startsWith("segment"))))
                .map(File::getName)
                .map(name -> name.split("\\.")[0].split("_")[1].charAt(0))
                .sorted()
                .toList();
    }

    private static byte[] getRecord(String key, String value) {
        String resultString = key + ":" + value + "\n";
        return resultString.getBytes(StandardCharsets.UTF_8);
    }
}
