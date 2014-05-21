import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TrafiklabAddressTest {

    private TrafiklabAddress subject;

    @Before
    public void setUp() throws Exception {
        Store store = new Store();
        store.setKey("nyckel");
        subject = new TrafiklabAddress(store);
    }

    @Test
    public void shouldCreateUrlBasedOnSiteIdInRequestPath() throws Exception {
        String result = subject.getUrl("/departures/1111");
        assertTrue(result.matches(".+\\?key=nyckel&timeWindow=60&siteId=1111$"));
    }
}