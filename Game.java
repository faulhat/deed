
/*
 * Thomas: Main driver class that handles game state and runs the game
 * License: free as in freedom
 * I like elephants and God likes elephants
 */
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.naming.OperationNotSupportedException;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.Instant;
import java.time.Duration;
import java.io.*;

/*
 * Thomas: This is a singleton class representing the entire game state.
 * It has static fields representing the Swing components used for the UI.
 */
public class Game {
  // Class constants
  // Width/height of text area for game display (in "tiles," the smallest units
  // the player can visibly move)
  // (Tiles are 2x2 chars for now. See render() for implementation)
  public static final int WIDTH = 40;
  public static final int HEIGHT = 25;
  public static final int DIALOGUE_HEIGHT = 4;
  public static final int DIALOGUE_WIDTH = WIDTH - 2;

  // Speed of player
  public static final double SPEED = 0.015;

  //boolean representing whether events should be generated
  public boolean eventsOn;

  // Thomas: This is a class representing a handler for key presses.
  public static class KeyBox {
    /*
     * Thomas: these two hashmaps represent the input state regarding key presses.
     * They must be atomic because they will be accessed by the main thread on each
     * update. For atomicity, we use the ConcurrentHashMap template.
     */
    // flags to be set when a key is pressed, processed when the frame is updated,
    // and then reset
    // Basically, "was this key ever pressed between the last reprinting and now?"
    public ConcurrentHashMap<Integer, Boolean> wasPressed;
    // flags to be set when a key is pressed and reset only when the key is released
    public ConcurrentHashMap<Integer, Boolean> isPressed;

    // A subclass of KeyListener that updates the KeyBox state when keys are
    // pressed.
    // Note that it is not static, as it is bound to the KeyBox instance it's a part
    // of.
    public class MyFrame extends JFrame implements KeyListener {
      public MyFrame() {
        super();
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
      }

      @Override
      public void keyTyped(KeyEvent e) {
      }

      @Override
      public void keyPressed(KeyEvent e) {
        KeyBox.this.wasPressed.put(e.getKeyCode(), true);
        KeyBox.this.isPressed.put(e.getKeyCode(), true);
      }

      @Override
      public void keyReleased(KeyEvent e) {
        KeyBox.this.isPressed.put(e.getKeyCode(), false);
      }
    }

    public JFrame frame;

    public KeyBox() {
      wasPressed = new ConcurrentHashMap<>();
      isPressed = new ConcurrentHashMap<>();
      frame = new MyFrame();
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setLayout(new FlowLayout());
    }

    // See if key has been pressed within the last update.
    // If it has and is not still being held down, reset the flag to false.
    public boolean getResetKey(int keyCode) {
      if (wasPressed.containsKey(keyCode) && wasPressed.get(keyCode)) {
        if (!(isPressed.containsKey(keyCode) && isPressed.get(keyCode))) {
          // Only reset wasPressed flag to false if the key is not still being held down.
          wasPressed.put(keyCode, false);
        }
        return true;
      }
      return false;
    }

    // Reset the flag even if it's being held down still.
    public boolean getReleaseKey(int keyCode) {
      if (wasPressed.containsKey(keyCode) && wasPressed.get(keyCode)) {
        wasPressed.put(keyCode, false);
        return true;
      }

      return false;
    }
  }

  public static enum EventType {
    IntersectEvent,
    InteractEvent,
    TouchEvent,
    LeaveSquareEvent
  }

