package it.aman;

import picocli.CommandLine;
import picocli.CommandLine.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@Command(name = "ccwc", description = "Linux wc coding challenge")
public class App implements Runnable {

    @Option(names = {"-l", "--lines"}, description = "print newline count")
    private boolean lines;

    @Option(names = {"-w", "--words"}, description = "print word count")
    private boolean words;

    @Option(names = {"-c", "--bytes"}, description = "print bytes count")
    private boolean chars;

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

    public App() {
    }

    public static void main(String[] args) {
        new CommandLine(new App()).execute(args);
    }

    @Override
    public void run() {
        if(fileNames != null && !fileNames.isEmpty()) {
            processFiles();
        } else {
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            System.out.println(eval(Collections.singletonList(line)));
        }
    }

    private void processFiles() {
        for (String file : fileNames) {
            try {
                System.out.println(eval(Files.readAllLines(Paths.get(file))) + " " + file);
            } catch (NoSuchFileException e) {
                System.err.printf("File [ %s ] not found.%n", file);
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }
    }

    private String eval(List<String> input) {
        StringBuilder stringBuilder = new StringBuilder();
        if (lines) {
            stringBuilder.append(input.size()).append(" ");
        }
        if (words) {
            stringBuilder.append(countWords(input)).append(" ");
        }
        if (chars) {
            stringBuilder.append(countBytes(input)).append(" ");
        }
        return stringBuilder.toString();
    }

    private long countWords(List<String> linesList) {
        int sum = 0;
        for (String s : linesList) {
            boolean word = false;
            for (char c : s.toCharArray()) {
                if (!Character.isWhitespace(c)) {
                    if (word) {
                        continue;
                    }
                    sum++;
                    word = true;
                } else {
                    word = false;
                }
            }
        }
        return sum;
    }

    private long countBytes(List<String> input) {
        return input.stream().mapToLong(String::length).sum();
    }

    private boolean isNotBlank(String input) {
        int length = input.length();
        while(length > 0) {
            if(Character.isLetterOrDigit(input.charAt(--length))) return true;
        }
        return false;
    }
}
