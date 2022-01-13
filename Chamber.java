import java.awt.Point;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.geom.Point2D;
import java.util.EnumMap;

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

    public Square(boolean isWall) {
      this.isWall = isWall;
      this.sprites = new ArrayList<>();
    }

    public Square() {
      this(false);
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
    public void eventOn(Game.Event eventToPut) throws InterruptedException{
      for (Sprite sp : sprites){
         sp.onEvent(eventToPut);
      }
    }
    // Method to deserialize from node
    @Override
    public void load(DS.Node node) throws LoadingException {
      try {
        if (!(node instanceof DS.MapNode)) {
          throw new SquareLoadingException("Can only take a map node.");
        }

        HashMap<String, DS.Node> map = ((DS.MapNode) node).getMap();
        if (!map.containsKey(":iswall")) {
          throw new SquareLoadingException("No isWall boolean found.");
        }

        DS.Node isWallValNode = map.get(":iswall");
        if (!(isWallValNode instanceof DS.IdNode && ((DS.IdNode) isWallValNode).isBool())) {
          throw new SquareLoadingException("isWall parameter is invalid.");
        }

        isWall = ((DS.IdNode) isWallValNode).isTrue();

        if (!map.containsKey(":sprites")) {
          throw new SquareLoadingException("No sprite list found.");
        }

        DS.Node spritesValNode = map.get(":sprites");
        if (!(spritesValNode instanceof DS.VectorNode)) {
          throw new SquareLoadingException("sprites parameter is invalid.");
        }

        for (DS.Node spriteNode : ((DS.VectorNode) spritesValNode).complexVal) {
          sprites.add(new Sprite(spriteNode));
        }
      } catch (DS.MapNode.NonDeserializableException e) {
        System.out.println("Error: Nondeserializable");
      }
    }

    // Method to serialize a chamber
    @Override
    public DS.MapNode dump() {
      DS.MapNode map = new DS.MapNode();

      map.complexVal.add(new DS.KeywordNode("iswall"));
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

    @Override
    public boolean equals(Object otherObj) {
      if (!(otherObj instanceof Square)) {
        return false;
      }

      Square other = (Square) otherObj;
      if (other.isWall != isWall) {
        return false;
      }

      if (!(other.sprites.size() == sprites.size())) {
        return false;
      }

      for (int i = 0; i < sprites.size(); i++) {
        if (!other.sprites.get(i).equals(sprites.get(i))) {
          return false;
        }
      }

      return true;
    }
  }

  public static final int width = 40;

  public static final int height = 25;

  public Square[][] matrix;

  public Point fromUpDrop,
      fromDownDrop,
      fromLeftDrop,
      fromRightDrop;
  public EnumMap<Game.Direction, Point> directionDropGetter;

  public static class ChamberLoadingException extends LoadingException {
    public ChamberLoadingException(String complaint) {
      super("Chamber", complaint);
    }
  }

  public Chamber() {
    this.matrix = new Square[height][width];
    this.directionDropGetter = new EnumMap<>(Game.Direction.class);
    /*
     * Default drop positions are at halfway up the chamber at left/right edge for
     * left/right entry
     * respectively,
     * and halfway acroos at top/bottom for top and bottom
     */
    int y_horizontal = width / 2;
    int x_horizontal = height / 2;
    fromUpDrop = new Point(y_horizontal, height-1);
    fromDownDrop = new Point(y_horizontal, 0);
    fromLeftDrop = new Point(0, x_horizontal);
    fromRightDrop = new Point(width-1, x_horizontal);
    directionDropGetter.put(Game.Direction.UP, fromUpDrop);
    directionDropGetter.put(Game.Direction.DOWN, fromDownDrop);
    directionDropGetter.put(Game.Direction.LEFT, fromLeftDrop);
    directionDropGetter.put(Game.Direction.RIGHT, fromRightDrop);
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        ArrayList<Sprite> s = new ArrayList<Sprite>();
        Square toAdd = new Square(false, s);
        matrix[j][i] = toAdd;
      }
    }

  }

  public Chamber(DS.Node node) throws LoadingException {
    this();
    
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

  public Chamber(int width) {
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

  @Override
  public void load(DS.Node node) throws LoadingException {
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
          matrix[j][i] = null;
        }
        else {
          matrix[j][i] = new Square(colVector.complexVal.get(j));
      
        }
      }
    }
  }

  @Override
  public DS.VectorNode dump() {
    DS.VectorNode node = new DS.VectorNode();

    for (int i = 0; i < width; i++) {
      DS.VectorNode subnode = new DS.VectorNode();
      for (int j = 0; j < height; j++) {
        if (matrix[j][i] == null) {
          subnode.complexVal.add(new DS.IdNode("nil"));
        }
        else {
          subnode.complexVal.add(matrix[j][i].dump());
        }
      }

      node.complexVal.add(subnode);
    }

    return node;
  }

  @Override
  public boolean equals(Object otherObj) {
    if (!(otherObj instanceof Chamber)) {
      return false;
    }

    Chamber other = (Chamber) otherObj;

    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        if (other.matrix[j][i] == null) {
          if (matrix[j][i] == null) {
            continue;
          }

          return false;
        }

        if (!other.matrix[j][i].equals(matrix[j][i])) {
          return false;
        }
      }
    }

    return true;
  }

  public boolean testSerialization() throws Exception {
    File out = new File("SerializedChamber.data.txt");
    try (FileWriter writer = new FileWriter(out)) {
      dump().dump(writer);
    }

    try (FileReader reader = new FileReader(out)) {
      DS.Root retrievedNode = DS.load(reader);
      retrievedNode.print();

      if (retrievedNode.complexVal.size() != 1) {
        System.out.println("Serialization of Chamber failed (1).");
        return false;
      }
      
      DS.Node firstNode = retrievedNode.complexVal.get(0);
      if (!(firstNode instanceof DS.VectorNode)) {
        System.out.println("Serialization of Chamber failed (2).");
        return false;
      }

      Chamber retrievedChamber = new Chamber((DS.VectorNode) firstNode);
      if (!equals(retrievedChamber)) {
        System.out.println("Deserialized Chamber did not equal original.");
        return false;
      }
    }

    return true;
  }
  public void eventAtPos(Point2D.Double pos, Game.Event e) throws InterruptedException{
   Square toActOn = matrix[(int)pos.y][(int)pos.x];
   toActOn.eventOn(e);
  }
  public void insertAtPoint(Sprite insert, Point2D.Double where){
    matrix[(int)where.y][(int)where.x].sprites.add(insert);
  }
}
