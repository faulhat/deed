import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Stack;

/*
 * Thomas: Here's a class all about storing data
 * We are using an s-expression based format to store game data.
 * It is loosely based on the Clojure EDN format but many features unnecessary for our purposes are absent.
 */

public class DataStorage {
    public static interface Storable {
        public void load(Reader reader); // Read serialized data from reader

        public void loads(String str); // Read serialized data from string

        public void dump(Writer writer); // Write serialized data to writer

        public String dumps(); // Return string containing serialized data
    }

    public static class ParserException extends Exception {
        public ParserException(int lineno, int colno, String msg) {
            super("Line " + lineno + " at char " + colno + ":\n" + msg);
        }
    }

    public static class StillParsingException extends ParserException {
        public StillParsingException(int lineno, int colno) {
            super(lineno, colno, "Error! EOF reached while parsing an expression!");
        }
    }

    public static class UnrecognizedEscapeSequenceException extends ParserException {
        public UnrecognizedEscapeSequenceException(int lineno, int colno, String s) {
            super(lineno, colno, "Error! Escape sequence not recognized: " + s);
        }
    }

    public static class TokenParsingException extends ParserException {
        public TokenParsingException(int lineno, int colno, String token) {
            super(lineno, colno, "Error! This token could not be properly parsed: " + token);
        }
    }

    public static abstract class Node {
        // Write a string containing a diagram of this node
        public abstract String walk(int depth);

        @Override
        public String toString() {
            return walk(1);
        }

        // Print out the parse tree
        public void print() {
            System.out.println(toString());
        }
    }

    public static abstract class ComplexNode extends Node {
        public ArrayList<Node> complexVal;

        public ComplexNode() {
            this.complexVal = new ArrayList<>();
        }

        public String walkSubordinates(int depth) {
            String out = "";

            for (Node subnode : complexVal) {
                out += subnode.walk(depth + 1);
            }

            return out;
        }
    }

    public static class Root extends ComplexNode {
        @Override
        public String walk(int depth) {
            return " ".repeat(depth) + " ROOT {-\n" + walkSubordinates(depth) + "-}\n";
        }
    }

    public static class ListNode extends ComplexNode {
        @Override
        public String walk(int depth) {
            return " ".repeat(depth) + "(\n" + walkSubordinates(depth) + " ".repeat(depth) + ")\n";
        }
    }

    public static class VectorNode extends ComplexNode {
        @Override
        public String walk(int depth) {
            return " ".repeat(depth) + "[\n" + walkSubordinates(depth) + " ".repeat(depth) + "]\n";
        }
    }

    public static class MapNode extends ComplexNode {
        @Override
        public String walk(int depth) {
            String out = " ".repeat(depth) + "{\n";

            for (int i = 0; i < complexVal.size(); i += 2) {
                out += "KEY: " + complexVal.get(i).walk(depth + 1);

                if (complexVal.size() > i + 1) {
                    out += "VALUE: " + complexVal.get(i).walk(depth + 1);
                }
            }

            return out + " ".repeat(depth) + "}\n";
        }
    }

    // Nodes represented by single tokens.
    public static abstract class SimpleNode extends Node {
        public abstract void finalize(String s);
    }

    public static class IdNode extends SimpleNode {
        public String name;

        @Override
        public void finalize(String name) {
            this.name = name;
        }

        @Override
        public String walk(int depth) {
            return " ".repeat(depth) + name + '\n';
        }
    }

    public static class KeywordNode extends SimpleNode {
        public String key;

        @Override
        public void finalize(String key) {
            this.key = key;
        }

        @Override
        public String walk(int depth) {
            return " ".repeat(depth) + ':' + key + '\n';
        }
    }

    public static class StringNode extends SimpleNode {
        public String value;

        @Override
        public void finalize(String value) {
            this.value = value;
        }

        @Override
        public String walk(int depth) {
            String out = " ".repeat(depth);

            if (value.length() > 10) { // We will truncate the string if it is longer than ten characters.
                for (int i = 0; i < 7; i++) {
                    switch (value.charAt(i)) {
                    case '\n':
                    case '\r':
                    case '\t':
                        // All of these will be replaced with slashes
                        out += '/';
                    default:
                        out += value.charAt(i);
                    }
                }

                out += "...";
            }
            else {
                for (int i = 0; i < value.length(); i++) {
                    switch (value.charAt(i)) {
                    case '\n':
                    case '\r':
                    case '\t':
                        // All of these will be replaced with slashes
                        out += '/';
                    default:
                        out += value.charAt(i);
                    }
                }
            }

            return out + "\"\n";
        }
    }

    public static class IntNode extends SimpleNode {
        public int value;

        @Override
        public void finalize(String s) {
            this.value = Integer.parseInt(s);
        }

        @Override
        public String walk(int depth) {
            return " ".repeat(depth) + value + "\n";
        }
    }

    public static class FloatNode extends SimpleNode {
        public double value;

        @Override
        public void finalize(String s) {
            this.value = Double.parseDouble(s);
        }

        @Override
        public String walk(int depth) {
            return " ".repeat(depth) + value + "\n";
        }
    }

