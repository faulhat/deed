public class Sprite implements DS.Storable {
  public boolean visible;
  
  public Template.HandlerMap handlerMap;
  
  public final String name;

  public final Character symbol;

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

  public void onEvent(Game.Event e){
    Template.Handler handler = handlerMap.get(e.eventType);

    handler.accept(e, this);
  }
}
