package com.github.dfauth.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.Collectors.oops;

@Slf4j
public class CSVReader {

    public static <T> Stream<Stream<String>> read(InputStream istream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
        reader.readLine(); // ignore header
        String line = null;
        Stream.Builder<Stream<String>> builder = Stream.builder();
        while((line = reader.readLine())!=null) {
            builder.add(Stream.of(tabify(line).split("\\|")).map(CSVReader::trimStrings));
        }
        return builder.build();
    }

    private static String trimStrings(String s) {
        if(s.startsWith("\"")) {
            return trimStrings(s.substring(1));
        } else if(s.endsWith("\"")) {
            return trimStrings(s.substring(0, s.length()-2));
        } else {
            return s.trim();
        }
    }

    private static String tabify(String line) {
        var state = new ParserState();
        return line.chars().mapToObj(i -> (char)i).map(state::process).reduce(new StringWriter(), (sw, c) -> {
            sw.write(c);
            return sw;
        }, oops()).toString();
    }

    private enum Nested {
        QUOTED,
        NONQUOTED;
        public Nested doubleQuote() {
            return this == QUOTED ? NONQUOTED : QUOTED;
        }

        public char doit(char c) {
            return this == NONQUOTED ? c == ',' ?  '|' : c : c;
        }
    }

    private static class ParserState {
        private Nested nested = Nested.NONQUOTED;
        public char process(char c) {
            if(c == '\"') {
                nested = nested.doubleQuote();
            }
            return nested.doit(c);
        }
    }
}
