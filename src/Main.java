import GUI.CompressGUI;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            new CompressGUI();
        } else {
            List<File> files = Arrays.stream(args)
                    .map(File::new)
                    .collect(Collectors.toList());

            if (files.size() > 1) {
                new CompressGUI(files); // compress multiple
            } else {
                File file = files.get(0);
                if (file.getName().toLowerCase().endsWith(".iak")) {
                    Object[] options = {"Decompress", "Compress"};
                    int choice = JOptionPane.showOptionDialog(null,
                            "What do you want to do with " + file.getName() + "?",
                            "Choose Action",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);

                    if (choice == 0) {
                        new CompressGUI(file);
                    } else if (choice == 1) {
                        new CompressGUI(List.of(file));
                    }
                } else {
                    new CompressGUI(List.of(file));
                }
            }
        }
    }
}
