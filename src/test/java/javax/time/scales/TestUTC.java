/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package javax.time.scales;

import javax.time.TimeScaleInstant;
import javax.time.TimeScales;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import static javax.time.scales.Util.*;

/**
 *
 * @author mthornton
 */
public class TestUTC {
    /* The conversion between Instant and TimeScales.utc() is trivial so not tested here.
     * It is the conversion between TAI and UTC which is interesting
     * */

    @Test public void testUtcConversions() {
        // test ambigous region prior to 1965-09-01
        // note 100ms step in UTC gives 200ms step in TAI
        cvtToTAI(date(1965,8,31)+time(23,59,59), millis(900)+1, date(1965,9,1)+time(0,0,3), 955058000);
        cvtToTAI(date(1965,9,1), 0, date(1965,9,1)+4, 155058000);
        cvtFromTAI(date(1965,9,1)+time(0,0,3), 955058000, date(1965,8,31)+time(23,59,59), millis(900)+1);
        cvtFromTAI(date(1965,9,1)+time(0,0,4),   5058000, date(1965,8,31)+time(23,59,59), millis(950)+1);
        cvtFromTAI(date(1965,9,1)+time(0,0,4),  55058000, date(1965,8,31)+time(23,59,59), millis(900)+2);
        cvtFromTAI(date(1965,9,1)+time(0,0,4), 105058000, date(1965,8,31)+time(23,59,59), millis(950)+1);
        cvtFromTAI(date(1965,9,1)+time(0,0,4), 155058000, date(1965,9,1), 0);

        // Test near invalid region; result is clamped (should we throw an exception instead)
        cvtToTAI(date(1968,1,31)+time(23,59,59), millis(910), date(1968,2,1)+6, 185682000);
        cvtToTAI(date(1968,2,1), 0, date(1968,2,1)+6, 185682000);
        // note the 100ms gap in the UTC timeline
        cvtFromTAI(date(1968,2,1)+6, 185681999, date(1968,1,31)+time(23,59,59), millis(900)+2);
        cvtFromTAI(date(1968,2,1)+6, 185682000, date(1968,2,1), 0);

        // Test near leap second at 2008-12-31T23:59:60
        cvtToTAI(date(2008,12,31)+time(23,59,59), millis(100), date(2009,1,1)+time(0,0,32), millis(100));
        cvtToTAI(date(2009,1,1), millis(200), date(2009,1,1)+time(0,0,34), millis(200));

        cvtFromTAI(date(2009,1,1)+time(0,0,31), millis(100), date(2008,12,31)+time(23,59,58), millis(100));
        cvtFromTAI(date(2009,1,1)+time(0,0,32), millis(200), date(2008,12,31)+time(23,59,59), millis(200));
        cvtFromTAI(date(2009,1,1)+time(0,0,33), millis(300), date(2008,12,31)+time(23,59,59), millis(300));
        cvtFromTAI(date(2009,1,1)+time(0,0,34), millis(400), date(2009,1,1)+time(0,0,0), millis(400));
    }

    private void cvtToTAI(long epochSeconds, int nanoOfSecond, long taiEpochSeconds, int taiNanoOfSecond) {
        TimeScaleInstant t = TimeScaleInstant.seconds(TimeScales.utc(), epochSeconds, nanoOfSecond);
        TimeScaleInstant ts = TimeScales.utc().toTAI(t);
        assertEquals(ts.getEpochSeconds(), taiEpochSeconds);
        assertEquals(ts.getNanoOfSecond(), taiNanoOfSecond);
    }

    private void cvtFromTAI(long taiEpochSeconds, int taiNanoOfSecond, long expectedEpochSeconds, int expectedNanoOfSecond) {
        TimeScaleInstant ts = TimeScaleInstant.seconds(TimeScales.tai(), taiEpochSeconds, taiNanoOfSecond);
        TimeScaleInstant t = TimeScales.utc().toTimeScaleInstant(ts);
        assertEquals(t.getEpochSeconds(), expectedEpochSeconds);
        assertEquals(t.getLeapSecond(), 0);
        assertEquals(t.getNanoOfSecond(), expectedNanoOfSecond);
    }

    @Test public void testUtcValidity() {
        checkValidity(date(2008,12,31)+time(23,59,59), millis(500), TimeScaleInstant.Validity.ambiguous);
        checkValidity(date(2008,12,31)+time(23,59,59), 0, TimeScaleInstant.Validity.ambiguous);
        checkValidity(date(2008,12,31)+time(23,59,58), 0, TimeScaleInstant.Validity.valid);
        checkValidity(date(2009,1,1), 0, TimeScaleInstant.Validity.valid);
        
        // check an invalid interval
        checkValidity(date(1968,1,31)+time(23,59,59), millis(890), TimeScaleInstant.Validity.valid);
        checkValidity(date(1968,1,31)+time(23,59,59), millis(910), TimeScaleInstant.Validity.invalid);
        checkValidity(date(1968,2,1), 0, TimeScaleInstant.Validity.valid);

        // check an ambiguous interval
        checkValidity(date(1965, 8, 31)+time(23,59,59), millis(890), TimeScaleInstant.Validity.valid);
        checkValidity(date(1965, 8, 31)+time(23,59,59), millis(910), TimeScaleInstant.Validity.ambiguous);
        checkValidity(date(1965, 9, 1), 0, TimeScaleInstant.Validity.valid);
    }

    private void checkValidity(long epochSeconds, int nanoOfSecond, TimeScaleInstant.Validity validity) {
        TimeScaleInstant t = TimeScaleInstant.seconds(TimeScales.utc(), epochSeconds, nanoOfSecond);
        assertEquals(t.getValidity(), validity);
    }
}
