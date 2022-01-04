import java.util.Map;
import java.util.EnumMap;
import java.util.function.Function;
import java.util.function.BiConsumer;

public class Template {
    // A function which handles an event for a target sprite.
    public static class Handler extends BiConsumer<Game.Event, Sprite> {
        public void accept(){
            (Game.Event, Sprite) -> {

            };
        }
    }

    // A map of EventTypes to their appropriate Handlers.
    public static class HandlerMap extends EnumMap<Game.EventType, Handler> {
        public HandlerMap(Class<Game.EventType> keyType) {
            super(keyType);
        }
        
        public HandlerMap(HandlerMap other) {
            super(other);
        }

        public HandlerMap(Map<Game.EventType, BiConsumer<Game.Event, Sprite>> other) {
            super(other);
        }
    }

    // Alias for a BiFunction that will construct new Sprites
    public static class Initializer extends BiFunction<Map<Object, Object>, HandlerMap, Sprite> {

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

    // Utility method to generate a new sprite from this template
    public Sprite genSprite(Map<Object, Object> data) {
        return initializer.apply(data, handlerMap);
    }
}