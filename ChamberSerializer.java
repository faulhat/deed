import java.io.ObjectOutputStream;
import java.awt.Point;
import javax.naming.OperationNotSupportedException;
public class ChamberSerializer {
    public static String serialize(Chamber chamber) throws OperationNotSupportedException{
       String toReturn = "";
       for(int i = 0; i < chamber.height; i++){
         for (int j = 0; j < chamber.width; j++){
            toReturn += '(' + Integer.toString(i) + '|' + Integer.toString(j);
            if (chamber.getSquareAt(new Point(j,i)) != null){
               toReturn += '(' + Integer.toString(i) + '|' + Integer.toString(j);
               if (chamber.getSquareAt(new Point(j,i)).sprites.size() != 0){
                  for (Sprite s : chamber.getSquareAt(new Point(j,i)).sprites){
                     toReturn += s.getSymbol();
                  }
               }
               toReturn += ')';
            }
            else{
               toReturn += Integer.toString(i) + " | " + Integer.toString(j);
               toReturn += " | " + "null" + ')';
            }
         }
       }
       return toReturn;
    }
    public static Chamber deserializeChamber(String string){
      Chamber c = new Chamber();
      return c;
    }
}