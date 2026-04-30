import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


public class CaesarCipherFrequencyAnalysis {

    // Arabic alphabet used for the Caesar Cipher
    private static final char[] ARABIC_ALPHABET = {
            'ا', 'ب', 'ت', 'ث', 'ج', 'ح', 'خ', 'د', 'ذ', 'ر',
            'ز', 'س', 'ش', 'ص', 'ض', 'ط', 'ظ', 'ع', 'غ', 'ف',
            'ق', 'ك', 'ل', 'م', 'ن', 'ه', 'و', 'ي'
    };

    // Normal Arabic letter frequency percentages
    private static final double[] ARABIC_FREQUENCIES = {
            11.6, 4.8, 3.7, 1.1, 2.8, 2.6, 1.1, 3.5,
            1.0, 4.7, 0.9, 6.5, 3.0, 2.9, 1.5, 1.7,
            0.7, 3.9, 1.0, 3.0, 2.7, 3.6, 5.3, 3.1,
            7.2, 2.5, 6.0, 6.7
    };

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        // Gets user input
        Console console = System.console();

        String apiKey;

        if (console != null) {
            char[] keyChars = console.readPassword("Enter the API Key: ");
            apiKey = new String(keyChars);
        } else {
            System.out.print("Enter the API Key: ");
            apiKey = sc.nextLine();
        }

        System.out.print("Enter the plaintext: ");
        String text = sc.nextLine();

        System.out.print("Enter Caesar Cipher key/shift: ");
        int shift = sc.nextInt();

        // Translates English plaintext to Arabic
        String arabicText = translateText(apiKey, text, "ar");

        System.out.println("\nTranslated Arabic text:");
        System.out.println(arabicText);

        // Encrypts the Arabic text
        String encryptedText = encryptWithShift(arabicText, shift);

        System.out.println("\nEncrypted Arabic Caesar Cipher text:");
        System.out.println(encryptedText);

        // Uses frequency analysis to guess the original Arabic text
        String bestGuess = decryptUsingFrequencyAnalysis(encryptedText);

        System.out.println("\nFrequency Analysis best guess:");
        System.out.println(bestGuess);

        // Translates the guessed Arabic text back to English
        String englishGuess = translateText(apiKey, bestGuess, "en");

        System.out.println("\nGuessed English version:");
        System.out.println(englishGuess);

        sc.close();
    }

    // Sends text to Google Translate API
    public static String translateText(String apiKey, String text, String targetLanguage) throws Exception {

        String urlStr = "https://translation.googleapis.com/language/translate/v2"
                + "?key=" + apiKey;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        String jsonInput = "{ \"q\": \"" + text + "\", \"target\": \"" + targetLanguage + "\" }";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonInput.getBytes("utf-8"));
        }

        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf-8"));

        StringBuilder response = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            response.append(line.trim());
        }

        String jsonResponse = response.toString();

        int start = jsonResponse.indexOf("\"translatedText\": \"");

        if (start == -1) {
            return "Translation failed.";
        }

        start += "\"translatedText\": \"".length();

        int end = jsonResponse.indexOf("\"", start);

        return jsonResponse.substring(start, end);
    }

    // Encrypts Arabic letters using the Caesar shift
    public static String encryptWithShift(String text, int shift) {

        StringBuilder encryptedText = new StringBuilder();

        for (char c : text.toCharArray()) {

            int index = getArabicIndex(c);

            if (index != -1) {
                int newIndex = (index + shift) % ARABIC_ALPHABET.length;
                encryptedText.append(ARABIC_ALPHABET[newIndex]);
            } else {
                encryptedText.append(c);
            }
        }

        return encryptedText.toString();
    }

    // Tries every shift and picks the closest match
    public static String decryptUsingFrequencyAnalysis(String ciphertext) {

        String bestDecryption = "";
        double lowestChiSquare = Double.MAX_VALUE;

        for (int shift = 0; shift < ARABIC_ALPHABET.length; shift++) {

            String decryptedText = decryptWithShift(ciphertext, shift);
            double chiSquare = calculateChiSquare(decryptedText);

            if (chiSquare < lowestChiSquare) {
                lowestChiSquare = chiSquare;
                bestDecryption = decryptedText;
            }
        }

        return bestDecryption;
    }

    // Decrypts Arabic text with one shift
    public static String decryptWithShift(String text, int shift) {

        StringBuilder decryptedText = new StringBuilder();

        for (char c : text.toCharArray()) {

            int index = getArabicIndex(c);

            if (index != -1) {
                int newIndex = (index - shift + ARABIC_ALPHABET.length) % ARABIC_ALPHABET.length;
                decryptedText.append(ARABIC_ALPHABET[newIndex]);
            } else {
                decryptedText.append(c);
            }
        }

        return decryptedText.toString();
    }

    // Finds the letter position in the Arabic alphabet array
    public static int getArabicIndex(char c) {

        for (int i = 0; i < ARABIC_ALPHABET.length; i++) {
            if (ARABIC_ALPHABET[i] == c) {
                return i;
            }
        }

        return -1;
    }

    // Scores how close the text is to normal Arabic
    public static double calculateChiSquare(String text) {

        int[] letterCounts = new int[ARABIC_ALPHABET.length];
        int totalLetters = 0;

        for (char c : text.toCharArray()) {

            int index = getArabicIndex(c);

            if (index != -1) {
                letterCounts[index]++;
                totalLetters++;
            }
        }

        if (totalLetters == 0) {
            return Double.MAX_VALUE;
        }

        double chiSquare = 0.0;

        for (int i = 0; i < ARABIC_ALPHABET.length; i++) {

            double observed = letterCounts[i];
            double expected = totalLetters * ARABIC_FREQUENCIES[i] / 100;

            if (expected > 0) {
                chiSquare += Math.pow(observed - expected, 2) / expected;
            }
        }

        return chiSquare;
    }
}