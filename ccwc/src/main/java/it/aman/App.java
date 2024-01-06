package it.aman;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

@Command(name = "ccwc", description = "Linux wc coding challenge")
public class App implements Runnable {

    @Option(names = {"-l", "--lines"}, description = "print newline count")
    private boolean lines;

    @Option(names = {"-w", "--words"}, description = "print word count")
    private boolean words;

    @Option(names = {"-c", "--bytes"}, description = "print bytes count")
    private boolean bytes;

    /**
     * -c, --bytes
     * print the byte counts
     * -m, --chars
     * print the character counts
     * -l, --lines
     * print the newline counts
     * --files0-from=F
     * read input from the files specified by NUL-terminated names in file F; If F is - then read names from standard input
     * -L, --max-line-length
     * print the length of the longest line
     * -w, --words
     * print the word counts
     * --help
     * display this help and exit
     * --version
     * output version information and exit
     */

    @Parameters
    List<String> fileNames;

    private long lineCount = 0;
    private long wordCount = 0;
    private long byteCount = 0;
    private String currentFile = "";

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
            print(System.out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void readFile() throws IOException {
        for (String file : fileNames) {
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile = file))) {
                eval(reader);
                print(System.out);
            } catch (FileNotFoundException e) {
                System.err.printf("File [ %s ] not found.%n", file);
                throw new RuntimeException(e);
            }
        }
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
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void print(OutputStream os) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            if (lines) {
                stringBuilder.append(lineCount).append(" ");
            }
            if (words) {
                stringBuilder.append(wordCount).append(" ");
            }
            if (bytes) {
                stringBuilder.append(byteCount).append(" ");
            }

            if (isNotBlank(currentFile)) {
                stringBuilder.append(currentFile);
            }
            os.write(stringBuilder.toString().getBytes());
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
}
