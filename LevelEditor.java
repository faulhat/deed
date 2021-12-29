import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.naming.OperationNotSupportedException;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.time.Instant;
import java.time.Duration;
import java.io.*;


public class LevelEditor{
    
  public static final int WIDTH = 10;
  public static final int HEIGHT = 12;
  public static final int DIALOGUE_HEIGHT = 4;
  public static final int DIALOGUE_WIDTH = WIDTH-2;
  public static final double SPEED = 0.010;
  
  
  
  //KeyBox
  public static KeyBox box;

  // Components of the GUI
  public static JTextArea textArea;

  // Position of the player
  public static Point2D.Double pos;

  // Game display state
  private static String displayState;
  //Matrix of chars to store insertion for levels
  public static char[][] matrix = new char[HEIGHT][WIDTH];
  //Class representing menus for insertion of various sprites
  
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

    // flags to be set when a component is clicked or released
    public ConcurrentHashMap<Integer, Boolean> wasInteractedWith;
    
    //Input Queue for dialogue and other stuff
    public ConcurrentLinkedQueue dialogueInsert;
    // A subclass of KeyListener that updates the KeyBox state when keys are
    // pressed.
    // Note that it is not static, as it is bound to the KeyBox instance it's a part
    // of.
    public class MyFrame extends JFrame implements KeyListener, ActionListener{
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
      @Override
      public void actionPerformed(ActionEvent a){

      }
    }
    
    public JFrame frame;
    //Menu for sprite insertion
    public JMenuBar spriteMenuBar;
    

    public KeyBox() {
      wasPressed = new ConcurrentHashMap<>();
      isPressed = new ConcurrentHashMap<>();
      spriteMenuBar = new JMenuBar();
      frame = new MyFrame();
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setLayout(new FlowLayout());
      JMenuItem dialoguePoint = new JMenuItem("DialoguePoint");
      JMenuItem exit = new JMenuItem("Exit");
      JMenu interactions = new JMenu("interactions");
      JMenu NPC = new JMenu("NPC");
      JMenu Enemy = new JMenu("ENEMY");
      JMenuItem standardEncounter = new JMenuItem("StandardEncounter");
      JMenuItem bossEncounter = new JMenuItem("BossEncounter");
      JMenuItem trader = new JMenuItem("trader");
      JMenuItem partyMember = new JMenuItem("PartyMember");
      NPC.add(trader);
      NPC.add(partyMember);
      Enemy.add(standardEncounter);
      Enemy.add(bossEncounter);
      interactions.add(exit);
      interactions.add(dialoguePoint);
      spriteMenuBar.add(NPC);
      spriteMenuBar.add(Enemy);
      spriteMenuBar.add(interactions);
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
  
  // Initialize game state
  public static void init() throws OperationNotSupportedException{
    pos = new Point2D.Double(2.0, 2.0);
    
    textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
    for (int i = 0; i < HEIGHT; i++){
      for (int j = 0; j < WIDTH; j++){
         matrix[i][j] = ' ';
      }
    }
    render();
    textArea.setText(displayState);

    box = new KeyBox();
    box.frame.add(textArea);
    box.frame.pack();
    box.frame.setVisible(true);
    box.frame.setJMenuBar(box.spriteMenuBar);
  }
  public static void update(double delta) {
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
        terrainChangeVerticalWall = box.getResetKey(86),
        terrainChangeHorizontalWall = box.getResetKey(72),
        terrainChangeClear = box.getResetKey(67),
        openInsertMenu = box.getResetKey(73),
        Serialize = box.getResetKey(77),
        select = box.getResetKey(83);

    if (goingUp && !goingDown) { // Now that we've dealt with all possible diagonals, we can deal with the normal                          
                                        // directions.
      pos.y = Math.max(0.0, pos.y - currentSpeed);
     
    } else if (goingDown && !goingUp) {
      pos.y = Math.min(farBottom, pos.y + currentSpeed);
      
    } else if (goingLeft && !goingRight) {
      pos.x = Math.max(0.0, pos.x - currentSpeed);
      
    } else if (goingRight && !goingLeft) {
      pos.x = Math.min(farRight, pos.x + currentSpeed);
      
    }
    if (terrainChangeVerticalWall){
      matrix[(int)pos.y][(int)pos.x] = '|';
    }
    else if (terrainChangeHorizontalWall){
      matrix[(int)pos.y][(int)pos.x] = '-';
    }
    else if (terrainChangeClear){
      matrix[(int)pos.y][(int)pos.x] = ' ';
    }
    else if (Serialize){
     try{
         
         
         
     }
     catch(Exception e){
     }
    }
    
    
  }
  public static void render() throws OperationNotSupportedException{
    displayState = "";
    int trunc_x = (int) pos.x, trunc_y = (int) pos.y; // x and y are truncated so we can map them onto the grid.
    for (int i = 0; i < HEIGHT-1; i++) { // Vertical cursor coordinate (y)
      for (int j = 0; j < WIDTH-1; j++) { // Horizontal cursor coordinate (x)
        if (i == trunc_y && j == trunc_x) {
          displayState += "@";
        }
        else{           
           displayState += matrix[i][j];
        }   
        displayState += " ";
      }
      displayState += "\n\n";
    }
    displayState += "+";
    for (int i = 0; i < DIALOGUE_WIDTH*2+1; i++) { // Horizontal cursor coordinate (x)
      displayState += "-";
    }
    displayState += "+";
    displayState +="\n";
    for (int i = 0;i < DIALOGUE_HEIGHT; i++) {
      displayState += "|";
      for (int j = 0; j < DIALOGUE_WIDTH*2+1; j++){
        displayState += " ";
      }
      displayState+="|\n";
    }
    displayState += "+";
    for(int i = 0; i < DIALOGUE_WIDTH*2+1; i++){
      displayState += "-";
    }
    displayState += "+";
  }

  // Method to call update and render repeatedly until the program exits.
  public static void run() throws Exception{
    Instant then = Instant.now();
    while (true) {
      Instant now = Instant.now();
      update((double) Duration.between(then, now).toNanos() / 1e6);
       // Provide the time delta for update based on the
       // time since the last iteration.
       render();
       textArea.setText(displayState); // Write displayState to the actual display
       then = now;
    }
  }


  public static void main(String args[]) throws Exception { // program entry point
    try{  
      init();
      run();
    }
    catch(Exception e) {
      throw e;
    }
  }
}

  


