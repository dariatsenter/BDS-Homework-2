import edu.stanford.nlp.simple.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

class Term {
	String word;
	int document, count;
	double frequency;

	Term(String word, int document, int count) {
		this.word = word;
		this.document = document;
		this.count = count;
	}

	@Override
	public String toString() {
		return "[Document: " + document + ", Word: " + word + ", Count: " + count + ", Frequency: " + frequency + "]";
	}
}

public class Preprocessing {
	public static void main(String[] args) {
		List<List<Sentence>> documents = filteredDocuments(1);
		// HashMap<Integer, String> legend = constructLegend(documents);
		// int[][] documentMatrix = constructMatrix(documents, legend);
		// List<HashMap<String, int[]>> documentMatrix = constructMatrix(documents, legend);
		Term[][] documentMatrix = constructMatrix(documents);

		for (int i = 0; i < documentMatrix.length; i++)
			for (int j = 0; j < documentMatrix[i].length; j++)
				System.out.println(documentMatrix[i][j]);

		// int documentCount = 0;

		// for (HashMap<String, int[]> document : documentMatrix) {
		// 	System.out.println("\n\nDocument " + documentCount++ + ": \n==========\n");

		// 	for (String keyword : document.keySet())
		// 		if (document.get(keyword)[0] > 0)
		// 			System.out.println(keyword + ": " + document.get(keyword)[0]);
		// }


		// for (int i = 0; i < documentMatrix.length; i++) {
		// 	System.out.println("\n\nDocument " + i + "\n===========\n");

		// 	for (int j = 0; j < documentMatrix[i].length; j++)
		// 		System.out.println(legend.get(j) + ": " + documentMatrix[i][j]);
		// }
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
			s = new Sentence(s.toString().trim().replace("\n", "").replaceAll(" +", " "));

			String sentenceWithNER = "";

			int index = 0;

			// System.out.println("\nTAGS: " + nerTags);
			// System.out.println("SENTENCE: " + s.toString().toLowerCase() + "\n");

			for (String tag : nerTags) {
				int newIndex = s.toString().toLowerCase().substring(index).indexOf(tag.toLowerCase()) + index;

				// System.out.println("TAG: " + tag);
				// System.out.println("OLD INDEX: " + index);
				// System.out.println("NEW INDEX: " + newIndex);

				if (tag.split(" ").length > 1 && newIndex > index) {

					sentenceWithNER += s.toString().substring(index, newIndex) + String.join("_", tag.split(" "));
					index = sentenceWithNER.length();
				}
			}

			sentenceWithNER += s.toString().substring(index, s.toString().length());

			s = new Sentence(sentenceWithNER);

			// System.out.println("NEW SENTENCE: " + s.toString());

			List<String> words = s.lemmas();
			List<String> validWords = new ArrayList<>();

			for (String word : words)
				if (!stopWords.contains(word.toLowerCase()))
					validWords.add(word);

			filteredSentences.add(new Sentence(validWords));
		}

		return filteredSentences;
	}

	static List<List<Sentence>> filteredDocuments(int folder) {
		List<List<Sentence>> filteredDocuments = new ArrayList<>();
		List<String> stopWords = getStopWords();

		for (int i = 1; i < 9; i++)
			filteredDocuments.add(filteredSentences(createDocument(readFile("data/C" + folder + "/article0" + i + ".txt")), stopWords));
		// for (int i = 1; i < 8; i += 3)
		// 	for (int j = 1; j < 9; j++)
				// filteredDocuments.add(filteredSentences(createDocument(readFile("data/C" + i + "/article0" + j + ".txt")), stopWords));

		// int documentCount = 0;

		// for (List<Sentence> filteredSentences : filteredDocuments) {
		// 	int sentenceCount = 0;
		// 	System.out.println("\nDocument " + documentCount++ + "\n========");
			
		// 	for (Sentence s : filteredSentences)
		// 		System.out.println("Sentence " + sentenceCount++ + ": " + s);
		// }

		return filteredDocuments;
	}

	static Term[][] constructMatrix(List<List<Sentence>> documents) {
		List<String> uniqueWords = new ArrayList<>();

		for (List<Sentence> sentences : documents)
			for (Sentence s : sentences)
				for (String word : s.words())
					if (!uniqueWords.contains(word))
						uniqueWords.add(word);

		Term[][] documentMatrix = new Term[documents.size()][uniqueWords.size()];

		for (int i = 0; i < documentMatrix.length; i++) {
			for (int j = 0; j < documentMatrix[i].length; j++) {
				String keyword = uniqueWords.get(j);
				int count = 0;

				for (Sentence s : documents.get(i))
					count += Collections.frequency(s.words(), keyword);

				Term t = new Term(uniqueWords.get(j), i, count);

				documentMatrix[i][j] = t;
			}
		}

		for (int i = 0; i < documentMatrix.length; i++) {
			List<String> uniqueWordsForDocument = new ArrayList<>();
			
			for (Sentence s : documents.get(i))
				for (String word : s.words())
					if (!uniqueWordsForDocument.contains(word))
						uniqueWordsForDocument.add(word);

			int totalTermsInDocument = uniqueWordsForDocument.size();

			for (int j = 0; j < documentMatrix[i].length; j++)
				documentMatrix[i][j].frequency = (double) documentMatrix[i][j].count / (double) totalTermsInDocument;
		}

		return documentMatrix;
	}

	// static HashMap<Integer, String> constructLegend(List<List<Sentence>> documents) {
	// 	HashMap<Integer, String> allWords = new HashMap<>();
	// 	List<String> uniqueWords = new ArrayList<>();

	// 	int index = 0;

	// 	for (List<Sentence> sentences : documents) {
	// 		for (Sentence s : sentences) {
	// 			List<String> words = s.words();

	// 			for (String word : words) {
	// 				if (!uniqueWords.contains(word)) {
	// 					allWords.put(index++, word);
	// 					uniqueWords.add(word);
	// 				}
	// 			}
	// 		}
	// 	}

	// 	return allWords;
	// }

	// static int[][] constructMatrix(List<List<Sentence>> documents, HashMap<Integer, String> legend) {
	// 	int[][] documentMatrix = new int[documents.size()][legend.size()];

	// 	for (int i = 0; i < documentMatrix.length; i++) {
	// 		for (int j = 0; j < documentMatrix[i].length; j++) {
	// 			String keyWord = legend.get(j);

	// 			int frequency = 0;

	// 			for (Sentence s : documents.get(i))
	// 				frequency += Collections.frequency(s.words(), keyWord);

	// 			documentMatrix[i][j] = frequency;
	// 		}
	// 	}

	// 	return documentMatrix;
	// }

	// static List<HashMap<String, int[]>> constructMatrix(List<List<Sentence>> documents, HashMap<Integer, String> legend) {
	// 	List<HashMap<String, int[]>> documentMatrix = new ArrayList<>();

	// 	for (int i = 0; i < documents.size(); i++) {
	// 		documentMatrix.add(i, new HashMap<String, int[]>());

	// 		for (int j = 0; j < legend.size(); j++) {
	// 			String keyword = legend.get(j);
	// 			int frequency = 0;

	// 			for (Sentence s : documents.get(i))
	// 				frequency += Collections.frequency(s.words(), keyword);

	// 			int tfidf = (documents.size() / )

	// 			documentMatrix.get(i).put(keyword, new int[] { frequency, -1 } );
	// 		}
	// 	}

	// 	return documentMatrix;
	// }
}