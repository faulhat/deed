/*
 * Thomas: Main driver class that handles game state and runs the game
 * License: free as in freedom
 * I like elephants and God likes elephants
 */

import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
import java.awt.Font;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/*
 * Thomas: This is a singleton class representing the entire game state.
 * It has static fields representing the Swing components used for the UI.
 */
public class Game {
  // Width/height of text area for game display (in chars)
  public static final int BOXWIDTH = 10;
  public static final int BOXHEIGHT = 20;

  // Framerate of game (ms)
  public static final int UPDATE_INTERVAL = 100;

  // Enumeration of game-specific events to be handled by game objects
  public static enum Event {
    PlayerTouch,
    PlayerInteract
  }

  // Components of the GUI
  public static JFrame frame;

  public static JTextArea textArea;

  // Position of the player
  public static Point pos;

  // Game display state
  private static String displayState;

  // Initialize game state
  public static void init() {
    pos = new Point(0, 0);

    textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));

    render();
    textArea.setText(displayState);

    frame = new JFrame();
    frame.add(textArea);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new FlowLayout());
    frame.pack();
    frame.setVisible(true);
  }

  public static void render() {
    displayState = "  ";
    for (int i = 0; i < BOXWIDTH; i++) {
      displayState += (char)('A' + i);
      displayState += " ";
    }
    displayState += "\n\n";

    int ctr = 2 * (pos.y * BOXWIDTH + pos.x); // Cursor must count by twos starting from (pos.x, pos.y) going left-to-right and top-to-bottom.
    for (int i = 0; i < BOXHEIGHT; i++) { // Vertical cursor coordinate (y)
      displayState += (char)('A'+ i);
      displayState += " ";
      for (int j = 0; j < BOXWIDTH; j++) { // Horizontal cursor coordinate (x)
        if (ctr == BOXHEIGHT * BOXWIDTH * 2 - 2) { // Include a closed bracket two characters (one space) after pos
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
        ctr = (ctr - 2 + BOXHEIGHT * BOXWIDTH * 2) % (BOXHEIGHT * BOXWIDTH * 2); // Counter increments by two, circling back around after the bottom-right-hand corner.
      }

      displayState += "\n\n";
    }
  }

  // TimerTask for updating the game state at a fixed time step
  public static class Updater extends TimerTask {
    public void run() {
      textArea.setText(displayState);

      frame.repaint();

      if (++pos.x >= BOXWIDTH) {
        pos.x = 0;
        pos.y = (pos.y + 1) % BOXHEIGHT;
      }
      
      render();
    }
  }

  public static void run() {
    new Timer().scheduleAtFixedRate(new Updater(), 0, UPDATE_INTERVAL); // display reprinted at a fixed rate defined by the constant UPDATE_INTERVAL
  }
  
  public static void main(String args[]) { // program entry point
    init();
    run();
  }
}
