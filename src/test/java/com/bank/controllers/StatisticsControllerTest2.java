package com.bank.controllers;

import com.bank.Application;
import com.bank.models.Statistics;
import com.bank.models.Transaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class StatisticsControllerTest2 {

    @Autowired
    private StatisticsController statisticsController;

    @Test
    public void testStatisticsController() {

        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal(100.000));
        transaction.setTimestamp(new Date());

        Transaction transaction1 = new Transaction();
        transaction1.setAmount(new BigDecimal(110.000));
        transaction1.setTimestamp(new Date());

        statisticsController.createTransaction(transaction);
        statisticsController.createTransaction(transaction1);

        Statistics statistics = statisticsController.getStatistics();

        assertTrue(statistics.getCount() == 2);
        assertEquals(statistics.getMax(), "110.00");

        statisticsController.deleteAllTransactions();
        statistics = statisticsController.getStatistics();
        assertTrue(statistics.getCount() == 0);

    }

}
