import java.util.Map;
import java.util.ArrayList;
public class Sprite implements DS.Storable {
  public boolean visible;
  
  public Template.HandlerMap handlerMap;
  
  public final String name;

  public final Character symbol;
  
  public Object destination;
  
  public ArrayList<Object> uniqueData;

  public Sprite(String name) {
    this.visible = false;
    this.name = name;
    this.symbol = null;
  }

  public Sprite(String name, Character symbol) {
    this.visible = true;
    this.name = name;
    this.symbol = symbol;
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
  public DS.Node dump(){
    DS.Node n = new DS.IntNode();
    return n;
  }
  public void load(DS.Node n){
  
  }
}
