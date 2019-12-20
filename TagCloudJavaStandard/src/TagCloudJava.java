import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

/**
 * TagCloudGenerator takes a user-input file and word count, and generates a
 * .html file containing n words with frequency counts related to the font
 * sizes.
 *
 * @author Chase Fensore, Kayla Manouchehri
 *
 */
public final class TagCloudJava {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TagCloudJava() {
    }

    /**
     * Compare {@code Entry}s values (@code Integer) in decreasing order,
     * updated to be consistent with equals.
     */
    private static class MapValueLT
            implements Comparator<Entry<String, Integer>> {

        @Override
        public int compare(Entry<String, Integer> o1,
                Entry<String, Integer> o2) {
            if (o1.getValue() < o2.getValue()) {
                return 1;
            } else if (o1.getValue() > o2.getValue()) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Compare {@code Entry}s Keys (@code String) in lexiographical order,
     * updated to be consistent with equals.
     */
    private static class MapKeyLT
            implements Comparator<Entry<String, Integer>> {

        @Override
        public int compare(Entry<String, Integer> o1,
                Entry<String, Integer> o2) {
            if (o1.getKey().compareTo(o2.getKey().toLowerCase()) > 0) {
                return 1;
            } else if (o1.getKey().compareTo(o2.getKey().toLowerCase()) < 0) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Generates the .HTML code in a file for an ordered list of terms and their
     * definitions with varying font sizes corresponding to their frequency.
     *
     * @param alphaSort
     *            the {@code ArrayList} of term and count pairs
     * @param min
     *            the {@code int} for the smallest count of a unique term in
     *            alphaSort.
     * @param max
     *            the {@code int} for the largest count of a unique term in
     *            alphaSort.
     *
     * @param outFile
     *            the {@code PrinterWriter} to write to html file
     * @param input
     *            the {@code String} containing the name of index file path
     *
     * @requires {@code outFile} stream is open
     *
     * @ensures outFile contains appropriate HTML formatting, and hyperlinks to
     *          the counts of all terms in {@code termList} and {@code wordMap}
     *
     **/
    private static void generatePage(
            ArrayList<Entry<String, Integer>> alphaSort, int max, int min,
            PrintWriter outFile, String input) {

        //HTML Output begins
        outFile.println("<html>");
        outFile.println("<head>");
        outFile.println("<title>Top " + alphaSort.size() + " words in " + input
                + "</title>");
        outFile.println(
                "<link href=\"http://web.cse.ohio-state.edu/software/2231/web-sw2/assignments/projects/tag-cloud-generator/data/tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        outFile.println("</head>");
        outFile.println("<body>");
        outFile.println(
                "<h2>Top " + alphaSort.size() + " words in " + input + "</h2>");
        outFile.println("<hr>");
        outFile.println("<div class=\"cdiv\">");
        outFile.println("<p class=\"cbox\">"); //Start of table table

        // alphaSort is already alphabetically sorted.
        while (alphaSort.size() > 0) {
            Entry<String, Integer> result = alphaSort.remove(0); //remove first entry
            String key = result.getKey();
            int value = result.getValue();
            //pass into fontSize
            int font = fontSize(min, max, value); //value= count arg
            //do what it says in inspect
            outFile.println("<span style=\"cursor:default\" class=\"f" + font
                    + "\" title=\"count: " + value + "\">" + key + "</span>");
        }
        //Page closing tags
        outFile.println("</p>");
        outFile.println("</div>");
        outFile.println("</body>");
        outFile.println("</html>");
    }

    /**
     *
     *
     * @param termList
     *            the {@code Queue} of unique words to be written into HTML file
     * @param wordMap
     *            the {@code TreeMap} of unique words and how many times they
     *            appear in the text file
     *
     * @param inFile
     *            the {@code inRead} to read from a given (.txt) file
     *
     * @param separatorSet
     *            the {@code Set} of separators to be excluded from termList and
     *            wordMap
     *
     * @requires inFile stream is open
     *
     * @updates wordMap
     *
     * @updates termList
     *
     *
     * @ensures For a given text file (inFile), termList contains each unique
     *          word contained in inFile's text contents. Unique words and their
     *          number of occurrences are recorded in wordMap. inFile stream
     *          remains open.
     *
     **/
    private static void processLines(Queue<String> termList,
            TreeMap<String, Integer> wordMap, BufferedReader inRead,
            Set<Character> separatorSet) {

        String token = ""; //token is entire line.
        String term = ""; //term is 1 word or separator.
        try {
            token = inRead.readLine();
            while (token != null) {
                int pos = 0;
                while (pos < token.length()) {//Line index: reset each time
                    term = nextWordOrSeparator(token, pos, separatorSet)
                            .toLowerCase();
                    // Update current position in line. Move past old term.
                    pos = pos + term.length();
                    if (wordMap.containsKey(term)) {
                        // Term already present: add 1 to Value of existing Key
                        int val = wordMap.remove(term);
                        wordMap.put(term, val + 1);//adds 1 to Value
                    } else if (!separatorSet.contains(term.charAt(0))) {
                        // Term not present: ensure term is not separator
                        // termList.enqueue(term);
                        wordMap.put(term, 1); //new to the Map!
                    }

                }
                token = inRead.readLine();
            }
        } catch (IOException e1) {
            token = null;
            System.err.println("Error reading from file.");
        }
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        // Check "text" at position
        boolean isSep = false;
        // Check 0th char

        isSep = separators.contains(text.charAt(position));

        // Now we know if text starts with: word, or sep
        int strLen = position;
        if (isSep) { //sep cont forward

            while (strLen < text.length()
                    && separators.contains(text.charAt(strLen))) {
                strLen++; //index

            }
        } else { // Word continue forward
            while (strLen < text.length()
                    && !separators.contains(text.charAt(strLen))) {
                strLen++; //index

            }
        }
        String result = text.substring(position, strLen);
        //-1: will iterate through first "cont=false"

        return result;

    }

    /**
     *
     * @param min
     *            the minimum value in {@SortingMachine}
     * @param max
     *            the {@code int} for the largest count of a unique term in
     *            alphaSort.
     * @param count
     *            the number occurrences for {@Map.Pair}
     *
     *
     *
     *
     * @return an int that is the font size needed
     *
     *
     *
     * @ensures For a given word for the tag Cloud, the proportional size font
     *          is found
     *
     **/
    private static int fontSize(int min, int max, int count) {
        int fontSize = (47 - 11) * (count - min);
        //fix for dividing by zero
        if (max != min) {
            fontSize = fontSize / (max - min);
        }
        fontSize += 11;
        return fontSize;
    }

    /**
     * Generates the set of characters in the given {@code String} into the
     * given {@code Set}.
     *
     * @param str
     *            the given {@code String}
     * @param strSet
     *            the {@code Set} to be replaced
     * @replaces strSet
     * @ensures strSet = entries(str)
     */
    private static void generateElements(String str, Set<Character> strSet) {
        assert str != null : "Violation of: str is not null";
        assert strSet != null : "Violation of: strSet is not null";

        strSet.clear(); //ensure empty
        for (int i = 0; i < str.length(); i++) {
            char sub = str.charAt(i);

            if (!strSet.contains(sub)) { //only adds unique characters
                strSet.add(sub);
            }

        }

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        BufferedReader keyBoard = new BufferedReader(
                new InputStreamReader(System.in)); //keyboard stream
        //INPUT FILE
        String inputFile;
        System.out.println("Please enter the name of input file: ");
        try {
            inputFile = keyBoard.readLine();

        } catch (IOException e) {
            System.err.print("Error reading from keyboard input file");
            return;
        }
        //CHECK INPUT FILE
        BufferedReader inRead = null;
        try {
            inRead = new BufferedReader(new FileReader(inputFile)); //FileReader
        } catch (FileNotFoundException e) { //if not found
            System.err.print("Error trying to find input file");
            return;
        }
        //OUTPUT FILE
        System.out.println("Please enter name of output file: ");
        String destination = "";
        try {
            destination = keyBoard.readLine();

        } catch (IOException e) {
            System.err.print("Error reading from keyboard output file");
        }
        //CHECK FILE TO WRITE TO
        PrintWriter outFile = null;
        boolean checkError = false;
        try {
            outFile = new PrintWriter(
                    new BufferedWriter(new FileWriter(destination))); //FileReader
        } catch (IOException e) {
            checkError = true;
            System.err.print("Error opening writing file");
        }
        //IF NO ERRORS CONTINUE ON
        if (!checkError) {
            //ASK FOR NUMBER OF WORDS
            System.out.println("Enter desired number of words: ");
            int numWords = 0;
            try {
                numWords = Integer.parseInt(keyBoard.readLine());
            } catch (IOException e) {
                System.out
                        .println("Error reading from keyboard number of words");
            }
            // ERROR: Prevent negative counts, and Integer Underflow
            if (numWords < 0) {
                System.err
                        .println("You must enter a positive number of words.");
            }
            // ERROR: Prevent Integer Overflow
            if (numWords > Integer.MAX_VALUE) {
                System.err.println("You must enter a word count lesser than "
                        + Integer.MAX_VALUE);
            }
            //SEPARATOR SET
            final String separatorStr = " \t\n\r,.-;*`/\"@#$%&()[]";
            Set<Character> separatorSet = new HashSet<>();
            generateElements(separatorStr, separatorSet);
            //TREEMAP<K=word, V=#of times>
            TreeMap<String, Integer> wordMap = new TreeMap<>();
            //Create Queue for (unique) terms(non-separators) to be placed into
            Queue<String> termList = new PriorityQueue<>();
            // Unique words are placed into termList and wordMap
            // Additional Activity #2 is attempted here, with all words
            // turned to lower-case.
            processLines(termList, wordMap, inRead, separatorSet);
            //SORTING
            //SORT FROM LARGEST COUNTS TO SMALLEST COUNTS
            Comparator<Entry<String, Integer>> ci1 = new MapValueLT();
            List<Map.Entry<String, Integer>> countSort = new LinkedList<Map.Entry<String, Integer>>(
                    wordMap.entrySet());
            Collections.sort(countSort, ci1);
            //min and max for fontSize
            int min = 0;
            int max = 0;
            int i = 0;
            // NOTE: We chose to print all words in file if numWords> size of countSort
            ArrayList<Entry<String, Integer>> alphaSort = new ArrayList<>();
            while (i < numWords && countSort.size() > 0) { // Whichever comes first.
                Entry<String, Integer> add = countSort.remove(0); //remove first entry
                if (i == 0) { //1st iteration
                    min = add.getValue();
                    max = add.getValue();
                } else if (add.getValue() > max) {
                    max = add.getValue();
                } else if (add.getValue() < min) {
                    min = add.getValue();
                }
                alphaSort.add(add); //add into alphaSort
                i++;
            }
            //SORT ALPHABETICAL ORDER
            Comparator<Entry<String, Integer>> ci2 = new MapKeyLT();
            Collections.sort(alphaSort, ci2);
            //Format(HTML) and write wordMap contents to destination
            generatePage(alphaSort, max, min, outFile, inputFile);
        }
        //CLOSE FILES
        try {
            inRead.close();
            outFile.close();
        } catch (IOException e) {
            System.err.println("Error closing file");
        }
    }

}
