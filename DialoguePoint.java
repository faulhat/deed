import java.util.concurrent.LinkedBlockingQueue;

import javax.naming.OperationNotSupportedException;

public class DialoguePoint extends Sprite {
   // Dialogue to play when player reaches specific point
   public String dialogueSequence;
   // Name of dialogue point
   public String name;
   // Queue to print to
   public LinkedBlockingQueue<String> queue;
   // flag to be reset when the player moves away from the square and back
   public boolean repeat;

   public DialoguePoint(String dialogue, String name, LinkedBlockingQueue<String> queue) {
      this.dialogueSequence = dialogue;
      this.name = name;
      visible = true;
      this.queue = queue;
      this.repeat = true;
   }

   public char getSymbol() throws OperationNotSupportedException {
      return 'd';
   }

   public String getName() {
      return name;
   }

   public String getID() {
      return "D_POINT";
   }

   public void setDialogue(String dialogue) {
      dialogueSequence = dialogue;
   }

   public void onEvent(Game.Event e) {
      if (e instanceof Game.TouchEvent) {
         Game.TouchEvent touchE = (Game.TouchEvent) e;
         queue.add(dialogueSequence);

      }
   }

}