package it.aman;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

@Command(name = "ccwc", description = "Linux wc coding challenge")
public class App implements Runnable {

    @Option(names = {"-l", "--lines"}, description = "printResult newline count")
    private boolean lines;

    @Option(names = {"-w", "--words"}, description = "printResult word count")
    private boolean words;

    @Option(names = {"-c", "--bytes"}, description = "printResult bytes count")
    private boolean bytes;

    @Option(names = { "-h", "--help", "-?", "-help"}, usageHelp = true, description = "Display this help and exit")
    private boolean help;

    /**
     * -c, --bytes
     * printResult the byte counts
     * -m, --chars
     * printResult the character counts
     * -l, --lines
     * printResult the newline counts
     * --files0-from=F
     * read input from the files specified by NUL-terminated names in file F; If F is - then read names from standard input
     * -L, --max-line-length
     * printResult the length of the longest line
     * -w, --words
     * printResult the word counts
     * --help
     * display this help and exit
     * --version
     * output version information and exit
     */

    @Parameters
    List<String> fileNames;

    private String currentFile = "";

    List<Result> countResult = new ArrayList<>();

    public App() {
    }

    public static void main(String[] args) {
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

    private void readStdIn() {
        try (Scanner scanner = new Scanner(System.in);
             BufferedReader reader = new BufferedReader(new StringReader(scanner.nextLine()))) {
            eval(reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        printResult(System.out);
    }

    private void readFile() throws IOException {
        for (String file : fileNames) {
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile = file))) {
                eval(reader);
            } catch (FileNotFoundException e) {
                System.err.printf("File [ %s ] not found.%n", file);
                throw new RuntimeException(e);
            }
        }
        printResult(System.out);
    }

    private void applyDefaultOptions() {
        // default options
        if(Boolean.FALSE.equals(lines)
                && Boolean.FALSE.equals(words)
                && Boolean.FALSE.equals(bytes)) {
            lines = true; words = true; bytes = true;
        }
    }

    private void eval(BufferedReader reader) {
        try {
            long lineCount = 0, wordCount = 0, byteCount = 0;
            String current;
            while ((current = reader.readLine()) != null) {
                lineCount++;
                if(words) {
                    wordCount += countWords(current);
                }
                if(bytes) {
                    byteCount += current.getBytes().length;
                }
            }
            countResult.add(new Result(lineCount, wordCount, byteCount, currentFile));
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void printResult(OutputStream os) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (Result r : countResult) {
                if (lines) {
                    stringBuilder.append(r.lineCount).append(" ");
                }
                if (words) {
                    stringBuilder.append(r.wordCount).append(" ");
                }
                if (bytes) {
                    stringBuilder.append(r.byteCount).append(" ");
                }

                if (isNotBlank(currentFile)) {
                    stringBuilder.append(currentFile);
                }
                os.write(stringBuilder.toString().getBytes());
                os.write('\n');
                stringBuilder.setLength(0);
            }
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


    static class Result {
        final long lineCount;
        final long wordCount;
        final long byteCount;
        final String file;

        public Result(long lineCount, long wordCount, long byteCount, String file) {
            this.lineCount = lineCount;
            this.wordCount = wordCount;
            this.byteCount = byteCount;
            this.file = file;
        }
    }
}
