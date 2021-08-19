package com.bank.services;

import com.bank.exceptions.ExpiredTransactionException;
import com.bank.exceptions.FutureTransactionException;
import com.bank.exceptions.StatisticsException;
import com.bank.models.Statistics;
import com.bank.models.Transaction;
import com.bank.utils.BigDecimalStatistics;
import com.bank.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private static final List<Transaction> transactions = new ArrayList<>();
    private static final Integer TRANSACTIONS_SCALE = 2;
    private Statistics statistics;
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private Lock readLock = rwLock.readLock();
    private Lock writeLock = rwLock.writeLock();

    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

    public StatisticsService() {
        initiateCleanupTask();
    }

    public Statistics getStatistics() {
        readLock.lock();
        try {
            if (statistics == null) {
                calculateStats();
            }
            return statistics;
        } finally {
            readLock.unlock();
        }
    }

    public Transaction createTransaction(Transaction transaction) {

        if (System.currentTimeMillis() < transaction.getTimestamp().getTime()) {
            logger.warn("Transaction with future date: " + transaction.toString());
            throw new FutureTransactionException("Future dated transaction");
        }

        if ((System.currentTimeMillis() - transaction.getTimestamp().getTime()) > Constants.WINDOW_TIME) {
            logger.warn("Transaction with expired date: " + transaction.toString());
            throw new ExpiredTransactionException("Expired transaction");
        }

        writeLock.lock();

        try {
            logger.info("Adding transaction: " + transaction.toString());
            transactions.add(transaction);
            calculateStats();
            return transaction;
        } finally {
            writeLock.unlock();
        }
    }

    public void cleanAll() {
        writeLock.lock();
        try {
            logger.info("Cleaning all transaction");
            transactions.clear();
            calculateStats();
        } finally {
            writeLock.unlock();
        }
    }

    public void cleanExpiredTransactions() {
        writeLock.lock();
        try {
            transactions.removeIf(
                    t -> (System.currentTimeMillis() - t.getTimestamp().getTime()) > Constants.WINDOW_TIME);
            calculateStats();
        } finally {
            writeLock.unlock();
        }
    }

    private void calculateStats() {
        List<BigDecimal> allAmounts = transactions.stream().map(Transaction::getAmount).collect(Collectors.toList());
        BigDecimalStatistics bigDecimalStatistics = allAmounts.stream().collect(BigDecimalStatistics.statistics());
        statistics = new Statistics(bigDecimalStatistics.getSum().setScale(TRANSACTIONS_SCALE, BigDecimal.ROUND_HALF_UP).toString(),
                bigDecimalStatistics.getAverage(MathContext.DECIMAL128).setScale(TRANSACTIONS_SCALE, BigDecimal.ROUND_HALF_UP).toString(),
                bigDecimalStatistics.getMax() == null ? BigDecimal.ZERO.setScale(TRANSACTIONS_SCALE, BigDecimal.ROUND_HALF_UP).toString() : bigDecimalStatistics.getMax().setScale(TRANSACTIONS_SCALE, BigDecimal.ROUND_HALF_UP).toString(),
                bigDecimalStatistics.getMin() == null ? BigDecimal.ZERO.setScale(TRANSACTIONS_SCALE, BigDecimal.ROUND_HALF_UP).toString() : bigDecimalStatistics.getMin().setScale(TRANSACTIONS_SCALE, BigDecimal.ROUND_HALF_UP).toString(),
                bigDecimalStatistics.getCount());
    }


    private void initiateCleanupTask() {

        Thread task = new Thread(() -> {
            while (true) {
                cleanExpiredTransactions();
                try {
                    Thread.sleep(Constants.CLEAN_UP_INTERVAL);
                } catch (InterruptedException ex) {
                    logger.error("Exception in task thread sleeping: " + Thread.currentThread());
                    throw new StatisticsException("Exception in thread sleep" + ex.getMessage());
                }
            }
        });

        task.start();
    }

}
