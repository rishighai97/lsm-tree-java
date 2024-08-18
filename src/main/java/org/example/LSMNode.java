package org.example;

import lombok.Getter;

import java.io.File;

@Getter
public class LSMNode {

    private final File segmentFile;

    private final long offset;

    public LSMNode(File segmentFile, long offset) {
        this.segmentFile = segmentFile;
        this.offset = offset;
    }
}
