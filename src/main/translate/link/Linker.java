package main.translate.link;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import main.util.FileManager;

public class Linker {

    private final File cachedRootFile = new File("lib/traduction/root.trd");
    private final File cachedLinkFile = new File("lib/traduction/link.trd");
    private final File defaultRootFile = new File("lib/racines.txt");

    private HashMap<String, Set<String>> tree = new HashMap<>();

    private String[] links = null;

    public Linker() {

    }

    private void treeSearch(String key, HashSet<String> out, List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("=");

            if (parts[0].equals(key) && !out.contains(parts[1])) {
                lines.remove(i);
                i--;

                out.add(parts[1]);
                treeSearch(parts[1], out, lines);

            } else if (parts[1].equals(key) && !out.contains(parts[0])) {
                lines.remove(i);
                i--;

                out.add(parts[0]);
                treeSearch(parts[0], out, lines);

            }
        }
    }

    private void createLinkCacheFromRoot(HashMap<String, String> words) {
        List<String> lines = FileManager.read(cachedRootFile).get();

        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("=");

            if (words.containsKey(parts[1]) && !this.tree.containsKey(parts[1])) {
                lines.remove(i);
                i--;

                HashSet<String> out = new HashSet<>();

                treeSearch(parts[0], out, lines);
                treeSearch(parts[1], out, lines);

                tree.put(parts[1], out);
            }
        }

        List<String> cache = new ArrayList<>();
        tree.forEach((key, values) -> {
            values.forEach(value -> {
                if (value != key) {
                    cache.add(String.format("%s=%s", value, key));
                }
            });
        });
        Collections.sort(cache);
        FileManager.write(cachedLinkFile, cache);

        this.links = cache.toArray(new String[0]);
    }

    private void createRootCache() {
        List<String> lines = FileManager.read(defaultRootFile).orElse(Collections.emptyList());

        lines.parallelStream()
                .filter(line -> line.length() > 2)
                .map(line -> {
                    String[] parts = line.toLowerCase().split("\t");
                    if (parts.length < 3 && !parts[1].equals(parts[2])) {
                        return null;
                    }
                    return String.format("%s=%s", parts[1], parts[2]);
                })
                .filter(Objects::nonNull)
                .sorted()
                .collect(FileManager.write(cachedRootFile));
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
            this.loadLinkCache();
        } else {
            if (!this.cachedRootFile.exists()) {
                this.createRootCache();
            }

            if (!this.cachedLinkFile.exists()) {
                this.createLinkCacheFromRoot(words);
            }
        }
    }

    public void load() {
        this.loadLinkCache();
    }

    public String[] getLinks() {
        return this.links;
    }
}
