import javax.naming.OperationNotSupportedException;
public class DialoguePoint extends Sprite{
   //Dialogue to play when player reaches specific point
   public String dialogueSequence;
   //Name of dialogue point
   public String name;
   
   public DialoguePoint(String dialogue, String name){
      this.dialogueSequence = dialogue;
      this.name = name;
      visible = false;
   }
 
   public char getSymbol() throws OperationNotSupportedException{
      return 'd';
   }
   public String getName(){
      return name;
   }
   public void setName(String name){
      this.name = name;
   }
   public void setDialogue(String dialogue){
      dialogueSequence = dialogue;
   }
   public void onEvent(Game.Event e){
      if (e instanceof Game.TouchEvent) {
         Game.TouchEvent touchE = (Game.TouchEvent) e;
         
     
      }
   }
   
}