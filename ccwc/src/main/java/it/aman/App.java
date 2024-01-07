package it.aman;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.util.*;

@Command(name = "ccwc", description = "Linux wc coding challenge")
public class App implements Runnable {

    @Option(names = {"-l", "--lines"}, description = "Print newline count")
    private boolean lines;

    @Option(names = {"-w", "--words"}, description = "Print word count")
    private boolean words;

    @Option(names = {"-c", "--bytes"}, description = "Print bytes count")
    private boolean bytes;

    @Option(names = {"-m", "--chars"}, description = "Print char count")
    private boolean chars;

    @Option(names = {"-L", "--max-line-length"}, description = "Max line length")
    private boolean maxLineLength;

    @Option(names = { "-h", "--help", "-?", "-help"}, usageHelp = true, description = "Display this help and exit")
    private boolean help;

    /**
     * --files0-from=F
     * read input from the files specified by NUL-terminated names in file F; If F is - then read names from standard input
     * src/main/resources/test.txt
     */

    @Parameters
    List<String> fileNames;

    private String currentFile = "";
    List<Result> results = new ArrayList<>();

    public static void main(String[] args) {
        ParseArgs parseArgs = new ParseArgs(args);
        List<CommandOption> optionList = parseArgs.parse();
        new CommandLine(new App()).execute(args);
    }

    @Override
    public void run() {
        applyDefaultOptions();
        try {
            if (fileNames != null && !fileNames.isEmpty()) {
                readFile();
            } else {
                readStdIn();
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
            }
            if (maxLineLength) stringBuilder.append("\nMax line length: ").append(getMaxLineLength());

            os.write(stringBuilder.toString().getBytes());
            os.write('\n');
            stringBuilder.setLength(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private long countWords(String input) {
        return new StringTokenizer(input).countTokens();
    }

    private boolean isNotBlank(String input) {
        int length = input.length();
        while(length > 0) {
            if(Character.isLetterOrDigit(input.charAt(--length))) return true;
        }
        return false;
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
        List<CommandOption> enabled = new ArrayList<>();

        private static final List<String> SHORT_OPTIONS = Arrays.asList("l", "w", "c", "m", "L", "h");
        private static final List<String> LONG_OPTIONS = Arrays.asList( "lines", "words", "bytes", "chars", "max-line-length", "help");

        private ParseArgs(String[] args) {
            if(args != null && args.length > 0) {
                this.args = new LinkedList<>(Arrays.asList(args));
            }
        }

        public List<CommandOption> parse() {
            if (this.args == null) return new ArrayList<>();

            for (String s : args) {
                if (s.startsWith("--")){
                    handleLong(s.substring(2));
                    continue;
                }
                if (s.startsWith("-")){
                    handleShort(s.substring(1));
                }
            }
            return enabled;
        }

        private void handleLong(String s) {
            if (LONG_OPTIONS.contains(s)) {
                enabled.add(handleOption(s));
            } else {
                printHelp();
            }
        }

        private void handleShort(String s) {
            for (int i=0; i<s.length();i++) {
                // break on help
                if (SHORT_OPTIONS.contains(String.valueOf(s.charAt(i)))) {
                    enabled.add(handleOption(String.valueOf(s.charAt(i))));
                } else {
                    printHelp();
                }
            }
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
                    printHelp();
            }
            return null;
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

        public boolean isBytes() {
            return this.shortForm.equals("c") || this.longForm.equalsIgnoreCase("bytes");
        }

        public boolean isChars() {
            return this.shortForm.equals("m") || this.longForm.equalsIgnoreCase("chars");
        }

        public boolean isLines() {
            return this.shortForm.equals("l") || this.longForm.equalsIgnoreCase("lines");
        }

        public boolean isWords() {
            return this.shortForm.equals("w") || this.longForm.equalsIgnoreCase("words");
        }

        public boolean isMaxLineLength() {
            return this.shortForm.equals("L") || this.longForm.equalsIgnoreCase("max-line-length");
        }

        public boolean isHelp() {
            return this.shortForm.equals("h") || this.longForm.equalsIgnoreCase("help");
        }
    }
}
