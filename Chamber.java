import java.awt.Point;
import java.util.ArrayList;

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

  private final int width;

  public int getWidth() {
    return width;
  }

  private final int height;

  public int getHeight() {
    return height;
  }

  private final Square[][] matrix;

  public Square getSquareAt(Point point) {
    return matrix[point.x][point.y];
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
}