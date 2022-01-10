import java.util.ArrayList;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
public class SerializationTest{
    public static void main(String[] args) throws FileNotFoundException, IOException, Exception{
        ArrayList<Object> s = new ArrayList<>();
        s.add("Test");
        s.add("test");
        System.out.println(s);
        Sprite sp = Game.DialoguePoint.genSprite(s);
        DS.MapNode n = sp.dump();
        File f = new File("TestSerializationOutput.txt");
        FileWriter f_writer = new FileWriter(f);
        n.dump(f_writer);
        f_writer.close();
        FileReader f_in = new FileReader(f);
        BufferedReader f_reader = new BufferedReader(f_in);
        DS.Root r = DS.load(f_reader);
        String readFrom = r.walk(0);
        System.out.println(readFrom);
        
    }
}