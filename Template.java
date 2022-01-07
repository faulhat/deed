import java.util.Map;
import java.util.EnumMap;
import java.util.function.Function;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.ArrayList;

public class Template {
    @FunctionalInterface
    public interface CheckedFunction<T, T2>{
       void accept(T t, T2 t2) throws InterruptedException;
    }
    // A function which handles an event for a target sprite.
    @FunctionalInterface
    public static interface Handler extends CheckedFunction<Game.Event,Sprite>{
      
    }

    // A map of EventTypes to their appropriate Handlers.
    public static class HandlerMap extends EnumMap<Game.EventType, Handler> {
        public HandlerMap() {
            super(Game.EventType.class);
        }
        
        public HandlerMap(HandlerMap other) {
            super(other);
        }

        /*public HandlerMap(Map<Game.EventType, BiConsumer<Game.Event, Sprite>> other) {
            super(other);
        }*/
    }

    // Alias for a BiFunction that will construct new Sprites
    //<String,Object> map is for unique instance sprite bindings
    public static interface Initializer extends BiFunction<ArrayList<Object>, HandlerMap, Sprite> {
    }

    // Name for this template so that it can be referred to in debugging and in the level editor.
    public final String name;

    // A BiFunction which takes initial data and this template's handlerMap and generates a new Sprite.
    public Initializer initializer;

    public HandlerMap handlerMap;

    public Template(String name, Initializer initializer, HandlerMap handlerMap) {
        this.name = name;
        this.initializer = initializer;
        this.handlerMap = handlerMap;
    }
    public Template(){
        this(null,null,null);
    }

    // Utility method to generate a new sprite from this template
    public Sprite genSprite(ArrayList<Object> data) {
        return initializer.apply(data, handlerMap);
    }
}