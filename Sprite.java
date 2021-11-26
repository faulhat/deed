import javax.naming.OperationNotSupportedException;

public abstract class Sprite {
  public final boolean visible;

  public Sprite(boolean visible) {
    this.visible = visible;
  }

  public abstract String getName();

  public abstract char getSymbol() throws OperationNotSupportedException;

  public abstract void onEvent(Game.Event e);
}