    private static Root parse(Reader reader) throws IOException, ParserException, ClassCastException {
        Node frame = new Root();

        Stack<Node> stack = new Stack<>();

        // Read from reader until we reach the end of the input
        boolean inComment = false;

        // String for holding on to the characters read since we switched into the current state
        String temp = null;

        // Last character read;
        char last_c = 0;

        // Line and column number
        int lineno = 1, colno = 1;

        /* Start iterating through the input.
         * If we reach a character that prompts us to move into a different parsing mode, we push the current mode onto the "mode stack," and then switch.
         * This allows us to return to that state once we reach a termination character for the current state.
         */
        for (int next = reader.read(); next != -1; next = reader.read()) {
            char c = (char) next;

            if (inComment) {
                if (c == '\n') {
                    inComment = false;
                }
                else {
                    continue;
                }
            }
            
            if (c == ';') {
                inComment = true;
                continue;
            }

            boolean doPop = false; // Should we return to the previous state after this character?
            if (frame instanceof StringNode) {
                // Consume the next character, unless it is an unescaped double-quote.
                if (last_c == '\\') {
                    // Deal with escape sequences
                    if (c == '\\') {
                        temp += '\\';
                    }
                    else if (c == '"') {
                        temp += '\"';
                    }
                    else if (c == 'n') {
                        temp += '\n';
                    }
                    else if (c == 't') {
                        temp += '\t';
                    }
                    else if (c == 'r') {
                        temp += '\r';
                    }
                    else {
                        throw new UnrecognizedEscapeSequenceException(lineno, colno, "\\" + c);
                    }
                }
                else if (c == '"') { // If this character is an unescaped quote...
                    doPop = true; // ...the string is complete.
                }
                else if (c != '\\') { // Do not consume backslashes unless escaped
                    temp += c;
                }
            }
            if (Character.isWhitespace(c) || c == ',') { // Commas are considered whitespace in Lisp
                // Deal with whitespace
                if (frame instanceof StringNode) { // Consume whitespace if this is a string.
                    temp += c;
                }
                else if (frame instanceof SimpleNode) {
                    ((SimpleNode) frame).finalize(temp);
                    doPop = true;
                }
            }
            else if (c == '"') {
                if (frame instanceof SimpleNode) { // We already dealt with quotes in strings.
                    ((SimpleNode) frame).finalize(temp); // Finish parsing this token.
                    
                    // Store this node
                    Node prev = stack.peek();
                    assert(prev instanceof ComplexNode);
                    ((ComplexNode) prev).complexVal.add(frame);

                    frame = new StringNode(); // Start parsing the new string.
                }
                else {
                    stack.push(frame);
                    frame = new StringNode();
                }
            }
            else if (frame instanceof ListNode && c == ')' || frame instanceof VectorNode && c == ']' || frame instanceof MapNode && c == '}') {
                // If this is the end of a compound expresion...
                doPop = true;
            }
            else if (c == '(' || c == '[' || c == '{') { // If this is the beginning of a complex expression...
                if (frame instanceof SimpleNode) { // Whitespace is not required between simple and complex expressions, only simple expressions and other simple expressions.
                    ((SimpleNode) frame).finalize(temp);
                    
                    Node prev = stack.peek();
                    assert(prev instanceof ComplexNode);
                    ((ComplexNode) prev).complexVal.add(frame);
                }
                else {
                    stack.push(frame);
                }
                
                if (c == '(') {
                    frame = new ListNode();
                }
                else if (c == '[') {
                    frame = new VectorNode();
                }
                else if (c == '{') {
                    frame = new MapNode();
                }
            }
            else if (frame instanceof ComplexNode) {
                stack.push(frame); // Since we've already dealt with whitespace and closed-brackets, we know we will be encountering a new token.

                if (c >= '0' && c <= '9') {
                    frame = new IntNode();
                    temp = "" + c;
                }
                else if (c == '.') {
                    frame = new FloatNode();
                    temp = "" + c;
                }
                else if (c == ':') { // Keywords in Lisp are identifiers which start with colons.
                    frame = new KeywordNode();
                    temp = "";
                }
                else {
                    frame = new IdNode();
                    temp = "" + c;
                }
            }
            else if (frame instanceof IntNode) {
                temp += c;

                if (c == '.') {
                    frame = new FloatNode();
                }
                else if (!(c >= '0' && c <= '9')) { // c is not a numeral
                    throw new TokenParsingException(lineno, colno, temp);
                }
            }
            else if (frame instanceof FloatNode) {
                temp += c;

                if (!(c >= '0' && c <= '9')) {
                    throw new TokenParsingException(lineno, colno, temp);
                }
            }
            else { // Remaining cases are IdNode and KeywordNode, which can include any non-whitespace characters.
                temp += c;
            }
            
            // Store this node if the signal to do so has been given.
            if (doPop) {
                Node prev = stack.pop();
                assert(prev instanceof ComplexNode);
                ((ComplexNode) prev).complexVal.add(frame);

                frame = prev;
            }

            if (c == '\n') {
                lineno++;
                colno = 1;
            }
            else {
                colno++;
            }
        }

        // We have reached the end of the input. Finalize any simple expressions.
        if (frame instanceof SimpleNode && !(frame instanceof StringNode)) {
            ((SimpleNode) frame).finalize(temp);

            Node prev = stack.pop();
            assert(prev instanceof ComplexNode);
            ((ComplexNode) prev).complexVal.add(frame);

            frame = prev;
        }

        // Check to make sure all brackets and quotes were closed.
        if (!stack.empty()) {
            throw new StillParsingException(lineno, colno);
        }

        assert(frame instanceof Root);
        return (Root) frame;
    }

    // Same as above but takes a string instead of a reader
    public static Root parseString(String str) throws Exception {
        try (StringReader reader = new StringReader(str)) {
            return parse(reader);
        }
        catch (Exception e) {
            throw e;
        }
    }

    // Write the contents of the parse tree to a string
    public static String walk(Node node, int depth) {
        String out = "";

        if (node instanceof Root) {
            for (int i = 0; i < depth; i++) {
                out += '-';
            }

            out += " ROOT\n";

            for (Node subnode : ((Root) node).complexVal) {
                out += walk(subnode, depth + 1);
            }
        }

        return out;
    }
}