import java.awt.Point;
import java.util.ArrayList;
import java.awt.geom.Point2D;
import java.io.*;

/*
 * Thomas: This class represents an overworld chamber
 * It has a matrix of booleans. true elements represent walls
 * and false elements represent floors.
 * It also has a matrix of Sprite objects representing the sprite
 * on each square. A null element means there's nothing on that square.
 */
public class Chamber implements Serializable{
  public static class Square {
    public final boolean isWall;
   
    public transient ArrayList<Sprite> sprites; // We can still change this externally, because sprites move around.
   
    public Square(boolean isWall, ArrayList<Sprite> sprites) {
      this.isWall = isWall;
      this.sprites = new ArrayList<>();
      this.sprites.addAll(sprites);
    }
    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException{
      stream.defaultReadObject();
      for (int i = 0; i < sprites.size(); i++){
         sprites.add((Sprite)stream.readObject());
      }
    }
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException{
      stream.writeBoolean(isWall);
      for (int i = 0; i < sprites.size(); i++){
         sprites.get(i).writeObject(stream);
      }
    }
    private void readObjectNoData() throws ObjectStreamException{
      
    }
    
  }

  public final int width;

  public final int height;

  private  transient Square[][] matrix;

  public Square getSquareAt(Point point) {
    return matrix[point.x][point.y];
  }
  public Square getSquareAt(Point2D.Double point){
    return matrix[(int)point.x][(int)point.y];
  }
  public boolean isWall(Point2D.Double point){
    return matrix[(int)point.x][(int)point.y].isWall;
  }
  private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
  
  }
  private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        for (int i = 0; i < height; i++){
         for (int j = 0; j < width; j++){
          matrix[i][j].writeObject(oos);
         }
        }
      
  }
  public void readObjectNoData() throws ObjectStreamException{
  
  }
  
  // Construct new instance by deep-copying arrays for the walls and sprites
  public Chamber(int width, int height, Square[][] matrix) {
    // Integrity check: make sure the dimensions of both arrays match those
    // specified.
    assert (matrix.length == height);
    for (int i = 1; i < height; i++) {
      assert (matrix[i].length == width);
    }

    this.matrix = new Square[width][height];
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        this.matrix[i][j] = matrix[i][j];
      }
    }

    this.width = width;
    this.height = height;
  }
  public Chamber(int width, int height){
    this.width = width;
    this.height = height;
    matrix = new Square[height][width];
    for (int i = 0; i < height; i++){
      for (int j = 0; j < width; j++){
         ArrayList<Sprite> s = new ArrayList<Sprite>();
         Square toAdd = new Square(false, s);
         matrix[i][j] = toAdd;
      }
    }
  }
  public Chamber(char[][] c){
   width = c[0].length;
   height = c.length;
   matrix = new Square[height][width];
   
   for (int i = 0; i < matrix.length; i++){
      for (int j = 0; j < matrix[0].length; j++){
         ArrayList<Sprite> s = new ArrayList<Sprite>();
         Square toAdd = new Square(false, s);
         matrix[i][j] = toAdd;
      }
   }
  }
  //Default Constructor for Chamber
  public Chamber(){
      this.width = 13;
      this.height = 13;
      this.matrix = new Square[width][height];
         for (int i = 0; i < width-1; i++){
         for (int j = 0; j < height; j++){
            ArrayList<Sprite> s = new ArrayList<Sprite>();
            if (j > 6 && i < 6){
               Square toPut = new Square(true,s);
               matrix[i][j] = toPut;
            }
            else{
               Square toPut = new Square(false,s);
               matrix[i][j] = toPut;
            }
         }
      }
  }
  //toString for testing only thomas dont get mad
  public String toString(){
   String toReturn = "";
   for (int i = 0; i <height; i++){
      for (int j = 0; j < width; j++){
         if (matrix[i][j].isWall){
            toReturn += "|";
         }
         else{
           toReturn += " ";
         }
      }
      toReturn+= "\n";
   }
   return toReturn;
  
  }

  
}
