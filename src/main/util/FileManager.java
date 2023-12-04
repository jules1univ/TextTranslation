package main.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class FileManager {

    public static void read(File file, Hashtable<String, String> table) {
        List<String> lines = read(file).orElse(new ArrayList<>());
        int length = lines.size() % 2 == 0 ? lines.size() : lines.size() - 1;
        for (int i = 0; i < length; i += 2) {
            table.put(lines.get(i).toLowerCase(), lines.get(i + 1).toLowerCase());
        }
    }

    public static Optional<List<String>> read(File file) {
        try {
            Scanner sc = new Scanner(file, StandardCharsets.UTF_8);
            List<String> lines = new ArrayList<>();

            while (sc.hasNext()) {
                lines.add(sc.nextLine());
            }
            sc.close();

            return Optional.of(lines);
        } catch (IOException ignored) {
        }

        return Optional.empty();
    }

    private static boolean writer(File file, List<String> lines, boolean append) {
        try (FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8, append)) {
            try {
                for (String line : lines) {
                    fileWriter.write(String.format("%s\n", line));
                }
            } finally {
                fileWriter.close();
            }

        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public static boolean append(File file, List<String> lines) {
        return FileManager.writer(file, lines, true);
    }

    public static boolean write(File file, List<String> lines) {
        return FileManager.writer(file, lines, false);

    }

    public static Collector<String, ?, List<String>> write(File file) {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                lines -> {
                    FileManager.writer(file, lines, false);
                    return lines;
                });
    }

    public static boolean writeObj(File file, Object obj) {
        try (final FileOutputStream fout = new FileOutputStream(file);
                final ObjectOutputStream out = new ObjectOutputStream(fout)) {
            out.writeObject(obj);
            out.flush();
            out.close();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static Object readObj(File file) {
        try (final FileInputStream fin = new FileInputStream(file);
                final ObjectInputStream in = new ObjectInputStream(fin)) {
            Object obj = in.readObject();
            in.close();
            return obj;
        } catch (Exception e) {
            return null;
        }
    }
}