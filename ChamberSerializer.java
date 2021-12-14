import java.io.ObjectOutputStream;
public class ChamberSerializer{
    public static String serializeChamber(Chamber chamber) {
       String toReturn = "";
       for(int i = 0; i < chamber.height; i++){
         for (int j = 0; j < chamber.width; j++){
            if (chamber.getSquareAt(new Point(j,i)) != null){
               if (chamber.getSquare(new Point(j,i)).sprites.size() == 0){
                  toReturn += Integer.toString(i) + "," + Integer.toString(j);
               }
               else{
                  for (Sprite s in chamber.getSquareAt(new Point(j,i)).sprites){
                     
                  }
               }
            }
            else{
            
            }
         }
       }
    }
    public static Chamber deserializeChamber(String string){
    
    }
}