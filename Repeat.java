public class Repeat{
   public static String repeat(String s, int num){
      String toReturn = "";
      for (int i = 0; i < num; i++){
         toReturn += s;
      }
      return toReturn;
   }
}