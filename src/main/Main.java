package main;

import main.translate.WordsTranslator;
import util.ACX;

public class Main {
	private final static WordsTranslator translator = new WordsTranslator();

	public static String[] translate(String[] words) {
		return translator.translate(words);
	}

	public static String[] fastTranslate(String[] words) {
		return translator.fastTranslate(words);
	}

	public static String[] automaticTranslate(String[] words) {
		return translator.automaticTranslate(words);
	}

	public static String[] translateWithPrediction(String[] words) {
		return translator.translateWithPrediction(words);
	}

	public static String[] fullTranslate(String[] words) {
		return translator.fullTranslate(words);
	}

	public static void main(String[] args) {
		translator.load();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			translator.close();
		}));

		ACX.interfaceTraduction("fullTranslate");
	}
}