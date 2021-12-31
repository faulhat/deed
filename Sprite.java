import javax.naming.OperationNotSupportedException;

public abstract class Sprite implements DS.Storable {
  public boolean visible;
  
  public abstract String getName();

  public abstract char getSymbol() throws OperationNotSupportedException;

  public abstract void onEvent(Game.Event e);
  
  public abstract String getID();
}