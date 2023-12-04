package main.util;

import java.util.Optional;

public class StringAlgorithm {

    public static Optional<Integer> binarySearch(String[] arr, String search) {
        int left = 0;
        int right = arr.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            int cmp = search.compareTo(arr[mid]);

            if (cmp == 0) {
                return Optional.of(mid);
            } else if (cmp < 0) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return Optional.empty();
    }

    public static int levenshteinDistance(String word1, String word2) {
        int m = word1.length();
        int n = word2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int cost = (word1.charAt(i - 1) == word2.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
                }
            }
        }

        return dp[m][n];
    }

    public static Pair<String, Integer> getWordSuffixIndex(String target, String base) {

        int index = 0;
        int suffix = -1;

        StringBuilder stringBuilder = new StringBuilder();
        while (index < target.length()) {
            if (index >= base.length() || target.charAt(index) != base.charAt(index)) {
                stringBuilder.append(target.charAt(index));

                if (suffix == -1) {
                    suffix = index >= base.length() ? base.length() : index;
                }
            }
            index++;
        }
        if (suffix == -1) {
            suffix = base.length();
        }
        return new Pair<String, Integer>(stringBuilder.toString(), suffix);
    }
}
