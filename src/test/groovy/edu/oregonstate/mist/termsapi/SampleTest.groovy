package edu.oregonstate.mist.termsapi

import edu.oregonstate.mist.termsapi.core.Sample
import org.junit.Test
import static org.junit.Assert.*

class SampleTest {
    @Test
    public void testSample() {
        assertTrue(new Sample().message == 'hello world')
    }
}
