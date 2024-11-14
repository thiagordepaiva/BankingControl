package com.bankingcontrol.service;

import com.bankingcontrol.model.Account;
import com.bankingcontrol.model.Transaction;
import com.bankingcontrol.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Transaction> getTransactions(Account account) {
        return transactionRepository.findByFromAccountIdOrToAccountId(account.getId(), account.getId())
                .stream()
                .sorted((t1, t2) -> t1.getTransactionDate().compareTo(t2.getTransactionDate()))
                .collect(Collectors.toList());
    }

    public void save(Transaction transaction) {
        transaction.setTransactionDate(new Date());

        transactionRepository.save(transaction);
    }
}
