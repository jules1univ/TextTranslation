package main.translate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import main.translate.link.Linker;
import main.util.FileManager;

public class Loader {

    private final HashMap<String, String> words = new HashMap<>();
    private final HashMap<String, String> cacheWords = new HashMap<>();

    private final Linker linker = new Linker();

    private String[] wordsFR = null;
    private String[] wordsEN = null;

    private final File defaultTraductionFile = new File("lib/default.txt");
    private final File traductionFolder = new File("lib/traduction");
    private final File cachedTraductionFile = new File("lib/traduction/core.trd");
    private final File cachedWordFile = new File("lib/traduction/words.trd");

    public Loader() {

    }

    private void loadDefaultTraduction() {
        List<String> lines = FileManager.read(this.defaultTraductionFile).get();
        lines.removeIf(line -> line.startsWith("%"));

        final List<String> cacheLines = new ArrayList<>();

        this.wordsFR = new String[lines.size() / 2];
        this.wordsEN = new String[lines.size() / 2];

        for (int i = 0; i < lines.size(); i += 2) {
            int index = i / 2;
            this.wordsFR[index] = lines.get(i).toLowerCase();
            this.wordsEN[index] = lines.get(i + 1).toLowerCase();

            this.words.put(this.wordsFR[index], this.wordsEN[index]);
            cacheLines.add(String.format("%s=%s", this.wordsFR[index], this.wordsEN[index]));
        }

        Arrays.sort(cacheLines.toArray(new String[0]));

        new Thread(() -> {
            FileManager.write(this.cachedTraductionFile, cacheLines);
        }).start();
    }

    private void loadCacheTraduction() {
        List<String> lines = FileManager.read(this.cachedTraductionFile).get();

        this.wordsFR = new String[lines.size()];
        this.wordsEN = new String[lines.size()];

        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("=");

            this.wordsFR[i] = parts[0];
            this.wordsEN[i] = parts[1];
            this.words.put(this.wordsFR[i], this.wordsEN[i]);
        }
    }

    private void loadCacheWords() {
        List<String> lines = FileManager.read(this.cachedWordFile).get();
        for (String line : lines) {
            String[] parts = line.split("=");
            this.cacheWords.put(parts[0], parts[1]);
        }
    }

    public void load() {
        if (!this.traductionFolder.exists()) {
            this.traductionFolder.mkdir();
        }

        if (!this.cachedTraductionFile.exists()) {
            this.loadDefaultTraduction();
        } else {
            this.loadCacheTraduction();
        }

        if (!this.linker.isCacheReady()) {
            this.linker.create(this.words);
        } else {
            this.linker.load();
        }

        if (this.cachedWordFile.exists()) {
            this.loadCacheWords();
        }
    }

    public String[] getEN() {
        return this.wordsEN;
    }

    public String[] getFR() {
        return this.wordsFR;
    }

    public String[] getLinks() {
        return this.linker.getLinks();
    }

    public HashMap<String, String> getTable() {
        return this.words;
    }

    @FunctionalInterface
    public interface CallbackCache {
        Optional<String> run(String word);
    }

    public Optional<String> cache(CallbackCache func, String word) {
        if (this.cacheWords.containsKey(word)) {
            return Optional.of(this.cacheWords.get(word));
        }

        Optional<String> value = func.run(word);
        if (value.isPresent()) {
            this.cacheWords.put(word, value.get());
        }
        return value;
    }

    public void save() {
        new Thread(() -> {
            List<String> lines = new ArrayList<>();
            for (String key : this.cacheWords.keySet()) {
                lines.add(String.format("%s=%s", key, this.cacheWords.get(key)));
            }
            FileManager.write(cachedWordFile, lines);
        }).start();
    }
}
