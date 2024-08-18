package org.example;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class Test {
    public static void main(String[] args)  {
        var directory = new File(Helper.DATA_DIRECTORY);
        if (directory != null) {
            Arrays.stream(directory.listFiles((f, p) -> p.endsWith(".txt"))).filter(Objects::nonNull).forEach(File::delete);
        }
        LSMTree tree = new LSMTree();
        for (int i = 0 ; i < 56 ; i++) {
            tree.put(String.valueOf(i), String.valueOf(i));
        }
        System.out.println("Value test for 5: "+tree.get("5")); // fetched from segment 1
        System.out.println("Value test for 15: "+tree.get("15")); // fetched from segment 2
        System.out.println("Value test for 25: "+tree.get("25")); // fetched from segment 3
        System.out.println("Value test for 35: "+tree.get("35")); // fetched from segment 4
        System.out.println("Value test for 45: "+tree.get("45")); // fetched from segment 5
        System.out.println("Value test for 55: "+tree.get("55")); // fetched from segment 6
    }
}
