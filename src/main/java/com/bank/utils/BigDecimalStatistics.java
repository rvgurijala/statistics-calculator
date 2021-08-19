package com.bank.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.function.Consumer;
import java.util.stream.Collector;


public class BigDecimalStatistics implements Consumer<BigDecimal> {

    private BigDecimal sum = BigDecimal.ZERO, min, max;
    private long count = 0l;

    public BigDecimalStatistics() {
    }

    public static Collector<BigDecimal,?,BigDecimalStatistics> statistics() {
        return Collector.of(BigDecimalStatistics::new,
                BigDecimalStatistics::accept, BigDecimalStatistics::combine);
    }

    @Override
    public void accept(BigDecimal bd) {
        if(count == 0) {
            count = 1;
            sum = bd;
            min = bd;
            max = bd;
        } else {
            sum = sum.add(bd);
            if(min.compareTo(bd) > 0) min = bd;
            if(max.compareTo(bd) < 0) max = bd;
            count++;
        }
    }

    public BigDecimalStatistics combine(BigDecimalStatistics bigDecimalStatistics) {
        if(bigDecimalStatistics.count > 0) {
            if(count == 0) {
                count = bigDecimalStatistics.count;
                sum = bigDecimalStatistics.sum;
                min = bigDecimalStatistics.min;
                max = bigDecimalStatistics.max;
            } else {
                sum = sum.add(bigDecimalStatistics.sum);
                if(min.compareTo(bigDecimalStatistics.min) > 0) min = bigDecimalStatistics.min;
                if(max.compareTo(bigDecimalStatistics.max) < 0) max = bigDecimalStatistics.max;
                count += bigDecimalStatistics.count;
            }
        }
        return this;
    }

    public long getCount() {
        return count;
    }

    public BigDecimal getSum()
    {
        return sum;
    }

    public BigDecimal getAverage(MathContext mc)
    {
        return count < 2 ? sum: sum.divide(BigDecimal.valueOf(count), mc);
    }

    public BigDecimal getMin(){
        return min;
    }

    public BigDecimal getMax() {
        return max;
    }

    @Override
    public String toString() {
        return "BigDecimalStatistics{" +
                "sum=" + sum +
                ", min=" + min +
                ", max=" + max +
                ", count=" + count +
                '}';
    }
}
