import edu.stanford.nlp.simple.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Preprocessing {
	public static void main(String[] args) {
		List<Sentence> filteredSentences = filteredSentences(createDocument(readFile(args[0])), getStopWords());

		int count = 0;
		System.out.println("FILTERED\n==========");
		
		for (Sentence s : filteredSentences)
			System.out.println("Sentence " + count++ + ": " + s);


	}

	static Scanner readFile(String fileName) {
		try {
			return new Scanner(new File(fileName));
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	static List<String> getStopWords() {
		Scanner stopWordReader = readFile("stopwords.txt");
		List<String> stopWords = new ArrayList<>();

		while (stopWordReader.hasNextLine())
			stopWords.add(stopWordReader.nextLine());

		return stopWords;
	}

	static Document createDocument(Scanner fileReader) {
		String contents = "";

		while (fileReader.hasNextLine())
			contents += fileReader.nextLine() + '\n';

		return new Document(contents);
	}

	static List<Sentence> filteredSentences(Document d, List<String> stopWords) {
		List<Sentence> filteredSentences = new ArrayList<>();
		
		for (Sentence s : d.sentences()) {
			List<String> nerTags = s.mentions();
			s = new Sentence(s.toString().replace("\n", ""));

			String sentenceWithNER = "";

			int index = 0;

			System.out.println("\nTAGS: " + nerTags);
			System.out.println("SENTENCE: " + s.toString().toLowerCase() + "\n");

			for (String tag : nerTags) {
				if (tag.split(" ").length > 1) {
					System.out.println("TAG: " + tag);
					System.out.println("OLD INDEX: " + index);
					System.out.println("NEW INDEX: " + s.toString().toLowerCase().substring(index).indexOf(tag.toLowerCase()) + index);

					sentenceWithNER += s.toString().substring(index, s.toString().toLowerCase().substring(index).indexOf(tag.toLowerCase()) + index)
									+ String.join("_", tag.split(" "));
					index = sentenceWithNER.length();
				}
			}

			sentenceWithNER += s.toString().substring(index, s.toString().length());

			s = new Sentence(sentenceWithNER);

			System.out.println("NEW SENTENCE: " + s.toString());

			List<String> words = s.lemmas();
			List<String> validWords = new ArrayList<>();

			for (String word : words)
				if (!stopWords.contains(word.toLowerCase()))
					validWords.add(word);

			filteredSentences.add(new Sentence(validWords));
		}

		return filteredSentences;
	}
}