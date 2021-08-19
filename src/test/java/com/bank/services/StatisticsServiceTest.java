package com.bank.services;

import com.bank.Application;
import com.bank.exceptions.ExpiredTransactionException;
import com.bank.exceptions.FutureTransactionException;
import com.bank.models.Statistics;
import com.bank.models.Transaction;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class StatisticsServiceTest {

    @Autowired
    private StatisticsService statisticsService;

    @After
    public void clean() {
        statisticsService.cleanAll();
    }

    @Test
    public void testCreateTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal(100.000));
        transaction.setTimestamp(new Date());

        Transaction transaction2 = new Transaction();
        transaction2.setAmount(new BigDecimal(110.000));
        transaction2.setTimestamp(new Date());

        statisticsService.createTransaction(transaction);
        statisticsService.createTransaction(transaction2);

        Statistics statistics = statisticsService.getStatistics();

        assertEquals(statistics.getMin(), "100.00");
        assertEquals(statistics.getMax(), "110.00");

        assertTrue(statistics.getCount() == 2);
    }

    @Test(expected = ExpiredTransactionException.class)
    public void testCreateTransactionWithOldDate() {
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal(100.000));
        transaction.setTimestamp(new Date(System.currentTimeMillis() - 100000));
        statisticsService.createTransaction(transaction);
    }

    @Test(expected = FutureTransactionException.class)
    public void testCreateTransactionWithFutureDate() {
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal(100.000));
        transaction.setTimestamp(new Date(System.currentTimeMillis() + 100000));
        statisticsService.createTransaction(transaction);
    }

    @Test
    public void testCleanExpiredTransactions() throws InterruptedException {
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal(100.000));
        transaction.setTimestamp(new Date(System.currentTimeMillis() - 57000));
        statisticsService.createTransaction(transaction);

        Statistics statistics = statisticsService.getStatistics();
        assertTrue(statistics.getCount() == 1);
        Thread.sleep(4000);

        statisticsService.cleanExpiredTransactions();

        statistics = statisticsService.getStatistics();
        assertTrue(statistics.getCount() == 0);

    }

    @Test
    public void testCleanAll() throws InterruptedException {
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal(100.000));
        transaction.setTimestamp(new Date());

        Transaction transaction2 = new Transaction();
        transaction2.setAmount(new BigDecimal(110.000));
        transaction2.setTimestamp(new Date());

        statisticsService.createTransaction(transaction);
        statisticsService.createTransaction(transaction2);

        Statistics statistics = statisticsService.getStatistics();
        assertTrue(statistics.getCount() == 2);
        statisticsService.cleanAll();
        statistics = statisticsService.getStatistics();
        assertTrue(statistics.getCount() == 0);
    }
}
