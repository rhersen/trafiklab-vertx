import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrafiklabProxy extends Verticle {

    private Matcher m;

    public void start() {
        vertx.createHttpServer()
                .requestHandler(request -> {
                    m = Pattern.compile(".*/(\\d+)").matcher(request.path());
                    String siteId = m.matches() ? m.group(1) : "9525";
                    String s = "/sl/realtid/GetDpsDepartures?key=" + getKey() + "&timeWindow=60&siteId=" + siteId;
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

    private String getKey() {
        throw new IllegalStateException("get your own key");
    }
}
