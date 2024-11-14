package com.bankingcontrol.service;

import com.bankingcontrol.model.Account;
import com.bankingcontrol.model.Transaction;
import com.bankingcontrol.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getTransactions_DeveRetornarTransacoesOrdenadasPorData() {
        Account account = new Account();
        account.setId(1L);

        Transaction transaction1 = new Transaction();
        transaction1.setFromAccountId(1L);
        transaction1.setTransactionDate(new Date(1000));  // Data mais antiga

        Transaction transaction2 = new Transaction();
        transaction2.setToAccountId(1L);
        transaction2.setTransactionDate(new Date(2000));  // Data mais recente

        Mockito
                .doReturn(Arrays.asList(transaction2, transaction1))
                .when(transactionRepository)
                .findByFromAccountIdOrToAccountId(account.getId(), account.getId());

        List<Transaction> transactions = transactionService.getTransactions(account);

        assertEquals(transaction1, transactions.get(0));
        assertEquals(transaction2, transactions.get(1));

        Mockito
                .verify(transactionRepository, times(1))
                .findByFromAccountIdOrToAccountId(account.getId(), account.getId());
    }

    @Test
    void save_DeveSalvarTransacaoComDataAtual() {
        Transaction transaction = new Transaction();

        transactionService.save(transaction);

        Mockito
                .verify(transactionRepository, times(1))
                .save(transaction);

        assertEquals(
                (float) new Date().getTime() / 1000,
                (float) transaction.getTransactionDate().getTime() / 1000, 1);
    }
}
