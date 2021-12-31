import java.awt.Point;
import java.util.ArrayList;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.HashMap;

/*
 * Thomas: This class represents an overworld chamber
 * It has a matrix of booleans. true elements represent walls
 * and false elements represent floors.
 * It also has a matrix of Sprite objects representing the sprite
 * on each square. A null element means there's nothing on that square.
 */
public class Chamber implements DS.Storable {
  public static class Square implements DS.Storable {
    public boolean isWall;

    public ArrayList<Sprite> sprites; // We can still change this externally, because sprites move around.

    public static class SquareLoadingException extends LoadingException {
      public SquareLoadingException(String complaint) {
        super("Square", complaint);
      }
    }

    public Square() {
      this.isWall = false;
      this.sprites = new ArrayList<>();
    }

    public Square(DS.Node node) throws LoadingException {
      this();
      load(node);
    }

    public Square(boolean isWall, ArrayList<Sprite> sprites) {
      this.isWall = isWall;
      this.sprites = new ArrayList<>();
      this.sprites.addAll(sprites);
    }

    @Override
    public void load(DS.Node node) throws LoadingException {
      if (!(node instanceof DS.MapNode)) {
        throw new SquareLoadingException("Can only take a map node.");
      }

      HashMap<DS.Node, DS.Node> map = ((DS.MapNode) node).getMap();
      DS.KeywordNode isWallKeyNode = new DS.KeywordNode("isWall");
      if (!map.containsKey(isWallKeyNode)) {
        throw new SquareLoadingException("No isWall boolean found.");
      }

      DS.Node isWallValNode = map.get(isWallKeyNode);
      if (!(isWallValNode instanceof DS.IdNode && ((DS.IdNode) isWallValNode).isBool())) {
        throw new SquareLoadingException("isWall parameter is invalid.");
      }

      isWall = ((DS.IdNode) isWallValNode).isTrue();

      DS.KeywordNode spritesKeyNode = new DS.KeywordNode("sprites");
      if (!map.containsKey(spritesKeyNode)) {
        throw new SquareLoadingException("No sprite list found.");
      }

      DS.Node spritesValNode = map.get(spritesKeyNode);
      if (!(spritesValNode instanceof DS.VectorNode)) {
        throw new SquareLoadingException("sprites parameter is invalid.");
      }

      for (DS.Node spriteNode : ((DS.VectorNode) spritesValNode).complexVal) {
        sprites.add(new Sprite(spriteNode));
      }
    }

    @Override
    public DS.MapNode dump() {
      DS.MapNode map = new DS.MapNode();

      map.complexVal.add(new DS.KeywordNode("isWall"));
      if (isWall) {
        map.complexVal.add(new DS.IdNode("true"));
      } else {
        map.complexVal.add(new DS.IdNode("false"));
      }

      map.complexVal.add(new DS.KeywordNode("sprites"));
      DS.VectorNode spritesNode = new DS.VectorNode();
      for (Sprite sprite : sprites) {
        spritesNode.complexVal.add(sprite.dump());
      }

      map.complexVal.add(spritesNode);
      return map;
    }
  }

  public static final int width = 40;

  public static final int height = 30;

  public Square[][] matrix;

  public static class ChamberLoadingException extends LoadingException {
    public ChamberLoadingException(String complaint) {
      super("Chamber", complaint);
    }
  }

  public Chamber() {
    this.matrix = new Square[width][height];
  }

  public Chamber(DS.Node node) throws LoadingException {
    load(node);
  }

  // Construct new instance by deep-copying arrays for the walls and sprites
  public Chamber(Square[][] matrix) {
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
  }

  public Chamber(int width, int height) {
    matrix = new Square[height][width];
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        ArrayList<Sprite> s = new ArrayList<Sprite>();
        Square toAdd = new Square(false, s);
        matrix[i][j] = toAdd;
      }
    }
  }

  public Chamber(char[][] c) {
    matrix = new Square[height][width];

    for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < matrix[0].length; j++) {
        ArrayList<Sprite> s = new ArrayList<Sprite>();
        Square toAdd = new Square(false, s);
        matrix[i][j] = toAdd;
      }
    }
  }

  // toString for testing only thomas dont get mad
  public String toString() {
    String toReturn = "";
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (matrix[i][j].isWall) {
          toReturn += "|";
        } else {
          toReturn += " ";
        }
      }
      toReturn += "\n";
    }
    return toReturn;
  }

  @Override
  public void load(DS.Node node) throws LoadingException {
    matrix = new Square[width][height];

    if (!(node instanceof DS.VectorNode)) {
      throw new ChamberLoadingException("Node passed is invalid.");
    }

    DS.VectorNode vectorNode = (DS.VectorNode) node;
    if (vectorNode.complexVal.size() != width) {
      throw new ChamberLoadingException("Vector passed must be of a length equal to the Chamber class constant width.");
    }
    
    for (int i = 0; i < width; i++) {
      DS.Node colNode = vectorNode.complexVal.get(i);
      if (!(colNode instanceof DS.VectorNode)) {
        throw new ChamberLoadingException("Node passed is invalid.");
      }

      DS.VectorNode colVector = (DS.VectorNode) colNode;
      for (int j = 0; j < height; j++) {
        DS.Node squareNode = colVector.complexVal.get(j);
        if (squareNode instanceof DS.IdNode && ((DS.IdNode) squareNode).isNil()) {
          matrix[i][j] = null;
        }

        matrix[i][j] = new Square(colVector.complexVal.get(j));
      }
    }
  }

  @Override
  public DS.VectorNode dump() {
    DS.VectorNode node = new DS.VectorNode();

    for (int i = 0; i < width; i++) {
      DS.VectorNode subnode = new DS.VectorNode();
      for (int j = 0; j < height; j++) {
        if (matrix[i][j] == null) {
          subnode.complexVal.add(new DS.IdNode("nil"));
        }

        subnode.complexVal.add(matrix[i][j].dump());
      }

      node.complexVal.add(subnode);
    }

    return node;
  }
}
