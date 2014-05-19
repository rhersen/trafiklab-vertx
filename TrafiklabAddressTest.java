import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TrafiklabAddressTest {

    private TrafiklabAddress subject;

    @Before
    public void setUp() throws Exception {
        subject = new TrafiklabAddress(new Store());
    }

    @Test
    public void shouldCreateUrlBasedOnSiteIdInRequestPath() throws Exception {
        assertNotNull(subject);
        String result = subject.getUrl("/departures/1111");
        assertTrue(result.matches(".+\\?key=[^&]+&timeWindow=60&siteId=1111$"));
    }
}