package main;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import main.translate.WordsTranslator;
import main.util.FileManager;

public class TranslatorTest {

	public static final String[] INPUT_WORDS = { "bonjour", "je", "cherchais" };
	public static final String[] INPUT_AUTOMATIC_WORDS = { "je", "cherche", "mes", "belles", "chaussures" };

	public static final String[] EXPECTED_WORDS = { "good morning", "i", "cherchais" };
	public static final String[] EXPECTED_AUTOMATIC_WORDS = { "i", "look for", "my", "beautiful", "shoe" };

	private final static WordsTranslator translator = new WordsTranslator();

	private final static File testFile = new File("lib/test.txt");
	public static String[] testFileContent = null;

	@BeforeClass
	public static void setUp() {
		translator.load();

		List<String> lines = FileManager.read(testFile).orElse(new ArrayList<>());
		List<String> words = new ArrayList<>();

		for (String line : lines) {
			for (String word : line.split(" ")) {
				words.add(word);
			}
		}
		testFileContent = words.toArray(new String[0]);
	}

	@Test
	public void testTranslate() {
		assertArrayEquals(EXPECTED_WORDS, translator.translate(INPUT_WORDS));
	}

	@Test
	public void testFastTranslate() {
		assertArrayEquals(EXPECTED_WORDS, translator.fastTranslate(INPUT_WORDS));
	}

	@Test
	public void testTranslateAutomatic() {
		assertArrayEquals(EXPECTED_AUTOMATIC_WORDS, translator.automaticTranslate(INPUT_AUTOMATIC_WORDS));
	}

	@Test
	public void testTranslateWithPrediction() {
		assertArrayEquals(EXPECTED_WORDS,
				translator.translateWithPrediction(INPUT_WORDS));
	}

	@Test
	public void testCompareTranslateSpeed() {

		long startTime = System.nanoTime();
		translator.translate(INPUT_WORDS);
		long translateTime = System.nanoTime() - startTime;

		startTime = System.nanoTime();
		translator.fastTranslate(INPUT_WORDS);
		long fastTranslateTime = System.nanoTime() - startTime;

		assertTrue(translateTime >= fastTranslateTime);
	}

}
