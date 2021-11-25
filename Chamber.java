import java.awt.Point;

/*
 * Thomas: This class represents an overworld chamber
 * It has a matrix of booleans. true elements represent walls
 * and false elements represent floors.
 * It also has a matrix of Sprite objects representing the sprite
 * on each square. A null element means there's nothing on that square.
 */
public class Chamber {
  private boolean[][] walls;

  public boolean isWallAt(Point point) {
    return walls[point.x][point.y];
  }

  private Sprite[][] sprites;

  public Sprite getSpriteAt(Point point) {
    return sprites[point.x][point.y];
  }

  public Chamber(
    boolean[][] walls,
    Sprite[][] sprites
  ) {
    assert(walls.length > 0);
    assert(walls.length == sprites.length);
    int lenWalls = walls[0].length;
    for (int i = 1; i < walls.length; i++) {
      assert(walls[i].length == lenWalls);
    }
    int lenSprites = sprites[0].length;
    for (int i = 1; i < sprites.length; i++) {
      assert(sprites[i].length == lenWalls);
    }
    assert(lenWalls == lenSprites);

    this.walls = new boolean[walls.length][lenWalls];
    for (int i = 0; i < walls.length; i++) {
      for (int j = 0; j < lenWalls; j++) {
        this.walls[i][j] = walls[i][j];
      }
    }

    this.sprites = new Sprite[sprites.length][lenSprites];
    for (int i = 0; i < sprites.length; i++) {
      for (int j = 0; j < lenSprites; j++) {
        this.sprites[i][j] = sprites[i][j];
      }
    }
  }
}