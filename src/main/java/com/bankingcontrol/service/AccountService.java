package com.bankingcontrol.service;

import com.bankingcontrol.model.Account;
import com.bankingcontrol.model.Transaction;
import com.bankingcontrol.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionService transactionService;

    @PersistenceContext
    private EntityManager entityManager;

    public Account save(Account account) {
        return accountRepository.save(account);
    }

    public Account getAccount(Long id) {
        Account account = accountRepository.findById(id).orElse(null);

        if (account == null) {
            throw new IllegalArgumentException("Conta não encontrada");
        }

        return account;
    }

    @Transactional
    public Account deposit(Transaction transaction) {
        Account account = this.getAccount(transaction.getFromAccountId());

        entityManager.lock(account, LockModeType.PESSIMISTIC_WRITE);

        account.setBalance(account.getBalance().add(transaction.getAmount()));

        transactionService.save(transaction);

        return this.save(account);
    }

    @Transactional
    public Account withdraw(Transaction transaction) {
        Account account = this.getAccount(transaction.getFromAccountId());

        isSufficientBalanceForDebit(transaction, account);

        entityManager.lock(account, LockModeType.PESSIMISTIC_WRITE);

        account.setBalance(account.getBalance().subtract(transaction.getAmount()));

        transactionService.save(transaction);

        return this.save(account);
    }

    @Transactional
    public Account transfer(Transaction transaction) {
        Account fromAccount = this.getAccount(transaction.getFromAccountId());
        Account toAccount = this.getAccount(transaction.getToAccountId());

        isSufficientBalanceForDebit(transaction, fromAccount);

        entityManager.lock(fromAccount, LockModeType.PESSIMISTIC_WRITE);
        entityManager.lock(toAccount, LockModeType.PESSIMISTIC_WRITE);

        fromAccount.setBalance(fromAccount.getBalance().subtract(transaction.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(transaction.getAmount()));

        transactionService.save(transaction);

        this.save(fromAccount);
        this.save(toAccount);

        return fromAccount;
    }

    private static void isSufficientBalanceForDebit(Transaction transaction, Account account) {
        if (account.getBalance().compareTo(transaction.getAmount()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }
    }

    public Account findAccountByUsernameAndPassword(String username, String password) {
        return accountRepository.findByUsernameAndPassword(username, password);
    }

    public boolean findAccountByUsername(String username) {
        return accountRepository.findByUsername(username) != null;
    }
}
