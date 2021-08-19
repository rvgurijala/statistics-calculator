package com.bank.utils;

import com.bank.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class BigDecimalStatisticsTest {

    @Test
    public void testAccept() {
        BigDecimalStatistics bigDecimalStatistics = new BigDecimalStatistics();

        bigDecimalStatistics.accept(new BigDecimal(100.00));

        assertTrue(bigDecimalStatistics.getCount() ==  1);

        bigDecimalStatistics.accept(new BigDecimal(110.00));

        assertTrue(bigDecimalStatistics.getCount() ==  2);
        assertEquals(bigDecimalStatistics.getMax(), new BigDecimal(110.00));
        assertEquals(bigDecimalStatistics.getMin(), new BigDecimal(100.00));
        assertEquals(bigDecimalStatistics.getAverage(MathContext.DECIMAL128), new BigDecimal(105.00));
    }

    @Test
    public void testCombine() {
        BigDecimalStatistics bigDecimalStatistics = new BigDecimalStatistics();
        bigDecimalStatistics.accept(new BigDecimal(100.00));
        assertTrue(bigDecimalStatistics.getCount() ==  1);

        BigDecimalStatistics bigDecimalStatistics1 = new BigDecimalStatistics();
        bigDecimalStatistics1.accept(new BigDecimal(110.00));
        assertTrue(bigDecimalStatistics1.getCount() ==  1);

        BigDecimalStatistics bigDecimalStatistics2 = bigDecimalStatistics.combine(bigDecimalStatistics1);
        assertTrue(bigDecimalStatistics2.getCount() ==  2);

        assertEquals(bigDecimalStatistics2.getMax(), new BigDecimal(110.00));
        assertEquals(bigDecimalStatistics2.getMin(), new BigDecimal(100.00));
        assertEquals(bigDecimalStatistics2.getAverage(MathContext.DECIMAL128), new BigDecimal(105.00));

    }
}
