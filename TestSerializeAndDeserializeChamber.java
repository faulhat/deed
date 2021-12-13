import java.io.*;
import java.awt.Point;
import java.util.ArrayList;
public class TestSerializeAndDeserializeChamber{
   public static void main(String[] args) throws Exception{
     Chamber c = new Chamber(13,13);
     //System.out.println(c.toString());

 
      // Serializing 'c'
      FileOutputStream fos = new FileOutputStream("SerializedChambers.txt");
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(c);  
      // De-serializing 'c'
      FileInputStream fis = new FileInputStream("SerializedChambers.txt");
      ObjectInputStream ois = new ObjectInputStream(fis);
      System.out.print(ois.available());
      //Chamber c2 = (Chamber)ois.readObject(); // down-casting object
      
      //System.out.println(c);
      //System.out.println(c2);
      //System.out.println(c2.toString());
      
  
      // closing streams
      oos.close();
      ois.close();
   }
}
