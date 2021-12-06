import java.awt.Point;
import java.util.ArrayList;
import java.awt.geom.Point2D;

/*
 * Thomas: This class represents an overworld chamber
 * It has a matrix of booleans. true elements represent walls
 * and false elements represent floors.
 * It also has a matrix of Sprite objects representing the sprite
 * on each square. A null element means there's nothing on that square.
 */
public class Chamber {
  public static class Square {
    public final boolean isWall;
    
    public ArrayList<Sprite> sprites; // We can still change this externally, because sprites move around.
   
    public Square(boolean isWall, ArrayList<Sprite> sprites) {
      this.isWall = isWall;
      this.sprites = new ArrayList<>();
      this.sprites.addAll(sprites);
    }
  }

  public final int width;

  public final int height;

  private Square[][] matrix;

  public Square getSquareAt(Point point) {
    return matrix[point.x][point.y];
  }
  public boolean isWall(Point2D.Double point){
    return matrix[(int)point.x][(int)point.y].isWall;
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
  //Default Constructor for Chamber
  public Chamber(){
      this.width = 13;
      this.height = 13;
      this.matrix = new Square[width][height];
      for (int i = 0; i < width; i++){
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
}