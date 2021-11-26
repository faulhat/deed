import javax.naming.OperationNotSupportedException;
import java.awt.Point;

/*
 * Thomas: Here's a subclass of Sprite that deals with the invisible exits in Chambers.
 * Basically, when you leave a room, you will trigger an event on one of these Exits.
 * That way, the game will know which room to send you to next and where to drop you in that room!
 */
public class Exit extends Sprite {
    public final Game.Event.Direction direction; // What direction does the player have to go to use this Exit?

    public final Chamber goesTo; // What Chamber does this Exit lead to?

    public final Point nextSquare; // What square do we appear on in the Chamber we're going to?

    public Exit(Game.Event.Direction direction, Chamber goesTo, Point nextSquare) {
        super(false);
        this.direction = direction;
        this.goesTo = goesTo;
        this.nextSquare = nextSquare;
    }

    @Override
    public String getName() {
        return "EXIT";
    }

    @Override
    public char getSymbol() throws OperationNotSupportedException {
        throw new OperationNotSupportedException("Sprite of class Exit is not visible, and therefore does not have a symbol.");
    }

    public void onEvent(Game.Event e) {
        if (e instanceof Game.LeaveSquareEvent) {
            if (((Game.LeaveSquareEvent) e).toDirection == direction) {
                Game.goToChamber(goesTo, nextSquare);
            }
        }
    }
}
