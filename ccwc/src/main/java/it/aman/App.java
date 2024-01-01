package it.aman;

import picocli.CommandLine;
import picocli.CommandLine.*;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Command(name ="ccwc", description = "Linux wc coding challenge")
public class App implements Runnable {

    @Option(names = {"-l", "--lines"}, description = "print the newline counts")
    private boolean lines;

    @Option(names = {"-w", "--words"}, description = "print the word counts")
    private boolean words;

    /**
     *
     -c, --bytes
     print the byte counts
     -m, --chars
     print the character counts
     -l, --lines
     print the newline counts
     --files0-from=F
     read input from the files specified by NUL-terminated names in file F; If F is - then read names from standard input
     -L, --max-line-length
     print the length of the longest line
     -w, --words
     print the word counts
     --help
     display this help and exit
     --version
     output version information and exit
     */

    @Parameters
    List<String> fileNames;

    public App() {
    }

    public static void main(String[] args ) {
        new CommandLine(new App()).execute(args);
    }

    @Override
    public void run() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String file : fileNames) {
            if (lines) {
                stringBuilder.append(countLines(file)).append(" ");
            }
            System.out.println(stringBuilder + " " + file);

            //reset
            stringBuilder.setLength(0);
        }
    }

    private long countLines(String fileName) {
        try(Stream<String> stream = Files.lines(Paths.get(fileName))) {
            return stream.count();
        } catch (NoSuchFileException e) {
            System.err.printf("File [ %s ] not found.%n", fileName);
        } catch (Exception ignore) {
            //
        }
        return 0;
    }
}
