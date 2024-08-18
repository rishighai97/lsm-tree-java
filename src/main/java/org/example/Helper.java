package org.example;

import java.io.File;
import java.util.Objects;

public class Helper {
    private Helper() {
        throw new IllegalStateException("Constant class cannot be instantiated");
    }

    public static final String DATA_DIRECTORY = ".";
    public static final int MEMTABLE_THRESHOLD = 10;

}
