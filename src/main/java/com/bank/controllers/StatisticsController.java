package com.bank.controllers;


import com.bank.models.Statistics;
import com.bank.models.Transaction;
import com.bank.services.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @PostMapping(path = "/transactions", produces = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public void createTransaction(@RequestBody @Valid Transaction transaction) {
        logger.debug("Creating transaction: " + transaction.toString());
        statisticsService.createTransaction(transaction);
        System.out.println(statisticsService.getStatistics());
    }


    @GetMapping(path = "/statistics", produces = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    public Statistics getStatistics() {
        logger.debug("Getting statistics");
        return statisticsService.getStatistics();
    }


    @DeleteMapping(path = "/transactions", produces = {"application/json"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllTransactions() {
        logger.debug("Deleting all transactions");
        statisticsService.cleanAll();
    }

}
