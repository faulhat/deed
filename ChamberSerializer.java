import java.awt.Point;
import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
//Note: I thought of using a '|' character to seperate the coordinates of points
//This might let us use .csv files
//Note 2: This code is incomplete
public class ChamberSerializer{
    public static String serializeChamber(Chamber chamber) throws OperationNotSupportedException {
       String toReturn = "";
       //Iterates through chamber and adds to a string, which will be added to a file 
       for(int i = 0; i < chamber.height; i++){
         for (int j = 0; j < chamber.width; j++){
<<<<<<< HEAD
            if (chamber.getSquareAt(new Point(j,i)) != null){
               if (chamber.getSquare(new Point(j,i)).sprites.size() == 0){
                  toReturn += Integer.toString(i) + "," + Integer.toString(j);
               }
               else{
                  for (Sprite s : chamber.getSquareAt(new Point(j,i)).sprites){
                     
=======
            Point squareAt = new Point(i,j);
            toReturn += '(' + Integer.toString(i) + '|' + Integer.toString(j) + ' ';
            if (chamber.getSquareAt(squareAt) != null){
               if (chamber.getSquareAt(squareAt).sprites.size() != 0){
                  for (Sprite s : chamber.getSquareAt(squareAt).sprites){
                     toReturn += ' ' + s.getID(); 
>>>>>>> 4b152923566c4b3fe4b02a98f525e87a7b6897d5
                  }
               }
            }
            toReturn += ')';
         }
       }
       return toReturn;
    }
    public static Chamber deserializeChamber(String string){
<<<<<<< HEAD
        Chamber c = new Chamber();
        return c;
=======
      double length = Math.sqrt((double)string.length());
      int useLength = (int)length;
      char[][] matrix = new char[useLength][useLength];
      ArrayList<ArrayList<ArrayList<Sprite>>> sprite_matrix = new ArrayList<ArrayList<ArrayList<Sprite>>>();
      //Iterates through string, parsing coordinates based on encoding format(See trello and TDD)
      for (int i = 0; i < string.length(); i++){
         char c = string.charAt(i);
         if (c == '('){
            int y = -1;
            int x = -1;
            while (c != ')'){
               i++;
               if ((int)c > 48 && (int)c < 57){
                  if (y == -1 && (int)string.charAt(i+1) > 48 && (int)string.charAt(i+1) < 57){
                     y = (int)(c)-48;
                  }
                  else{
                     x = (int)(c)-48;
                  }
                  break;
               }
               ArrayList<Sprite> sprites = sprite_matrix.get(y).get(x);
               if (x != -1 && y != -1){
                  if (c == 1){
                     sprites.add(new DialoguePoint("PLACEHOLDER_TEXT", "PLACEHOLDER"));
                  }
               } 
            }
         }
      }
      for (int i = 0; i < string.length(); i++){
         matrix[i/useLength][i%useLength] = string.charAt(i);
      }
      for (int i = 0; i < matrix.length; i++){
         for (int j = 0; j < matrix[0].length; j++){
            if (matrix[i][j] == '\n'){
            }
         }
      } 
>>>>>>> 4b152923566c4b3fe4b02a98f525e87a7b6897d5
    }
    //I just put this here because the error was irritating me lol
    Chamber c = new Chamber();
    return c;
}
