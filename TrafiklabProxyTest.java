import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.junit.Assert.assertEquals;

public class TrafiklabProxyTest {

    private TrafiklabProxy target;

    @Before
    public void setUp() throws Exception {
        target = new TrafiklabProxy();
    }

    @Test
    public void getStationsReturnsIntegers() throws Exception {
        JsonArray result = target.getStations();
        for (Object station : result) {
            assertEquals(JsonObject.class, station.getClass());
        }
    }
}