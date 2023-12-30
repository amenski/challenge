package it.aman.jsonparser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

public class JsonParser {

    public static void main(String[] args){

        try {
            Path path = Paths.get("src/main/resources/tests/step3/invalid.json");
            List<String> lines = Files.readAllLines(path);
            new Lexer(String.join("", lines)).parseObject();
        } catch (Exception ignore){
            ignore.printStackTrace();
        }
}
}
