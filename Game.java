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
import java.util.concurrent.*;
import java.time.Instant;
import java.time.Duration;

/*
 * Thomas: This is a singleton class representing the entire game state.
 * It has static fields representing the Swing components used for the UI.
 */
public class Game {
  // Class constants

  // Width/height of text area for game display (in "tiles," the smallest units
  // the player can visibly move)
  // (Tiles are 2x2 chars for now. See render() for implementation)
  public static final int WIDTH = 10;
  public static final int HEIGHT = 12;

  // Speed of player
  public static final double SPEED = 0.015;

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
  }

  // Base class for game-specific events
  public static abstract class Event {
    public static enum Direction {
      UP, DOWN, LEFT, RIGHT
    };
  }

  // Classes representing specific game events
  public static class TouchEvent extends Event {
  }

  public static class InteractEvent extends Event {
    public final Direction fromDirection;

    public InteractEvent(Direction fromDirection) {
      this.fromDirection = fromDirection;
    }
  }

  public static class LeaveSquareEvent extends Event {
    public final Direction toDirection;

    public LeaveSquareEvent(Direction toDirection) {
      this.toDirection = toDirection;
    }
  }

  public static void goToChamber(Chamber goTo, Point dropAt) {

  }

  // Instance of KeyBox to represent the game state
  public static KeyBox box;

  // Components of the GUI
  public static JTextArea textArea;

  // Position of the player
  public static Point2D.Double pos;

  // Game display state
  private static String displayState;

  // Initialize game state
  public static void init() {
    pos = new Point2D.Double(2.0, 2.0);

    textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));

    render();
    textArea.setText(displayState);

    box = new KeyBox();

    box.frame.add(textArea);
    box.frame.pack();
    box.frame.setVisible(true);
  }

  public static void update(double delta) {
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

    boolean goingUp = box.getResetKey(KeyEvent.VK_UP),
        goingDown = box.getResetKey(KeyEvent.VK_DOWN),
        goingLeft = box.getResetKey(KeyEvent.VK_LEFT),
        goingRight = box.getResetKey(KeyEvent.VK_RIGHT);

    if (goingUp && goingLeft && !goingDown && !goingRight) { // Up and down would cancel each other out, as would left
                                                             // and right
      pos.x = Math.max(0.0, pos.x - diagInterval);
      pos.y = Math.max(0.0, pos.y - diagInterval);
    } else if (goingDown && goingLeft && !goingUp && !goingRight) {
      pos.x = Math.max(0.0, pos.x - diagInterval);
      pos.y = Math.min(farBottom, pos.y + diagInterval);
    } else if (goingUp && goingRight && !goingDown && !goingLeft) {
      pos.x = Math.min(farRight, pos.x + diagInterval);
      pos.y = Math.max(0.0, pos.y - diagInterval);
    } else if (goingDown && goingRight && !goingUp && !goingLeft) {
      pos.x = Math.min(farRight, pos.x + diagInterval);
      pos.y = Math.min(farBottom, pos.y + diagInterval);
    } else if (goingUp && !goingDown) { // Now that we've dealt with all possible diagonals, we can deal with the normal
                                        // directions.
      pos.y = Math.max(0.0, pos.y - currentSpeed);
    } else if (goingDown && !goingUp) {
      pos.y = Math.min(farBottom, pos.y + currentSpeed);
    } else if (goingLeft && !goingRight) {
      pos.x = Math.max(0.0, pos.x - currentSpeed);
    } else if (goingRight && !goingLeft) {
      pos.x = Math.min(farRight, pos.x + currentSpeed);
    }
  }

  // Method to rewrite displayState after each update.
  public static void render() {
    displayState = "  ";
    for (int i = 0; i < WIDTH; i++) {
      displayState += (char) ('A' + i);
      displayState += " ";
    }
    displayState += "\n\n";

    int trunc_x = (int) pos.x, trunc_y = (int) pos.y; // x and y are truncated so we can map them onto the grid.
    for (int i = 0; i < HEIGHT; i++) { // Vertical cursor coordinate (y)
      displayState += (char) ('A' + i);
      displayState += " ";
      for (int j = 0; j < WIDTH; j++) { // Horizontal cursor coordinate (x)
        if (i == trunc_y && j == trunc_x) {
          displayState += "@";
        } else { // Stars everywhere else
          displayState += "*";
        }

        displayState += " ";
      }

      displayState += "\n\n";
    }
  }

  // Method to call update and render repeatedly until the program exits.
  public static void run() {
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

  public static void main(String args[]) { // program entry point
    init();
    run();
  }
}
