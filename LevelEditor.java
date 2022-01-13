import java.awt.geom.Point2D;
import java.awt.Point;
import java.awt.Font;
import java.awt.event.*;
import javax.swing.JTextArea;
import javax.swing.JMenuBar;
import javax.naming.OperationNotSupportedException;
import java.util.concurrent.*;
import java.lang.InterruptedException;
import java.util.ArrayList;
import java.time.Instant;
import java.util.Arrays;
import java.time.Duration;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;

public class LevelEditor {

  public static final int WIDTH = 40;
  public static final int HEIGHT = 25;
  public static final int DIALOGUE_HEIGHT = 4;
  public static final int DIALOGUE_WIDTH = WIDTH - 2;
  public static final double SPEED = 0.010;
  public static final String baseTextMenu = "DIALOGUE\n";
  // String[][] representing submenu at each index
  public static final String[][] expandedTextMenu = { { "Dialogue_Point" } };
  // Game display state

  //

  // Components of the GUI
  public static JTextArea textArea;
  public static JTextArea textSpriteMenu;
  public static JMenuBar menuBar;

  // Singleton class representing the state of the insertion menu
  public static class MenuState {
    // Class representing an item on the menu, which can have sub-items.
    public static class MenuItem {
      // Does this menu item have sub-items?
      public final boolean isLeaf;

      public final String name;

      public ArrayList<MenuItem> subItems;

      public boolean expanded;

      // Contents represented by this item.
      public Sprite contents;

      public MenuItem(String name) {
        this.isLeaf = false;
        this.name = name;
        this.subItems = new ArrayList<>();
        this.contents = null;
        this.expanded = false;
      }

      public MenuItem(String name, Sprite contents) {
        this.isLeaf = true;
        this.name = name;
        this.subItems = null;
        this.contents = contents;
        this.expanded = false;
      }

      // Constant will be passed in to the fourth parameter:
      // n_lines will always be 8
      public String show(int[] lineno, int cursor, int depth) {
        String text = "";

        if (lineno[0] == cursor) {
          text += "- >";
        } else {
          text += "-  ";
        }

        if (!isLeaf) {
          if (expanded) {
            text += ">";
          } else {
            text += "^";
          }
        } else {
          text += "  ";
        }

        for (int i = 0; i < depth; i++) {
          text += ' ';
        }
        text += name + '\n';
        lineno[0]++;

        if (!isLeaf && expanded) {
          for (MenuItem item : items) {
            text += item.show(lineno, cursor, depth + 1);
          }
        }

        return text;
      }

      public int getCount() {
        if (isLeaf) {
          return 1;
        }

        int n = 1;
        for (MenuItem item : subItems) {
          n += item.getCount();
        }

        return n;
      }
    }

    // Which line is selected?
    public static int cursor;

    public static ArrayList<MenuItem> items;

    public static final int N_LINES = 2;

    public static void init(ArrayList<MenuItem> initItems) {
      items = initItems;
      cursor = 0;
    }

    public static int getCount() {
      int n = 0;
      for (MenuItem item : items) {
        n += item.getCount();
      }

      return n;
    }

    public static void moveCursor(int offset) {
      cursor = Math.max(0, Math.min(offset, getCount()));
    }

    public static String render() {
      String text = "";

      for (int i = 0; i < items.size(); i++) {
        text += items.get(i).show(new int[] { i }, cursor, 0);
      }

      String[] lines = text.split("\n");

      int last_line = Math.min(cursor + N_LINES, lines.length);

      return String.join("\n", Arrays.asList(Arrays.copyOfRange(lines, Math.max(last_line - N_LINES, 0), last_line)));
    }
  }

  // Position of the cursor in the level being edited
  public static Point2D.Double pos;
  //Direction in which the player is going
  public static Game.Direction direction;
  // boolean representing whether the player is in the menu or not
  public static boolean isInMenu;
  // String representing level editor state
  private static String displayState;
  // Dialogue Queue
  public static LinkedBlockingQueue<String> dialogueIn;
  // Matrix of chars to store insertion for levels
  public static Chamber c = new Chamber();

  public static Game.KeyBox box;
  
