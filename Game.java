/*
 * Thomas: Main driver class that handles game state and runs the game
 * License: free as in freedom
 * I like elephants and God likes elephants
 */

import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import java.util.concurrent.*;
import java.awt.geom.Point2D;
import java.time.Instant;
import java.time.Duration;

/*
 * Thomas: This is a singleton class representing the entire game state.
 * It has static fields representing the Swing components used for the UI.
 */
public class Game {
  // Class constants

  // Width/height of text area for game display (in "tiles," the smallest units the player can visibly move)
  // (Tiles are 2x2 chars for now. See render() for implementation)
  public static final int WIDTH = 10;
  public static final int HEIGHT = 20;

  // Speed of player
  public static final double SPEED = 0.015;

  // Thomas: This is a class representing a handler for key presses.
  public static class KeyBox {
    /*
     * Thomas: these two hashmaps represent the input state regarding key presses.
     * They must be atomic because they will be accessed by the main thread on each update.
     * For atomicity, we use the ConcurrentHashMap template.
     */
    
    // flags to be set when a key is pressed, processed when the frame is updated, and then reset
    // Basically, "was this key ever pressed between the last reprinting and now?"
    public ConcurrentHashMap<Integer, Boolean> wasPressed;
    
    // flags to be set when a key is pressed and reset only when the key is released
    public ConcurrentHashMap<Integer, Boolean> isPressed;
  
    // A subclass of KeyListener that updates the KeyBox state when keys are pressed.
    // Note that it is not static, as it is bound to the KeyBox instance it's a part of.
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

  // Enumeration of game-specific events to be handled by game objects
  public static enum Event {
    PlayerTouch,
    PlayerInteract
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
    pos = new Point2D.Double(1.0, 0.0);

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
    // Multiply the time delta between now and the last update by the SPEED constant to calculate the offset
    // we should move the player by on this update (if he moves).
    double currentSpeed = delta * SPEED;

    if (box.getResetKey(KeyEvent.VK_UP)) {
      pos.y = Math.max(0.0, pos.y - currentSpeed);
    }
    
    if (box.getResetKey(KeyEvent.VK_DOWN)) {
      pos.y = Math.min((double) HEIGHT - 1.0, pos.y + currentSpeed);
    }
    
    if (box.getResetKey(KeyEvent.VK_LEFT)) {
      pos.x = Math.max(1.0, pos.x - currentSpeed);
    }
    
    if (box.getResetKey(KeyEvent.VK_RIGHT)) {
      pos.x = Math.min((double) WIDTH - 2.0, pos.x + currentSpeed);
    }
  }

  // Method to rewrite displayState after each update.
  public static void render() {
    displayState = "  ";
    for (int i = 0; i < WIDTH; i++) {
      displayState += (char)('A' + i);
      displayState += " ";
    }
    displayState += "\n\n";

    int ctr = 2 * ((int) pos.y * WIDTH + (int) pos.x); // Cursor must count by twos starting from (x, y) going left-to-right and top-to-bottom. x and y are truncated so we can map them onto the grid.
    for (int i = 0; i < HEIGHT; i++) { // Vertical cursor coordinate (y)
      displayState += (char)('A'+ i);
      displayState += " ";
      for (int j = 0; j < WIDTH; j++) { // Horizontal cursor coordinate (x)
        if (ctr == HEIGHT * WIDTH * 2 - 2) { // Include a closed bracket two characters (one space) after pos
          displayState += "]";
        }
        else if (ctr == 0) { // Include an at-sign at the current pos
          displayState += "@";
        }
        else if (ctr == 2) { // Include an open bracket two characters (one space) before pos
          displayState += "[";
        }
        else if (ctr % 2 == 0) { // Stars everywhere else
          displayState += "*";
        }
        else {
          displayState += " "; // Empty space to make everything look nicer.
        }
        displayState += " ";
        ctr = (ctr - 2 + HEIGHT * WIDTH * 2) % (HEIGHT * WIDTH * 2); // Counter increments by two, circling back around after the bottom-right-hand corner.
      }

      displayState += "\n\n";
    }
  }

  // Method to call update and render repeatedly until the program exits.
  public static void run() {
    Instant then = Instant.now();
    while (true) {
      Instant now = Instant.now();
      update((double) Duration.between(then, now).toNanos() / 1e6); // Provide the time delta for update based on the time since the last iteration.
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
