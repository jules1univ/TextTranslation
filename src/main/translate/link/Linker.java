package main.translate.link;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import main.util.FileManager;

public class Linker {

    private final File cachedRootFile = new File("lib/traduction/root.trd");
    private final File cachedLinkFile = new File("lib/traduction/link.trd");
    private final File defaultRootFile = new File("lib/racines.txt");

    // private final File cachedRootFile = new File("native/test/root_java_test.txt");
    // private final File cachedLinkFile = new File("native/test/link_java_test.txt");
    // private final File defaultRootFile = new File("native/test/racines.txt");

    HashMap<String, HashSet<String>> treeKeys = new HashMap<>();
    HashMap<String, String> treeValues = new HashMap<>();

    private String[] links = null;

    public Linker() {

    }

    private void groupLinkNodes(String link, HashSet<String> nodes) {
        for (String key : treeValues.keySet()) {
        
            if (key.equals(link)) {
                nodes.add(treeValues.get(key));
                this.groupLinkNodes(treeValues.get(key), nodes);
            }else if (treeValues.get(key).equals(link)) {
                nodes.add(key);
                this.groupLinkNodes(key, nodes);
            }
        }
    }

    private void createLinkTree() {
        List<String> lines = new ArrayList<>();

        for (String key : treeKeys.keySet()) {
            for (String link : treeKeys.get(key)) {
                HashSet<String> nodes = new HashSet<>();
                nodes.add(link);

                this.groupLinkNodes(link, nodes);

                for (String node : nodes) {
                    lines.add(String.format("%s=%s", node, key));
                }
            }
        }
        FileManager.write(cachedLinkFile, lines.parallelStream().sorted().collect(Collectors.toList()));
    }

    private void createRootCache(HashMap<String, String> words) {

        List<String> lines = FileManager.read(defaultRootFile).orElse(Collections.emptyList());
        for (String line : lines) {

            if (line.length() <= 10 || line.charAt(0) == '%') {
                return;
            }
            String[] parts = line.split("\t");
            if (parts.length < 3 || parts[1].equals(parts[2])) {
                return;
            }

            if (words.containsKey(parts[2])) {
                HashSet<String> set = new HashSet<>();
                set.add(parts[1]);
                treeKeys.put(parts[2], set);
            } else {
                treeValues.put(parts[1], parts[2]);
            }
        }

        List<String> outLines = new ArrayList<>();
        outLines.add(String.format("%d", treeKeys.size()));

        for (String key : treeKeys.keySet()) {
            StringBuilder sb = new StringBuilder(key);
            for (String link : treeKeys.get(key)) {
                sb.append('=');
                sb.append(link);
            }
            outLines.add(sb.toString());
        }

        for (String key : treeValues.keySet()) {
            outLines.add(String.format("%s=%s", key, treeValues.get(key)));
        }

        FileManager.write(this.cachedRootFile, outLines);
    }

    private void loadRootCache() {
        List<String> lines = FileManager.read(cachedRootFile).get();
        int links_len = Integer.parseInt(lines.get(0)) + 1;

        for (int i = 1; i < links_len; i++) {

            HashSet<String> set = new HashSet<>();

            String[] parts = lines.get(i).split("=");
            for (int j = 1; j < parts.length; j++) {
                set.add(parts[j]);
            }

            treeKeys.put(parts[0], set);
        }

        for (int i = links_len; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("=");
            treeValues.put(parts[0], parts[1]);
        }
    }

    private void loadLinkCache() {
        List<String> lines = FileManager.read(cachedLinkFile).orElse(Collections.emptyList());
        this.links = lines.toArray(new String[0]);
    }

    public boolean isCacheReady() {
        return this.cachedRootFile.exists() && this.cachedLinkFile.exists();
    }

    public void create(HashMap<String, String> words) {
        NativeLinker nativeLinker = new NativeLinker();
        if (nativeLinker.isReady()) {
            nativeLinker.create(this.defaultRootFile.getAbsolutePath(), this.cachedRootFile.getAbsolutePath(),
                    this.cachedLinkFile.getAbsolutePath(), words);
        } else {
            if (!this.cachedRootFile.exists()) {
                this.createRootCache(words);
            } else {
                this.loadRootCache();
            }

            if (!this.cachedLinkFile.exists()) {
                this.createLinkTree();
            }
        }
        this.loadLinkCache();

    }

    public void load() {
        this.loadLinkCache();
    }

    public String[] getLinks() {
        return this.links;
    }
}
