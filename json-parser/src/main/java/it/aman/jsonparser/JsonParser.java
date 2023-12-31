package it.aman.jsonparser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class JsonParser {

    public static void main(String[] args) {

        try {
            Path path = Paths.get("json-parser/src/main/resources/tests/step4/valid3.json");
            List<String> lines = Files.readAllLines(path);
            new Lexer(String.join("", lines)).parseArray();
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }
}
