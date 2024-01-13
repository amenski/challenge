package it.aman;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.function.Consumer;

public class App {

    private boolean lines;
    private boolean words;
    private boolean bytes;
    private final boolean chars;
    private final boolean maxLineLength;

    List<String> fileNames;

    /**
     * --files0-from=F
     * read input from the files specified by NUL-terminated names in file F; If F is - then read names from standard input
     * src/main/resources/test.txt
     */


    private String currentFile = "";
    List<Result> results = new ArrayList<>();

    public static void main(String[] args) {
        ParseArgs parseArgs = new ParseArgs(args);
        new App(parseArgs.parse()).run();
    }


    public App(Map<String, Object> options) {
        if(options.get("help") != null) {
            printHelp();
            System.exit(0);
        }

        this.lines = options.get("lines") != null;
        this.words = options.get("words") != null;
        this.bytes = options.get("bytes") != null;
        this.chars = options.get("chars") != null;
        this.maxLineLength = options.get("max-line-length") != null;
        this.fileNames = options.get("files") != null ? (List<String>) options.get("files") : new ArrayList<>() ;

    }

    public void run() {
        applyDefaultOptions();
        try {
            if (fileNames != null && !fileNames.isEmpty()) {
                readFile();
            } else {
                readStdIn(); // TODO process this
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyDefaultOptions() {
        // default options
        if(Boolean.FALSE.equals(lines)
                && Boolean.FALSE.equals(words)
                && Boolean.FALSE.equals(bytes)
                && Boolean.FALSE.equals(chars)
                && Boolean.FALSE.equals(maxLineLength) ) {
            lines = true; words = true; bytes = true;
        }
    }

    private void readStdIn() {
        try (Scanner scanner = new Scanner(System.in);
             BufferedReader reader = new BufferedReader(new StringReader(scanner.nextLine()))) {
            count(reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        printResult(System.out);
    }

    private void readFile() throws IOException {
        for (String file : fileNames) {
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile = file))) {
                count(reader);
            } catch (FileNotFoundException e) {
                System.err.printf("File [ %s ] not found.%n", file);
                throw new RuntimeException(e);
            }
        }
        printResult(System.out);
    }

    private void count(BufferedReader reader) {
        try {
            long lineCount = 0, wordCount = 0, byteCount = 0, maxLineLength = 0, currentLineLength = 0, chars = 0;
            String current;
            while ((current = reader.readLine()) != null) {
                lineCount++;
                wordCount += (currentLineLength = countWords(current));
                byteCount += current.getBytes().length;
                chars += current.length();

                if(currentLineLength > maxLineLength) maxLineLength = currentLineLength;
            }
            results.add(new Result(lineCount, wordCount, byteCount, maxLineLength, chars, currentFile));
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void printResult(OutputStream os) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (Result r : results) {
                if (lines) stringBuilder.append(r.lineCount).append(" ");
                if (words) stringBuilder.append(r.wordCount).append(" ");
                if (bytes) stringBuilder.append(r.byteCount).append(" ");
                if (chars) stringBuilder.append(r.chars).append(" ");
                if (isNotBlank(currentFile)) stringBuilder.append(currentFile);

                os.write(stringBuilder.toString().getBytes());
                os.write('\n');
                stringBuilder.setLength(0);
            }
            if (maxLineLength) os.write(("\nMax line length: " + getMaxLineLength()).getBytes());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private long countWords(String input) {
        return new StringTokenizer(input).countTokens();
    }

    public static boolean isBlank(final CharSequence cs) {
        final int strLen = length(cs);
        if (strLen == 0) return true;
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    private static boolean isNotBlank(String cs) {
        return !isBlank(cs);
    }

    private long getMaxLineLength() {
       if(results.isEmpty()) return 0;
       long max = 0;
       for (Result r : results) {
           if (r.maxLineLength > max) max = r.maxLineLength;
       }
       return max;
    }

    private static void printHelp() {
        System.out.println("Usage: java ccwc [-chlLmw] [<fileNames>...]");
        System.out.println("Options:");
        System.out.println("  -c, --bytes             Print bytes count");
        System.out.println("  -l, --lines             Print newline count");
        System.out.println("  -m, --chars             Print char count");
        System.out.println("  -w, --words             Print word count");
        System.out.println("  -L, --max-line-length   Max line length");
        System.out.println("  -h, --help   Display this help and exit");
    }


    static class Result {
        final long lineCount;
        final long wordCount;
        final long byteCount;
        final long maxLineLength;
        final long chars;
        final String file;

        public Result(long lineCount, long wordCount, long byteCount, long maxLineLength, long chars, String file) {
            this.lineCount = lineCount;
            this.wordCount = wordCount;
            this.byteCount = byteCount;
            this.maxLineLength = maxLineLength;
            this.chars = chars;
            this.file = file;
        }
    }

    static class ParseArgs {

        private LinkedList<String> args;
        private final Map<String, Object> commandsAndFile = new HashMap<>();

        private static final List<String> SHORT_OPTIONS = Arrays.asList("l", "w", "c", "m", "L", "h");
        private static final List<String> LONG_OPTIONS = Arrays.asList( "lines", "words", "bytes", "chars", "max-line-length", "help");

        ParseArgs(String[] args) {
            if(args != null && args.length > 0) {
                this.args = new LinkedList<>(Arrays.asList(args));
            }
        }

        public Map<String, Object> parse() {
            if (this.args == null) return new HashMap<>();

            for (String s : args) {
                if (s.startsWith("--")){
                    handleLong(s.substring(2));
                    continue;
                }
                if (s.startsWith("-")){
                    handleShort(s.substring(1));
                    continue;
                }
                
                //file names
                handleFiles(s);
            }
            return commandsAndFile;
        }

        @SuppressWarnings("unchecked")
        private void handleFiles(String fileName) {
            if (isBlank(fileName)) return;
            Object files = commandsAndFile.get("files");
            if(files == null) {
                List<String> f = new ArrayList<>();
                f.add(fileName);
                commandsAndFile.put("files", new ArrayList<>(f));
                return;
            }
            ((List<String>) files).add(fileName);
        }

        private void handleLong(String s) {
            if (LONG_OPTIONS.contains(s)) {
                setIfNotNull(handleOption(s), t -> commandsAndFile.put(t.longForm, t));
            } else {
                commandsAndFile.put("help", new CommandOption("h", "help", "Print help."));
            }
        }

        private void handleShort(String s) {
            for (int i=0; i<s.length();i++) {
                if (SHORT_OPTIONS.contains(String.valueOf(s.charAt(i)))) {
                    setIfNotNull(handleOption(String.valueOf(s.charAt(i))), t -> commandsAndFile.put(t.longForm, t));
                } else {
                    commandsAndFile.put("help", new CommandOption("h", "help", "Print help."));
                }
            }
        }

        private <T> void setIfNotNull(T value, Consumer<T> consumer) {
            if(value == null) return;
            consumer.accept(value);
        }

        private static CommandOption handleOption(String option) {
            switch (option) {
                case "lines":
                case "l":
                    return new CommandOption("l", "lines", "Count lines");
                case "words":
                case "w":
                    return new CommandOption("w", "words", "Count words");
                case "bytes":
                case "c":
                    return new CommandOption("c", "bytes", "Count bytes");
                case "chars":
                case "m":
                    return new CommandOption("m", "chars", "Count chars");
                case "max-line-length":
                case "L":
                    return new CommandOption("L", "max-line-length", "Max line length");
                default:
                    return new CommandOption("h", "help", "Print help.");
            }
        }
    }


    static class CommandOption {
        final String shortForm;
        final String longForm;
        final String description;

        public CommandOption(String shortForm, String longForm, String description) {
            this.shortForm = shortForm;
            this.longForm = longForm;
            this.description = description;
        }
    }
}
