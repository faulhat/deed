import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import javax.swing.text.html.HTMLEditorKit.Parser;

/*
 * Thomas: Here's a class all about storing data
 * We are using an s-expression based format to store game data
 */

public class DataStorage {
    public static interface Storable {
        public void load(Reader reader); // Read serialized data from reader

        public void loads(String str); // Read serialized data from string

        public void dump(Writer writer); // Write serialized data to writer

        public String dumps(); // Return string containing serialized data
    }

    public static enum ParserMode {
        TOP, LIST, ID, KEYWORD, NUMBER, VECTOR, MAP, SET
    }

    public static boolean isSimple(ParserMode mode) {
        return mode == ParserMode.ID || mode == ParserMode.KEYWORD || mode == ParserMode.NUMBER;
    }

    public static class StillParsingException extends Exception {
        public StillParsingException() {
            super("Error! EOF reached while parsing an expression!");
        }
    }

    public static class ParsedObject {
        public static enum ObjType {
            LIST, VECTOR, ID, KEYWORD, INT, FLOAT, MAP, SET
        }

        public ObjType myType;

        public Object value;

        public ParsedObject(ObjType myType, Object value) {
            this.myType = myType;
            this.value = value;
        }
    }

    private static ParsedObject parse(Reader reader, ParserMode mode, Integer carried) throws IOException, StillParsingException {
        ParsedObject value = null;
        if (mode == ParserMode.LIST) {
            value = new ParsedObject(ParsedObject.ObjType.LIST, new ArrayList<Object>());
        }
        else if (mode == ParserMode.TOP || mode == ParserMode.VECTOR) {
            value = new ParsedObject(ParsedObject.ObjType.VECTOR, new ArrayList<Object>());
        }
        else if (mode == ParserMode.MAP) {
            value = new ParsedObject(ParsedObject.ObjType.MAP, new HashMap<Object, Object>());
        }
        else if (mode == ParserMode.SET) {
            value = new ParsedObject(ParsedObject.ObjType.SET, new HashSet<Object>());
        }

        Object temp = null; // Temporary value for use in MAP mode

        // Read from reader until we reach the end of the input
        boolean inComment = false;

        int next;
        if (carried == null) {
            next = reader.read();
        }
        else {
            // The last character read may be significant
            next = carried;
        }

        for (; next != -1; next = reader.read()) {
            char c = (char) next;

            if (inComment) {
                if (c == '\n') {
                    if (isSimple(mode)) {
                        break;
                    }

                    inComment = false;
                }
            }
            else if (Character.isWhitespace(c) || c == ',') { // Commas are considered whitespace in Lisp
                if (isSimple(mode)) {
                    break;
                }
            }
            else if (c == '(' || c == '[' || c == '{' || c == '#') {
                if (isSimple(mode)) {
                    break;
                }

                Object newVal = null;
                if (c == '(') {
                    newVal = parse(reader, ParserMode.LIST, null);
                }
                else if (c == '[') {
                    newVal = parse(reader, ParserMode.VECTOR, null);
                }
                else if (c == '{') {
                    newVal = parse(reader, ParserMode.MAP, null);
                }
                else if (c == '#') {
                    next = reader.read();

                    if (reader.read() == -1) {
                        throw new StillParsingException();
                    }
                    else {
                        c = (char) next;

                        // The syntax "#{a b c}" is used for sets
                        if (c == '{') {
                            newVal = parse(reader, ParserMode.SET, null);
                        }
                    }
                }
                
                if (mode == ParserMode.TOP || mode == ParserMode.LIST || mode == ParserMode.VECTOR) {
                    try {
                        ((ArrayList<Object>) value.value).add(newVal);
                    }
                    catch (ClassCastException e) {
                        throw e;
                    }
                }
                else if (mode == ParserMode.MAP) {
                    // Syntax for maps is "{ key0 val0 key1 val1 }"
                    try {
                        if (temp == null) {
                            temp = load(reader, ParserMode.LIST, null);
                        }
                        else {
                            ((HashMap<Object, Object>) value.value).put(temp, newVal);
                            temp = null;
                        }
                    }
                    catch (ClassCastException e) {
                        throw e;
                    }
                }
                else if (mode == ParserMode.SET) {
                    try {
                        ((HashSet<Object>) value.value).add(newVal);
                    }
                    catch (ClassCastException e) {
                        throw e;
                    }
                }
            }
        }

        return value;
    }

    public static Object load(Reader reader) throws Exception {
        try {
            return parse(reader, ParserMode.TOP, null);
        }
        catch (Exception e) {
            throw e;
        }
    }

    public static Object loads(String str) throws Exception {
        try (StringReader reader = new StringReader(str)) {
            return load(reader);
        }
        catch (Exception e) {
            throw e;
        }
    }
}