  public static enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    LEFT_UP,
    LEFT_DOWN,
    RIGHT_UP,
    RIGHT_DOWN
  }

  public static enum SpriteType {
    Dialogue_Point,
    Exit
  }

  public static class Event {
    public final EventType eventType;

    public final Direction direction;

    public Event(EventType eventType, Direction direction) {
      this.eventType = eventType;
      this.direction = direction;
    }
  }

  public void goToChamber(Chamber goTo, Direction direction) {
    Point toConvert = goTo.directionDropGetter.get(direction);
    pos = new Point2D.Double(toConvert.getX(), toConvert.getY());
    currentChamber = goTo;
  }

  public Template initDialoguePoint() {

    Template.Initializer init = (uniqueData, handlerMap) -> {
      Sprite iniSprite = new Sprite((String) uniqueData.remove(0), 'd');
      iniSprite.handlerMap = handlerMap;
      iniSprite.uniqueData = uniqueData;
      iniSprite.type = SpriteType.Dialogue_Point;
      return iniSprite;
    };

    Template.Handler t = (TouchEvent, dialoguePoint) -> dialogueIn
        .put((String) dialoguePoint.uniqueData.get(0)),
        i = (IntersectEvent, dialoguePoint) -> {
        },
        i_a = (InteractEvent, dialoguePoint) -> {
        },
        l = (LeaveSquareEvent, dialoguePoint) -> {
        };

    Template.HandlerMap handlerMap = new Template.HandlerMap();

    handlerMap.put(Game.EventType.TouchEvent, t);
    handlerMap.put(Game.EventType.IntersectEvent, i);
    handlerMap.put(Game.EventType.InteractEvent, i_a);
    handlerMap.put(Game.EventType.LeaveSquareEvent, l);
    Template dialoguePoint = new Template("dialoguePoint", 'd', init, handlerMap);
    return dialoguePoint;
  }

  public Template initExit() {
    Template.Initializer init = (uniqueData, handlerMap) -> {
      Sprite iniSprite = new Sprite((String) uniqueData.remove(0), 'e');
      iniSprite.handlerMap = handlerMap;
      iniSprite.uniqueData = uniqueData;
      iniSprite.type = SpriteType.Exit;
      return iniSprite;
    };
    Template.Handler touchHandler = (TouchEvent, exit) -> {
    },
        intersectHandler = (IntersectEvent, exit) -> {this.goToChamber(((Chamber)exit.uniqueData.get(0)), IntersectEvent.direction);},
        interactHandler = (InteractEvent, exit) -> {},
        leaveHandler = (LeaveSquareEvent, exit) -> {this.goToChamber(((Chamber)exit.uniqueData.get(0)), LeaveSquareEvent.direction);};
        
    Template.HandlerMap handlerMap = new Template.HandlerMap();
    handlerMap.put(EventType.TouchEvent, touchHandler);
    handlerMap.put(EventType.IntersectEvent, intersectHandler);
    handlerMap.put(EventType.InteractEvent, interactHandler);
    handlerMap.put(EventType.LeaveSquareEvent, leaveHandler);
    Template exit = new Template("exit", 'e', init, handlerMap);
    return exit;
  }

  public HashMap<SpriteType, Template> genBindings() {
    HashMap<SpriteType, Template> spriteTypeBindings = new HashMap<SpriteType, Template>();
    spriteTypeBindings.put(SpriteType.Dialogue_Point, initDialoguePoint());
    return spriteTypeBindings;
  }

  // chamber which the player is currently in
  public Chamber currentChamber = new Chamber();
  // DialoguePoint template
  public Template DialoguePoint = initDialoguePoint();
  // Map of spriteType to template
  public HashMap<SpriteType, Template> spriteTypeBindings = genBindings();

  public BlockingQueue<String> dialogueIn;
  // Instance of KeyBox to represent the game state
  public KeyBox box;
  // Components of the GUI
  public JTextArea textArea;
  // Position of the player
  public Point2D.Double pos;
  // Game display state
  private String displayState;
  // Direction in which the player is currently going
  public static Direction playerDirection;

  // Initialize game state
  public Game() throws OperationNotSupportedException, FileNotFoundException, InterruptedException, IOException, 
  DS.ParserException, DS.Storable.LoadingException{
    eventsOn = true;
    
    DS.Root loadChamber = DS.load(new FileReader("tripleTest.txt"));
    DS.VectorNode nodeToUse = ((DS.VectorNode)loadChamber.complexVal.get(0));
    Chamber chamberToUse = new Chamber(nodeToUse);
    System.out.println(chamberToUse.matrix[0][1]);
    pos = new Point2D.Double(2.0, 2.0);
    dialogueIn = new LinkedBlockingQueue<String>();
    textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setFocusable(false);
    textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
    render();
    textArea.setText(displayState);
    box = new KeyBox();
    box.frame.add(textArea);
    box.frame.pack();
    box.frame.setVisible(true);
  }

  public void update(double delta) {
    // Multiply the time delta between now and the last update by the SPEED constant
    // to calculate the offset
    // we should move the player by on this update (if he moves).
    double currentSpeed = delta * SPEED;
    double diagInterval = Math.sqrt(Math.pow(currentSpeed, 2.0) / 2); // How far to move in both directions when we move
                                                                      // diagonally.
    // If we use WIDTH and HEIGHT as our bounds, we can end up a tile off the
    // display to the right or bottom.
    double farRight = (double) WIDTH - 1.0;
    double farBottom = (double) HEIGHT - 1.0;
    Point2D.Double prevPos = new Point2D.Double(pos.x, pos.y);
    boolean goingUp = box.getResetKey(KeyEvent.VK_UP),
        goingDown = box.getResetKey(KeyEvent.VK_DOWN),
        goingLeft = box.getResetKey(KeyEvent.VK_LEFT),
        goingRight = box.getResetKey(KeyEvent.VK_RIGHT);
    if (goingUp && goingLeft && !goingDown && !goingRight) { // Up and down would cancel each other out, as would left
      playerDirection = Direction.LEFT_UP; // and right
      pos.x = Math.max(0.0, pos.x - diagInterval);
      pos.y = Math.max(0.0, pos.y - diagInterval);

    } else if (goingDown && goingLeft && !goingUp && !goingRight) {
      playerDirection = Direction.LEFT_DOWN;
      pos.x = Math.max(0.0, pos.x - diagInterval);
      pos.y = Math.min(farBottom, pos.y + diagInterval);

    } else if (goingUp && goingRight && !goingDown && !goingLeft) {
      playerDirection = Direction.RIGHT_UP;
      pos.x = Math.min(farRight, pos.x + diagInterval);
      pos.y = Math.max(0.0, pos.y - diagInterval);
    } else if (goingDown && goingRight && !goingUp && !goingLeft) {
      playerDirection = Direction.RIGHT_DOWN;
      pos.x = Math.min(farRight, pos.x + diagInterval);
      pos.y = Math.min(farBottom, pos.y + diagInterval);
    } else if (goingUp && !goingDown) { // Now that we've dealt with all possible diagonals, we can deal with the normal
                                        // directions.
      playerDirection = Direction.UP;
      pos.y = Math.max(0.0, pos.y - currentSpeed);

    } else if (goingDown && !goingUp) {
      playerDirection = Direction.UP;
      pos.y = Math.min(farBottom, pos.y + currentSpeed);

    } else if (goingLeft && !goingRight) {
      playerDirection = Direction.LEFT;
      pos.x = Math.max(0.0, pos.x - currentSpeed);
    } else if (goingRight && !goingLeft) {
      playerDirection = Direction.RIGHT;
      pos.x = Math.min(farRight, pos.x + currentSpeed);
    }
    if ((int) prevPos.x != (int) pos.x || (int) prevPos.y != (int) pos.y) {
      if (currentChamber.matrix[(int) pos.y][(int) pos.x].isWall == true) {
        pos = prevPos;
      }
    }

  }

  // Method to rewrite displayState after each update.
  public void render() throws OperationNotSupportedException, InterruptedException {

    displayState = "";
    int trunc_x = (int) pos.x, trunc_y = (int) pos.y; // x and y are truncated so we can map them onto the grid.
    for (int i = 0; i < HEIGHT; i++) { // Vertical cursor coordinate (y)
      for (int j = 0; j < WIDTH; j++) { // Horizontal cursor coordinate (x)
        System.out.println(i + " " + j);
        ArrayList<Sprite> s = new ArrayList<Sprite>(currentChamber.matrix[i][j].sprites);
        if (i == trunc_y && j == trunc_x) {
          displayState += "@";
        } else if (currentChamber.matrix[i][j].isWall) {
          displayState += "|";
        } else if (currentChamber.matrix[i][j].sprites.size() == 0) {
          displayState += " ";
        } else {
          if (s.get(0) != null) {
            if (s.get(0).visible == true) {
              displayState += currentChamber.matrix[i][j].sprites.get(0).symbol;
            } else {
              displayState += " ";
            }
          } else {
            displayState += " ";
          }
        }
        displayState += " ";
      }
      displayState += "\n\n";
    }
  }

  // Method to call update and render repeatedly until the program exits.
  public void run() throws OperationNotSupportedException, InterruptedException {
    Instant then = Instant.now();
    while (true) {
      Instant now = Instant.now();
      update((double) Duration.between(then, now).toNanos() / 1e6); // Provide the time delta for update based on the
                                                                    // time since the last iteration.
      render();
      textArea.setText(displayState); // Write displayState to the actual display
      then = now;
    }
  }

  public static void main(String args[]) throws Exception { // program entry point
    new Game().run();
  }
}