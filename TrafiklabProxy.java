import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrafiklabProxy extends Verticle {

    private final TrafiklabAddress trafiklabAddress = new TrafiklabAddress();

    public void start() {
        vertx.createHttpServer()
                .requestHandler(request -> {
                    if (request.path().startsWith("/key")) {
                        handlePost(request);
                    } else {
                        vertx.eventBus().send("store", "", (Message<String> m) -> {
                            String key = m.body();
                            if (key.isEmpty()) {
                                request.response().setStatusCode(401).end();
                            } else {
                                handleGetDeparture(request, key);
                            }
                        });
                    }
                })
                .listen(3000);
    }

    private void handleGetDeparture(HttpServerRequest request, String key) {
        vertx.createHttpClient()
                .setHost("api.trafiklab.se")
                .setSSL(true)
                .setPort(443)
                .get(trafiklabAddress.getUrl(request.path(), key), rsp -> rsp.bodyHandler(trafiklabData -> {
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
                .bodyHandler(buffer -> vertx.eventBus().send(
                        "store",
                        request.formAttributes().get("key"),
                        (Message<String> m) -> request.response().setStatusCode(200).end()
                ));
    }
}

class TrafiklabAddress {
    private final Pattern pattern = Pattern.compile(".*/(\\d+)");

    String getUrl(String path, String key) {
        Matcher m = pattern.matcher(path);
        String siteId = m.matches() ? m.group(1) : "9525";
        return "/sl/realtid/GetDpsDepartures?key=" + key + "&timeWindow=60&siteId=" + siteId;
    }
}