  public static void goToChamber(Chamber goTo, Game.Direction direction) {
    c = goTo;
    pos.x = goTo.directionDropGetter.get(direction).getX();
    pos.y = goTo.directionDropGetter.get(direction).getY();
    
  }
  // Initialize game state
  public static void init() throws OperationNotSupportedException, InterruptedException {
    ArrayList<MenuState.MenuItem> ex = new ArrayList<>();
    box = new Game.KeyBox();
    dialogueIn = new LinkedBlockingQueue<String>();
    direction = Game.Direction.UP;
    
    ArrayList<Object> m = new ArrayList<>();
    m.add("DialoguePointEx");
    m.add("Example");
    ArrayList<Object> exitExample = new ArrayList<>();
    exitExample.add("Exit");
    exitExample.add(c);
    System.out.println(exitExample);
    System.out.println(c);

    Template dialoguePoint = Game.initDialoguePoint();
    Template exit = Game.initExit();
    Sprite toInsert = dialoguePoint.genSprite(m);
    Sprite exitSprite = exit.genSprite(exitExample);
    System.out.println(exitSprite.uniqueData);
    ex.add(new MenuState.MenuItem("DialoguePoint", toInsert));
    ex.add(new MenuState.MenuItem("Exit", exitSprite));

    MenuState.init(ex);
    pos = new Point2D.Double(2.0, 2.0);

    textSpriteMenu = new JTextArea();
    textSpriteMenu.setText(baseTextMenu);
    textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setFocusable(false);
    textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
    render();
    textArea.setText(displayState);

    menuBar = new JMenuBar();
    // Set menu bar properties

    box.frame.setJMenuBar(menuBar);
    box.frame.add(textArea);
    box.frame.pack();
    box.frame.setVisible(true);
  }

  public static void update(double delta) throws InterruptedException {
    // Multiply the time delta between now and the last update by the SPEED constant
    // to calculate the offset
    // we should move the player by on this update (if he moves).
    double currentSpeed = delta * SPEED;
    // If we use WIDTH and HEIGHT as our bounds, we can end up a tile off the
    // display to the right or bottom.
    double farRight = (double) WIDTH - 1.0;
    double farBottom = (double) HEIGHT - 1.0;

    boolean goingUp = box.getResetKey(KeyEvent.VK_UP),
        goingDown = box.getResetKey(KeyEvent.VK_DOWN),
        goingLeft = box.getResetKey(KeyEvent.VK_LEFT),
        goingRight = box.getResetKey(KeyEvent.VK_RIGHT),
        terrainChangeVerticalWall = box.getResetKey(KeyEvent.VK_V),
        terrainChangeHorizontalWall = box.getResetKey(KeyEvent.VK_H),
        terrainChangeClear = box.getResetKey(KeyEvent.VK_C),
        openInsertMenu = box.getResetKey(KeyEvent.VK_I),
        closeInsertMenu = box.getResetKey(KeyEvent.VK_E),
        Ctrl = box.getResetKey(KeyEvent.VK_CONTROL),
        select = box.getResetKey(KeyEvent.VK_S),
        delete = box.getResetKey(KeyEvent.VK_BACK_SPACE),
        reset = box.getResetKey(KeyEvent.VK_R),
        checkPos = box.getResetKey(KeyEvent.VK_P);
    for (int i = (int) Math.max(pos.y - 1, 0.0); i <= (int) Math.min(farRight, pos.y + 1.0); i++) {
      for (int j = (int) Math.max(pos.x - 1.0, 0.0); j <= (int) Math.min(farBottom, pos.x + 1.0); j++) {
        Chamber.Square s = c.matrix[i][j];
        s.eventOn(new Game.Event(Game.EventType.TouchEvent, Game.Direction.UP));
      }
    }
    c.eventAtPos(pos, new Game.Event(Game.EventType.IntersectEvent, direction));
 
    if (openInsertMenu) {
      isInMenu = true;
    } else if (closeInsertMenu) {
      isInMenu = false;
    }
    if (isInMenu) {
      if (goingUp && !goingDown) {
        MenuState.moveCursor(-1);
      } else if (goingDown && !goingUp) {
        MenuState.moveCursor(1);
      } else if (select) {
        c.matrix[(int) pos.y][(int) pos.x].sprites.add(MenuState.items.get(MenuState.cursor).contents);
        isInMenu = false;
      }
    } else {

      if (goingUp && !goingDown) { // Now that we've dealt with all possible diagonals, we can deal with the normal
                                   // directions.
        pos.y = Math.max(1.0, pos.y - currentSpeed);
        direction = Game.Direction.UP;
      } else if (goingDown && !goingUp) {
        pos.y = Math.min(farBottom, pos.y + currentSpeed);
        direction = Game.Direction.DOWN;
      } else if (goingLeft && !goingRight) {
        pos.x = Math.max(0.0, pos.x - currentSpeed);
        direction = Game.Direction.LEFT;
      } else if (goingRight && !goingLeft) {
        pos.x = Math.min(farRight, pos.x + currentSpeed);
        direction = Game.Direction.RIGHT;
      }
      else if (reset){
        pos = new Point2D.Double(2.0,2.0);
      }
      else if (checkPos){
        System.out.println(pos);
      }
      if (terrainChangeVerticalWall) {
       c.matrix[(int) pos.y][(int) pos.x].isWall = true;
      } else if (terrainChangeHorizontalWall) {
       c.matrix[(int) pos.y][(int) pos.x].isWall = true;
      } else if (terrainChangeClear) {
       c.matrix[(int) pos.y][(int) pos.x].isWall = false;
      } else if (delete) {
        c.matrix[(int) pos.y][(int) pos.x].sprites.clear();
      } else if (Ctrl && select) {
        try {
         DS.VectorNode n = c.dump();
         File writeTo = new File("tripleTest.txt");
         n.dump(new FileWriter(writeTo));
         DS.Root r = DS.load(new FileReader(writeTo)); 
         
        } catch (Exception e) {
        }
      }
    }

  }

