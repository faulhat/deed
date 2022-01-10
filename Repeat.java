public class Repeat {
   public static String repeat(String s, int num) {
      String toReturn = "";
      for (int i = 0; i < num; i++) {
         toReturn += s;
      }
      return toReturn;
   }

   public static String repeat2(String s, int count) {
      return repeat(s, count * 2);
   }
}