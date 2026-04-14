import java.util.*;

public class GermanCipherFrequencyAnalysis {

    // English letter frequencies
    private static final double[] GERMAN_FREQUENCIES = {
            6.51, 1.89, 3.06, 5.08, 17.40, 1.66, 3.01, 4.76,
            7.55, 0.27, 1.21, 3.44, 2.53, 9.78, 2.51, 0.79,
            0.02, 7.00, 7.27, 6.15, 4.35, 0.67, 1.89, 0.03,
            0.04, 1.13
    };

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // PROMPT USER INPUT
        System.out.print("Enter ciphertext: ");
        String ciphertext = scanner.nextLine();

        System.out.print("Enter shift key (1-26): ");
        int shift = scanner.nextInt();

        // DECRYPT USING USER KEY
        String decrypted = decryptWithShift(ciphertext, shift);

        System.out.println("Decrypted text: " + decrypted);
        System.out.println("Translated to English: " + translateGermanToEnglish(decrypted));
    }

    // Method to decrypt using frequency analysis (UNCHANGED)
    public static String decryptUsingFrequencyAnalysis(String ciphertext) {
        String bestDecryption = "";
        double lowestChiSquare = Double.MAX_VALUE;

        for (int shift = 5; shift < 26; shift++) {
            String decryptedText = decryptWithShift(ciphertext, shift);
            double chiSquare = calculateChiSquare(decryptedText);

            if (chiSquare < lowestChiSquare) {
                lowestChiSquare = chiSquare;
                bestDecryption = decryptedText;
            }
        }

        return bestDecryption;
    }

    // Method to decrypt text with a specific shift
    public static String decryptWithShift(String text, int shift) {
        StringBuilder decryptedText = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                decryptedText.append((char) ((c - base - shift + 26) % 26 + base));
            } else {
                decryptedText.append(c);
            }
        }

        return decryptedText.toString();
    }

    // Method to calculate Chi-Square statistic
    public static double calculateChiSquare(String text) {
        int[] letterCounts = new int[26];
        int totalLetters = 0;

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                char lowerCaseChar = Character.toLowerCase(c);
                letterCounts[lowerCaseChar - 'a']++;
                totalLetters++;
            }
        }

        double chiSquare = 0.0;

        for (int i = 0; i < 26; i++) {
            double observed = letterCounts[i];
            double expected = totalLetters * GERMAN_FREQUENCIES[i] / 100;
            chiSquare += Math.pow(observed - expected, 2) / expected;
        }

        return chiSquare;
    }

    // TRANSLATOR (UNCHANGED)
    public static String translateGermanToEnglish(String germanText) {
        Map<String, String> dictionary = new HashMap<>();

        dictionary.put("und", "and");
        dictionary.put("ist", "is");
        dictionary.put("sind", "are");
        dictionary.put("sehr", "very");
        dictionary.put("gut", "good");
        dictionary.put("hallo", "hello");
        dictionary.put("welt", "world");
        dictionary.put("ich", "I");
        dictionary.put("bin", "am");
        dictionary.put("du", "you");
        dictionary.put("das", "that");
        dictionary.put("ein", "a");
        dictionary.put("eine", "a");

        String[] words = germanText.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            String clean = word.replaceAll("[^a-zA-Zäöüß]", "");
            result.append(dictionary.getOrDefault(clean, "[" + clean + "]")).append(" ");
        }

        return result.toString().trim();
    }
}
