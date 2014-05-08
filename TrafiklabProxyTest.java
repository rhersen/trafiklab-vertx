import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class TrafiklabProxyTest {

    private TrafiklabProxy target;

    @Before
    public void setUp() throws Exception {
        target = new TrafiklabProxy();
    }

    @Test
    public void testStart() throws Exception {
        assertNotNull(target);
    }
}