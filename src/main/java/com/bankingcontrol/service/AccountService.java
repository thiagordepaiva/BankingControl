package com.bankingcontrol.service;

import com.bankingcontrol.model.Account;
import com.bankingcontrol.model.Transaction;
import com.bankingcontrol.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

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
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));
    }

    public Account createAccount(Account account, Model model) {
        if (this.findAccountByUsername(account.getUsername())) {
            model.addAttribute("error", "Já existe uma conta para o usuário informado.");
            model.addAttribute("account", account);

            return null;
        }

        if (!account.getPassword().equals(account.getConfirmPassword())) {
            model.addAttribute("error", "As senhas não coincidem.");
            model.addAttribute("account", account);

            return null;
        }

        return this.save(account);
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
        if (transaction.getFromAccountId().equals(transaction.getToAccountId())) {
            throw new IllegalArgumentException("Conta de origem e destino devem ser diferentes");
        }

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
            throw new IllegalArgumentException("O seu saldo é insuficiente para realizar essa operação");
        }
    }

    public Account findAccountByUsernameAndPassword(String username, String password) {
        return accountRepository.findByUsernameAndPassword(username, password);
    }

    public boolean findAccountByUsername(String username) {
        return accountRepository.findByUsername(username) != null;
    }
}
