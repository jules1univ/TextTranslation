package main.translate;

import java.util.Optional;
import main.util.StringAlgorithm;

public class WordsTranslator {

    private final Loader loader;
    private final int MIN_WORD_DISTANCE = 3;
    // private final Pattern filterWord = Pattern.compile("[a-z]");

    public WordsTranslator() {
        this.loader = new Loader();
    }

    public void load() {
        this.loader.load();
    }

    public void close() {
        this.loader.save();
    }

    public String[] translate(String[] words) {
        String[] translated = new String[words.length];
        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase();
            Optional<Integer> wIndex = StringAlgorithm.binarySearch(this.loader.getFR(), word);
            translated[i] = wIndex.isPresent() ? this.loader.getEN()[wIndex.get()] : word;
        }
        return translated;

    }

    public String[] fastTranslate(String[] words) {
        String[] translated = new String[words.length];
        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase();
            translated[i] = this.loader.getTable().getOrDefault(word, word);
        }
        return translated;
    }

    private Optional<String> findNearestTraductionWord(String word) {
        Optional<String> nearest = Optional.empty();
        int current = Integer.MAX_VALUE;
        for (int i = 0; i < this.loader.getFR().length; i++) {

            int dist = StringAlgorithm.levenshteinDistance(this.loader.getFR()[i], word);
            if (dist < current && dist < MIN_WORD_DISTANCE) {
                nearest = Optional.of(this.loader.getEN()[i]);
                current = dist;
            }
        }

        return nearest;
    }

    public Optional<String> findWordRootTraduction(String word) {

        int left = 0;
        int right = this.loader.getLinks().length - 1;

        String[] parts = null;
        while (left <= right) {
            int mid = left + (right - left) / 2;

            parts = this.loader.getLinks()[mid].split("=");
            int cmp = word.compareTo(parts[0]);

            if (cmp == 0) {
                return Optional.of(this.loader.getTable().get(parts[1]));
            } else if (cmp < 0) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return Optional.empty();
    }

    public String[] automaticTranslate(String[] words) {
        String[] translated = new String[words.length];
        for (int i = 0; i < translated.length; i++) {
            String lword = words[i].toLowerCase();
            translated[i] = this.loader.getTable().getOrDefault(lword,
                    this.findWordRootTraduction(lword).orElse(lword));
        }
        return translated;
    }

    public String[] translateWithPrediction(String[] words) {
        String[] translated = new String[words.length];

        for (int i = 0; i < translated.length; i++) {
            String lword = words[i].toLowerCase();
            translated[i] = this.loader.getTable().getOrDefault(lword,
                    this.findNearestTraductionWord(lword).orElse(lword));
        }

        return translated;
    }

    public String[] fullTranslate(String[] words) {
        String[] translated = new String[words.length];

        for (int i = 0; i < translated.length; i++) {
            String lword = words[i].trim().toLowerCase();
            if (lword.length() == 0) {
                translated[i] = lword;
                continue;
            }

            translated[i] = this.loader
                    .getTable()
                    .getOrDefault(lword,
                            this.loader.cache((word) -> this.findWordRootTraduction(word), lword)
                                    .orElse(
                                            this.loader.cache((word) -> this.findNearestTraductionWord(word), lword)
                                                    .orElse(lword)));
        }

        return translated;
    }

}
