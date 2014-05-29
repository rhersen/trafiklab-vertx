import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

public class Store extends Verticle implements Handler<Message<String>> {
    private String key = "";

    public void start() {
        vertx.eventBus().registerHandler("store", this);
    }

    String get() {
        return key;
    }

    public void put(String key) {
        this.key = key;
    }

    public void handle(Message<String> message) {
        String body = message.body();
        if (!body.isEmpty()) {
            put(body);
        }
        message.reply(get());
    }
}
