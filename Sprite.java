import java.util.Map;

import java.util.ArrayList;
import java.awt.geom.Point2D;

public class Sprite extends LevelEditor.Insertable implements DS.Storable{
  public boolean visible;

  public Game.SpriteType type;

  public Template.HandlerMap handlerMap;

  public String name;

  public char symbol;
  // String representing name of location of sprite
  public String location;

  public Game game;

  public ArrayList<Object> uniqueData;

  public Sprite(String name) {
    this.visible = false;
    this.name = name;
    this.symbol = ' ';
  }

  public Sprite(String name, Character symbol) {
    this.visible = true;
    this.name = name;
    this.symbol = symbol;
  }

  public Sprite(String name, char symbol, Game.SpriteType type, String location) {
    this(name, symbol);
    this.type = type;
    this.location = location;
  }

  public void onEvent(Game.Event e) throws InterruptedException {
    Template.Handler handler = handlerMap.get(e.eventType);

    if (handler != null) {
      handler.accept(e, this);
    }
  }

  public void insert(){
    Point2D.Double insertpos = Game.pos;
    
  }

  public DS.MapNode dump() {
    DS.MapNode mapOfSprite = new DS.MapNode();
    String isVis = "";
    if (this.visible = true) {
      isVis = "true";
    } else {
      isVis = "false";
    }
    // Encoding all fields as nodes to be written to files
    DS.Node vis_key = new DS.KeywordNode("visible"),
        visible = new DS.IdNode(isVis),
        type_key = new DS.KeywordNode("spriteType"),
        type = new DS.IdNode("Dialogue_Point"),
        name_key = new DS.KeywordNode("name"),
        name = new DS.IdNode(this.name),
        location_key = new DS.KeywordNode("location"),
        location = new DS.IdNode(this.location),
        uniqueDataKey = new DS.KeywordNode("uniqueData");
    DS.ComplexNode uniqueData = new DS.ListNode();

    for (int i = 0; i < this.uniqueData.size(); i++) {
      System.out.println(this.uniqueData.get(i));
      DS.SimpleNode uniqueDataElement = null;
      if (this.uniqueData.get(i) instanceof String) {
        uniqueDataElement = new DS.IdNode((String) this.uniqueData.get(i));
      } else {
        uniqueDataElement = new DS.UniqueNode(this.uniqueData.get(i));
      }
      uniqueData.complexVal.add(uniqueDataElement);
    }
    mapOfSprite.complexVal.add(vis_key);
    mapOfSprite.complexVal.add(visible);
    mapOfSprite.complexVal.add(type_key);
    mapOfSprite.complexVal.add(type);
    mapOfSprite.complexVal.add(name_key);
    mapOfSprite.complexVal.add(name);
    mapOfSprite.complexVal.add(location_key);
    mapOfSprite.complexVal.add(location);
    mapOfSprite.complexVal.add(uniqueDataKey);
    mapOfSprite.complexVal.add(uniqueData);
    return mapOfSprite;
  }

  public void load(DS.Node node) throws LoadingException {
    DS.MapNode mNode = (DS.MapNode) node;
    this.uniqueData = new ArrayList<>();
    try {
      Map<String, DS.Node> map = mNode.getMap();
      DS.Node valNode = map.get(":name");

      if (!(valNode instanceof DS.IdNode)) {
        throw new LoadingException("Sprite", "Value for name field is not an IdNode!");
      }

      name = ((DS.IdNode) valNode).name;

      DS.IdNode isVisible = (DS.IdNode) map.get(":visible");
      if (isVisible.isBool()) {
        visible = isVisible.isTrue();
      } else {
        throw new LoadingException("Sprite", "IdNode not true/false resolvable");
      }
      DS.IdNode spriteTypeNode = (DS.IdNode) map.get(":spriteType");
      String spriteType = spriteTypeNode.name;
      for (Game.SpriteType s : Game.SpriteType.values()) {
        if (spriteType.equals(s.name())) {
          type = s;
        }
      }
      this.symbol = Game.spriteTypeBindings.get(type).symbol;
      DS.ListNode uniquesList = (DS.ListNode) map.get(":uniqueData");
      System.out.println(uniquesList);
      for (DS.Node n : uniquesList.complexVal) {
        if (n instanceof DS.IdNode) {
          uniqueData.add(((DS.IdNode) n).name);
        } else if (n instanceof DS.UniqueNode) {
          uniqueData.add(((DS.UniqueNode) n).data);
        } else {
          throw new DS.MapNode.NonDeserializableException();
        }
      }
    } catch (DS.MapNode.NonDeserializableException d) {
      System.out.println("Map to serialize sprite from is nonfunctional!");
    }

  }

  public Sprite(DS.Node node) throws LoadingException {
    load(node);
  }

  // Custom equals method
  // Just checks if all parameters are equal
  public boolean equals(Sprite toCompare) {
    if ((this.location == null && toCompare.location == null || this.location.equals(toCompare.location))
        && this.name.equals(toCompare.name) && this.type == toCompare.type
        && this.uniqueData.equals(toCompare.uniqueData)) {
      return true;
    } else {
      return false;
    }
  }
}