  public static void render() throws OperationNotSupportedException {
    displayState = MenuState.render();
    int trunc_x = (int) pos.x, trunc_y = (int) pos.y; // x and y are truncated so we can map them onto the grid.
    for (int i = 0; i < WIDTH; i++) { // Vertical cursor coordinate (y)
      for (int j = 0; j < HEIGHT; j++) { // Horizontal cursor coordinate (x)
        if (i == trunc_y && j == trunc_x) {
          displayState += "@";
        } else if (c.matrix[j][i].sprites.size() != 0) {
          if (c.matrix[j][i].sprites.get(0).visible) {
            displayState += c.matrix[j][i].sprites.get(0).symbol;
          }
        } else {
          if (c.matrix[j][i].isWall == true){
            displayState += "|"; 
          }
          else{ 
            displayState += " ";
          }
        }
        displayState += " ";
      }
      displayState += "\n";
    }

    if (dialogueIn.size() == 0) {
      displayState += "+";
      for (int i = 0; i < DIALOGUE_WIDTH * 2 + 1; i++) { // Horizontal cursor coordinate (x)
        displayState += "-";
      }
      displayState += "+";
      displayState += "\n";
      for (int i = 0; i < DIALOGUE_HEIGHT; i++) {
        displayState += "|";
        for (int j = 0; j < DIALOGUE_WIDTH * 2 + 1; j++) {
          displayState += " ";
        }
        displayState += "|\n";
      }
      displayState += "+";
      for (int i = 0; i < DIALOGUE_WIDTH * 2 + 1; i++) {
        displayState += "-";
      }
      displayState += "+";
    } else {
      String[] split_in = ((String) dialogueIn.remove()).split("");
      String[][] to_insert = formatString(split_in);
      displayState += "+";
      for (int i = 0; i < DIALOGUE_WIDTH * 2 + 1; i++) { // Horizontal cursor coordinate (x)
        displayState += "-";
      }
      displayState += "+";
      displayState += "\n";
      for (int i = 0; i < DIALOGUE_HEIGHT; i++) {
        displayState += "|";
        for (int j = 0; j < DIALOGUE_WIDTH * 2 + 1; j++) {
          if (to_insert[i][j] != null) {
            displayState += to_insert[i][j];
          } else {
            displayState += ' ';
          }
        }
        displayState += "|\n";
      }
      displayState += "+";
      for (int i = 0; i < DIALOGUE_WIDTH * 2 + 1; i++) {
        displayState += "-";
      }
      displayState += "+";
    }
  }

  public static String[][] formatString(String[] str) {
    String[][] formattedDialogue = new String[DIALOGUE_HEIGHT][DIALOGUE_WIDTH * 2 + 1];
    for (int i = 0; i < Math.min(str.length, (DIALOGUE_HEIGHT - 2) * (DIALOGUE_WIDTH * 2)); i++) {
      formattedDialogue[i / DIALOGUE_WIDTH][i % DIALOGUE_WIDTH] = str[i];
    }
    return formattedDialogue;
  }

  // Method to call update and render repeatedly until the program exits.
  public static void run() throws Exception {
    Instant then = Instant.now();
    while (true) {
      Instant now = Instant.now();
      update((double) Duration.between(then, now).toNanos() / 1e6);
      // Provide the time delta for update based on the
      // time since the last iteration.
      render();
      textArea.setText(displayState); // Write displayState to the actual display
      textSpriteMenu.setText(MenuState.render());
      then = now;
    }
  }

  public static void main(String args[]) throws Exception { // program entry point
    init();
    run();
  }
}
