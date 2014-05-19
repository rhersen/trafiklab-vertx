import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrafiklabProxy extends Verticle {

    private final TrafiklabAddress trafiklabAddress;

    public TrafiklabProxy() {
        trafiklabAddress = new TrafiklabAddress(new Store());
    }

    public void start() {
        vertx.createHttpServer()
                .requestHandler(request -> {
                    String s = trafiklabAddress.getUrl(request.path());
                    vertx.createHttpClient()
                            .setHost("api.trafiklab.se")
                            .setSSL(true)
                            .setPort(443)
                            .get(s, rsp -> rsp.bodyHandler(trafiklabData -> {
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
                })
                .listen(3000);
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
    String getKey() {
        return "8589732b19f6c9a78b004aff74f28d98";
//        throw new IllegalStateException("get your own key");
    }
}