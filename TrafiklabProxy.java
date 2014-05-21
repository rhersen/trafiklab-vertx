import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrafiklabProxy extends Verticle {

    private final TrafiklabAddress trafiklabAddress;
    private final Store store;

    public TrafiklabProxy() {
        store = new Store();
        trafiklabAddress = new TrafiklabAddress(store);
    }

    public void start() {
        vertx.createHttpServer()
                .requestHandler(request -> {
                    if (request.path().startsWith("/key")) {
                        handlePost(request);
                    } else
                        try {
                            handleGetDeparture(request);
                        } catch (IllegalStateException e) {
                            request.response().setStatusCode(401).end();
                        }
                })
                .listen(3000);
    }

    private void handleGetDeparture(HttpServerRequest request) {
        vertx.createHttpClient()
                .setHost("api.trafiklab.se")
                .setSSL(true)
                .setPort(443)
                .get(trafiklabAddress.getUrl(request.path()), rsp -> rsp.bodyHandler(trafiklabData -> {
                    Buffer buffer = new Buffer(
                            new JsonObject(trafiklabData.toString())
                                    .getObject("DPS")
                                    .getObject("Trains")
                                    .getArray("DpsTrain")
                                    .encode()
                    );

                    request.response()
                            .putHeader("Content-Length", Integer.toString(buffer.length()))
                            .putHeader("Content-Type", "application/json")
                            .write(buffer);
                }))
                .putHeader("Accept", "application/json")
                .end();
    }

    private void handlePost(final HttpServerRequest request) {
        request
                .expectMultiPart(true)
                .endHandler(new VoidHandler() {
                    protected void handle() {
                        store.setKey(request.formAttributes().get("key"));
                    }
                });
    }
}

class TrafiklabAddress {
    private final Pattern pattern = Pattern.compile(".*/(\\d+)");
    private Store store;

    TrafiklabAddress(Store store) {
        this.store = store;
    }

    String getUrl(String path) {
        Matcher m = pattern.matcher(path);
        String siteId = m.matches() ? m.group(1) : "9525";
        return "/sl/realtid/GetDpsDepartures?key=" + store.getKey() + "&timeWindow=60&siteId=" + siteId;
    }
}

class Store {
    private String key;

    String getKey() {
        if (key == null) {
            throw new IllegalStateException("get your own key");
        }
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}