import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

public class Store extends Verticle implements Handler<Message<String>> {
    private String key = "";

    public void start() {
        vertx.eventBus().registerHandler("store", this);
    }

    String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void handle(Message<String> message) {
        String body = message.body();
        if (!body.isEmpty()) {
            setKey(body);
        }
        message.reply(getKey());
    }
}
