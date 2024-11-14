package com.bankingcontrol.service;

import com.bankingcontrol.model.Account;
import com.bankingcontrol.model.Transaction;
import com.bankingcontrol.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Model model;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_DeveSalvarEretornarConta() {
        Account account = new Account();
        Mockito
                .doReturn(account)
                .when(accountRepository)
                .save(account);

        Account savedAccount = accountService.save(account);

        assertEquals(account, savedAccount);
        Mockito
                .verify(accountRepository, times(1)).save(account);

    }

    @Test
    void getAccount_DeveRetornarContaQuandoContaExiste() {
        Account account = new Account();
        account.setId(1L);
        Mockito
                .doReturn(Optional.of(account))
                .when(accountRepository)
                .findById(1L);


        Account foundAccount = accountService.getAccount(1L);

        assertEquals(account, foundAccount);
    }

    @Test
    void getAccount_DeveLancarExcecaoQuandoContaNaoExiste() {
        Mockito
                .doReturn(Optional.empty())
                .when(accountRepository).findById(1L);

        Exception exception = assertThrows(
                IllegalArgumentException.class, () -> accountService.getAccount(1L));

        assertEquals("Conta não encontrada", exception.getMessage());
    }

    @Test
    void createAccount_DeveRetornarErroQuandoUsuarioJaExiste() {
        Account account = new Account();
        account.setUsername("user");
        Mockito
                .doReturn(account)
                .when(accountRepository)
                .findByUsername("user");

        Account result = accountService.createAccount(account, model);

        Mockito
                .verify(model)
                .addAttribute("error", "Já existe uma conta para o usuário informado.");

        Mockito
                .verify(model)
                .addAttribute("account", account);

        assertNull(result);
    }

    @Test
    void createAccount_DeveRetornarErroQuandoSenhasNaoCoincidem() {
        Account account = new Account();
        account.setUsername("user");
        account.setPassword("pass");
        account.setConfirmPassword("differentPass");

        Account result = accountService.createAccount(account, model);

        Mockito
                .verify(model)
                .addAttribute("error", "As senhas não coincidem.");

        Mockito
                .verify(model)
                .addAttribute("account", account);

        assertNull(result);
    }

    @Test
    void createAccount_DeveSalvarERetornarContaQuandoContaValida() {
        Account account = new Account();
        account.setUsername("user");
        account.setPassword("pass");
        account.setConfirmPassword("pass");
        Mockito
                .doReturn(null)
                .when(accountRepository)
                .findByUsername("user");
        Mockito
                .doReturn(account)
                .when(accountRepository)
                .save(account);

        Account savedAccount = accountService.createAccount(account, model);

        assertEquals(account, savedAccount);
        Mockito
                .verify(accountRepository)
                .save(account);

    }

    @Test
    void deposit_DeveAdicionarSaldoEretornarConta() {
        Transaction transaction = new Transaction();
        transaction.setFromAccountId(1L);
        transaction.setAmount(BigDecimal.valueOf(100));

        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(200));
        Mockito
                .doReturn(Optional.of(account))
                .when(accountRepository)
                .findById(1L);
        Mockito
                .doReturn(account)
                .when(accountRepository)
                .save(account);

        Account updatedAccount = accountService.deposit(transaction);

        assertEquals(BigDecimal.valueOf(300), updatedAccount.getBalance());
        Mockito
                .verify(transactionService)
                .save(transaction);

        Mockito
                .verify(entityManager)
                .lock(account, LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void withdraw_DeveSubtrairSaldoEretornarConta() {
        Transaction transaction = new Transaction();
        transaction.setFromAccountId(1L);
        transaction.setAmount(BigDecimal.valueOf(50));

        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(100));
        Mockito
                .doReturn(Optional.of(account))
                .when(accountRepository)
                .findById(1L);
        Mockito
                .doReturn(account)
                .when(accountRepository)
                .save(account);

        Account updatedAccount = accountService.withdraw(transaction);

        assertEquals(BigDecimal.valueOf(50), updatedAccount.getBalance());

        Mockito
                .verify(transactionService)
                .save(transaction);

        Mockito
                .verify(entityManager)
                .lock(account, LockModeType.PESSIMISTIC_WRITE);

    }

    @Test
    void withdraw_DeveLancarExcecaoQuandoSaldoInsuficiente() {
        Transaction transaction = new Transaction();
        transaction.setFromAccountId(1L);
        transaction.setAmount(BigDecimal.valueOf(200));

        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(100));
        Mockito
                .doReturn(Optional.of(account))
                .when(accountRepository)
                .findById(1L);

        Exception exception = assertThrows(
                IllegalArgumentException.class, () -> accountService.withdraw(transaction));

        assertEquals("O seu saldo é insuficiente para realizar essa operação", exception.getMessage());
    }

    @Test
    void transfer_DeveTransferirSaldoEntreContasDiferentes() {
        Transaction transaction = new Transaction();
        transaction.setFromAccountId(1L);
        transaction.setToAccountId(2L);
        transaction.setAmount(BigDecimal.valueOf(50));

        Account fromAccount = new Account();
        fromAccount.setBalance(BigDecimal.valueOf(100));
        Mockito
                .doReturn(Optional.of(fromAccount))
                .when(accountRepository)
                .findById(1L);

        Account toAccount = new Account();
        toAccount.setBalance(BigDecimal.valueOf(200));
        Mockito
                .doReturn(Optional.of(toAccount))
                .when(accountRepository)
                .findById(2L);

        accountService.transfer(transaction);

        assertEquals(BigDecimal.valueOf(50), fromAccount.getBalance());
        assertEquals(BigDecimal.valueOf(250), toAccount.getBalance());

        Mockito
                .verify(transactionService)
                .save(transaction);

        Mockito
                .verify(entityManager)
                .lock(fromAccount, LockModeType.PESSIMISTIC_WRITE);

        Mockito
                .verify(entityManager)
                .lock(toAccount, LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void transfer_DeveLancarExcecaoQuandoContasOrigemEDestinoSaoIguais() {
        Transaction transaction = new Transaction();
        transaction.setFromAccountId(1L);
        transaction.setToAccountId(1L);

        Exception exception = assertThrows(
                IllegalArgumentException.class, () -> accountService.transfer(transaction));


        assertEquals("Conta de origem e destino devem ser diferentes", exception.getMessage());

        Mockito
                .verify(transactionService, never())
                .save(Mockito.any(Transaction.class));

        Mockito
                .verify(entityManager, never())
                .lock(Mockito.any(Account.class), Mockito.any(LockModeType.class));

        Mockito
                .verify(accountRepository, never())
                .save(Mockito.any(Account.class));
    }

    @Test
    void findAccountByUsernameAndPassword_DeveRetornarContaQuandoCredenciaisValidas() {
        Account account = new Account();
        Mockito
                .doReturn(account)
                .when(accountRepository)
                .findByUsernameAndPassword("user", "pass");

        Account foundAccount = accountService.findAccountByUsernameAndPassword("user", "pass");

        assertEquals(account, foundAccount);
    }

    @Test
    void findAccountByUsername_DeveRetornarVerdadeiroQuandoUsuarioExiste() {
        Mockito
                .doReturn(new Account())
                .when(accountRepository)
                .findByUsername("user");

        assertTrue(accountService.findAccountByUsername("user"));
    }

    @Test
    void findAccountByUsername_DeveRetornarFalsoQuandoUsuarioNaoExiste() {
        Mockito
                .doReturn(null)
                .when(accountRepository)
                .findByUsername("user");

        assertFalse(accountService.findAccountByUsername("user"));
    }
}
