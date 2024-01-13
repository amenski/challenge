package it.aman;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {

    @Test
    public void parseArg() {
        App.ParseArgs parseArgs = new App.ParseArgs(new String[0]);
        assertEquals(0, parseArgs.parse().size());
    }

    @Test
    public void parseArg_fail_on_wrong_command() {
        App.ParseArgs parseArgs = new App.ParseArgs(new String[]{"--head"});
        App.ParseArgs parseArgs2 = new App.ParseArgs(new String[]{"-s"});

        assertEquals(1, parseArgs.parse().size());
        assertNotNull(parseArgs.parse().get("help"));

        assertEquals(1, parseArgs2.parse().size());
        assertNotNull(parseArgs2.parse().get("help"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void parseArg_success() {
        App.ParseArgs parseArgs = new App.ParseArgs(new String[]{"--chars", "ignore.file"});
        App.ParseArgs parseArgs2 = new App.ParseArgs(new String[]{"-l"});

        assertEquals(2, parseArgs.parse().size());
        assertNotNull(parseArgs.parse().get("chars"));
        assertNotNull(parseArgs.parse().get("files"));
        assertEquals("ignore.file", ((List<String>)parseArgs.parse().get("files")).get(0));

        assertEquals(1, parseArgs2.parse().size());
        assertNotNull(parseArgs2.parse().get("lines"));

    }
}
