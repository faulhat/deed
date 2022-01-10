import java.util.ArrayList;
import java.io.FileWriter;
import java.io.File;
import java.io.FileReader;

public class SerializationTest {
    public static void main(String[] args) throws Exception {
        ArrayList<Object> s = new ArrayList<>();
        s.add("Test");
        s.add("test");
        System.out.println(s);
        Sprite sp = Game.DialoguePoint.genSprite(s);
        DS.MapNode n = sp.dump();
        File f = new File("TestSerializationOutput.data.txt");
        FileWriter f_writer = new FileWriter(f);
        n.dump(f_writer);
        f_writer.close();
        FileReader reader = new FileReader(f);
        DS.Root root = DS.load(reader);
        if (root.complexVal.size() < 1) {
            throw new Exception("you need to at least have something in here right");
        }

        DS.Node firstSubNode = root.complexVal.get(0);
        if (!(firstSubNode instanceof DS.MapNode)) {
            throw new Exception("first root element needs to be mapnode");
        }

        Sprite deserializedSprite = new Sprite((DS.MapNode) firstSubNode);
        System.out.println(deserializedSprite.name);
        System.out.println(sp.equals(deserializedSprite));
        System.out.println();

        // Test serialization of Chamber and Square
        Chamber chamber = new Chamber();
        chamber.matrix[1][1] = new Chamber.Square(true);
        boolean didItWork = chamber.testSerialization();
        if (didItWork) {
            System.out.println("Chamber serialization and deserialization successful");
        }
        else {
            System.out.println("Serialization or deserialization of Chamber failed.");
        }
    }
}