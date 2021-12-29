import javax.naming.OperationNotSupportedException;
import java.io.*;

public abstract class Sprite {
  public boolean visible;
 
  
  public abstract String getName();

  public abstract char getSymbol() throws OperationNotSupportedException;

  public abstract void onEvent(Game.Event e);
  
  public abstract String getID();
}