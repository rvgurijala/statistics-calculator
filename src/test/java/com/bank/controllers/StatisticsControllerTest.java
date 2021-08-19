package com.bank.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bank.Application;
import com.bank.exceptions.ExpiredTransactionException;
import com.bank.exceptions.FutureTransactionException;
import com.bank.models.Statistics;
import com.bank.models.Transaction;
import com.bank.services.StatisticsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class StatisticsControllerTest {

    @MockBean
    private StatisticsService service;

    @Autowired
    private GenericWebApplicationContext webApplicationContext;

    @Autowired
    private StatisticsController statisticsController;

    private MockMvc mockMvc;

    @Before
    public void getContext() {
        mockMvc = webAppContextSetup(webApplicationContext).build();
        assertNotNull(mockMvc);
    }

    protected String mapToJson(Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }

    protected <T> T mapFromJson(String json, Class<T> clazz)
            throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, clazz);
    }

    @Test
    public void testCreateTransaction() throws Exception {

        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal(100.000));
        transaction.setTimestamp(new Date());
        when(service.createTransaction(transaction)).thenReturn(transaction);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/transactions")
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapToJson(transaction))).andReturn();
        int status = mvcResult.getResponse().getStatus();
        assertEquals(201, status);
    }

    @Test
    public void testCreateTransactionWithInvalidJson() throws Exception {

        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal(100.000));
        transaction.setTimestamp(new Date());
        when(service.createTransaction(transaction)).thenReturn(transaction);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/transactions")
                .contentType(MediaType.APPLICATION_JSON_VALUE).content("{ss\"amount\":\"10.00\", \"timestamp\":\"2021-05-02T10:51:40.312Z\"}")).andReturn();
        int status = mvcResult.getResponse().getStatus();
        assertEquals(400, status);
    }

    @Test
    public void testCreateTransactionWithWrongFieldType() throws Exception {

        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal(100.000));
        transaction.setTimestamp(new Date());
        when(service.createTransaction(transaction)).thenReturn(transaction);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/transactions")
                .contentType(MediaType.APPLICATION_JSON_VALUE).content("{\"amount\":\"10.00aaa\", \"timestamp\":\"2021-05-02T10:51:40.312Z\"}")).andReturn();
        int status = mvcResult.getResponse().getStatus();
        assertEquals(422, status);
    }

    @Test
    public void testCreateTransactionWithOlderThanSixtySecTimestamp() throws Exception {

        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal(100.000));
        Date oldDate = new Date(System.currentTimeMillis() - 100000);
        transaction.setTimestamp(oldDate);

        when(service.createTransaction(any(Transaction.class))).thenThrow(new ExpiredTransactionException("Expired timestamp"));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/transactions")
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapToJson(transaction))).andReturn();

        int status = mvcResult.getResponse().getStatus();

        assertEquals(204, status);
    }


    @Test
    public void testCreateTransactionWithFutureTimestamp() throws Exception {

        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal(100.000));
        Date oldDate = new Date(System.currentTimeMillis() + 100000);
        transaction.setTimestamp(oldDate);

        when(service.createTransaction(any(Transaction.class))).thenThrow(new FutureTransactionException("Future timestamp"));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/transactions")
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(mapToJson(transaction))).andReturn();

        int status = mvcResult.getResponse().getStatus();

        assertEquals(422, status);
    }

    @Test
    public void testGetStatistics() throws Exception {

        Statistics statistics = new Statistics();
        statistics.setAvg("10.00");
        statistics.setMax("10.00");
        statistics.setMin("10.00");
        statistics.setCount(1l);

        when(service.getStatistics()).thenReturn(statistics);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/statistics")
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);

        String content = mvcResult.getResponse().getContentAsString();
        Statistics res = mapFromJson(content, Statistics.class);
        assertTrue(res.getCount() == 1);
    }

    @Test
    public void testDeleteAllTransactionsStatus() throws Exception {

        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal(100.000));
        transaction.setTimestamp(new Date());

        doNothing().when(service).cleanAll();

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete("/transactions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(204, status);
    }

}
