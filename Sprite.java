import java.util.Map;

import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
public class Sprite implements DS.Storable {
  public boolean visible;
  
  public Game.SpriteType type;

  public Template.HandlerMap handlerMap;
  
  public String name;

  public char symbol;
  //String representing name of location of sprite
  public String location;
  
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
  public Sprite(String name, char symbol, Game.SpriteType type, String location){
    this(name,symbol);
    this.type = type;
    this.location = location;
  }

  public void onEvent(Game.Event e) throws InterruptedException {
    Template.Handler handler = handlerMap.get(e.eventType);
    
    if (handler != null) {
      handler.accept(e, this);
    }
    else{
      System.out.println("Handler is null");
    }
  }
  //Dummy methods for testing
  public DS.MapNode dump(){
    DS.MapNode mapOfSprite = new DS.MapNode();
    String isVis = "";
    if (this.visible = true){
      isVis = "true";
    }
    else{
      isVis = "false";
    }
    //Encoding all fields as nodes to be written to files
    DS.Node vis_key = new DS.KeywordNode(":visible"),
        visible = new DS.IdNode(isVis),
        type_key = new DS.KeywordNode(":spriteType"),
        type = new DS.IdNode("DialoguePoint"),
        name_key = new DS.KeywordNode(":name"),
        name = new DS.IdNode(this.name),
        location_key = new DS.KeywordNode(":location"),
        location = new DS.IdNode(this.location),
        uniqueDataKey = new DS.KeywordNode(":uniqueData");
    DS.ComplexNode uniqueData = new DS.ListNode();
          
        
    for (int i = 0; i < this.uniqueData.size(); i++){
      System.out.println(this.uniqueData.get(i));
      DS.SimpleNode uniqueDataElement = null;
      if (this.uniqueData.get(i) instanceof String){
        uniqueDataElement = new DS.IdNode((String)this.uniqueData.get(i));
      }
      else{
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
  public void load(DS.Node node) throws LoadingException{
    DS.MapNode mNode = (DS.MapNode)node;
    try{
      Map<String, DS.Node> map = mNode.getMap();
      DS.Node valNode = map.get(":name");

      if (!(valNode instanceof DS.IdNode)) {
        throw new LoadingException("Sprite", "Value for name field is not an IdNode!");
      }

      name = ((DS.IdNode) valNode).name;

      DS.IdNode isVisible = (DS.IdNode)map.get(":visible");
      if (isVisible.isBool()){  
        visible = isVisible.isTrue();
      }
      else{
        throw new LoadingException("Sprite", "IdNode not true/false resolvable");
      }
      DS.IdNode spriteTypeNode = (DS.IdNode)map.get(":type");
      String spriteType = spriteTypeNode.name;
      for (Game.SpriteType s : Game.SpriteType.values()){
        if (spriteType.equals(s.toString())){
          type = s;
        }
      }
      this.symbol = Game.spriteTypeBindings.get(type).symbol;
      DS.ListNode uniquesList = (DS.ListNode)map.get(":unique");
      for (DS.Node n : uniquesList.complexVal){
        uniqueData.add(((DS.UniqueNode)n).data);
      }
    }
    catch(DS.MapNode.NonDeserializableException d){
      System.out.println("Map to serialize sprite from is nonfunctional!");
    }
    

  }

  public Sprite(DS.Node node) throws LoadingException {
    load(node);
  }
}
