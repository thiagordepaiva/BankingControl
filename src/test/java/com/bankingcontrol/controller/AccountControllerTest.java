package com.bankingcontrol.controller;

import com.bankingcontrol.model.Account;
import com.bankingcontrol.model.Transaction;
import com.bankingcontrol.service.AccountService;
import com.bankingcontrol.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loginPage_deveAdicionarAtributoContaAoAcesseTelaLogin() {
        String viewName = accountController.loginPage(model);

        Mockito
                .verify(model)
                .addAttribute(eq("account"), any(Account.class));

        assertEquals("login", viewName);
    }

    @Test
    void login_DeveRetornarContaQuandoLoginAutenticadoComSucesso() {
        String username = "user";
        String password = "pass";
        Account account = new Account();
        Mockito
                .doReturn(account)
                .when(accountService)
                .findAccountByUsernameAndPassword(username, password);

        String viewName = accountController.login(username, password, model);

        Mockito
                .verify(model)
                .addAttribute("account", account);

        assertEquals("accountDetails", viewName);
    }

    @Test
    void login_DeveRetornarErroQuandoLoginFalhaAoAutenticar() {
        String username = "user";
        String password = "wrongPass";

        Mockito
                .doReturn(null)
                .when(accountService)
                .findAccountByUsernameAndPassword(username, password);

        String viewName = accountController.login(username, password, model);

        Mockito
                .verify(model)
                .addAttribute("error", "Usuário ou senha inválidos.");

        Mockito
                .verify(model)
                .addAttribute(eq("account"), any(Account.class));

        assertEquals("login", viewName);
    }

    @Test
    void showCreateAccountPage_deveAdicionarAtributoContaAoAcessaTelaCriarConta() {
        String viewName = accountController.showCreateAccountPage(model);

        Mockito
                .verify(model)
                .addAttribute(eq("account"), any(Account.class));

        assertEquals("createAccount", viewName);
    }

    @Test
    void createAccount_DeveRetornarErroEmanterViewCreateAccountQuandoAcontecerErros() {
        Mockito
                .when(bindingResult.hasErrors())
                .thenReturn(true);

        String viewName = accountController.createAccount(new Account(), bindingResult, model);

        assertEquals("createAccount", viewName);
    }

    @Test
    void createAccount_DeveRetornarNumeroContaQuandoContaCriadaComSucesso() {
        Mockito
                .doReturn(false)
                .when(bindingResult)
                .hasErrors();

        Account account = new Account();
        account.setId(123L);
        Mockito
                .doReturn(account)
                .when(accountService)
                .createAccount(any(Account.class), any(Model.class));

        String viewName = accountController.createAccount(account, bindingResult, model);

        Mockito
                .verify(model)
                .addAttribute("accountNumber", 123L);

        assertEquals("createAccount", viewName);
    }

    @Test
    void viewAccountDetails_deveRetornarViewAccountDetails() {
        String viewName = accountController.viewAccountDetails();

        assertEquals("accountDetails", viewName);
    }

    @Test
    void deposit_DeveAdicionarContaESeusDetalhesAoRealizarDepositoComSucesso() {
        Transaction transaction = new Transaction();
        Account account = new Account();
        Mockito
                .doReturn(account)
                .when(accountService)
                .deposit(transaction);

        String viewName = accountController.deposit(transaction, model);

        Mockito
                .verify(model)
                .addAttribute("account", account);

        Mockito
                .verify(model)
                .addAttribute(eq("transaction"), any(Transaction.class));

        Mockito
                .verify(model)
                .addAttribute("transactions", Collections.emptyList());

        assertEquals("accountDetails", viewName);
    }

    @Test
    void deposit_DeveAdicionarContaESeusDetalhesAoAcontecerExcecaoDuranteDeposito() {
        Transaction transaction = new Transaction();
        Account account = new Account();
        Mockito
                .when(accountService.deposit(transaction))
                .thenThrow(new IllegalArgumentException("Erro ao depositar"));
        Mockito
                .doReturn(account)
                .when(accountService)
                .getAccount(transaction.getFromAccountId());

        String viewName = accountController.deposit(transaction, model);

        Mockito
                .verify(model)
                .addAttribute("error", "Erro ao depositar");

        Mockito
                .verify(model)
                .addAttribute("account", account);

        Mockito
                .verify(model)
                .addAttribute(eq("transaction"), any(Transaction.class));

        Mockito
                .verify(model)
                .addAttribute("transactions", Collections.emptyList());

        assertEquals("accountDetails", viewName);
    }

    @Test
    void withdraw_DeveAdicionarContaESeusDetalhesAoRealizarSaqueComSucesso() {
        Transaction transaction = new Transaction();
        Account account = new Account();
        Mockito
                .doReturn(account)
                .when(accountService)
                .withdraw(transaction);

        String viewName = accountController.withdraw(transaction, model);

        Mockito
                .verify(model)
                .addAttribute("account", account);

        Mockito
                .verify(model)
                .addAttribute(eq("transaction"), any(Transaction.class));

        Mockito
                .verify(model)
                .addAttribute("transactions", Collections.emptyList());

        assertEquals("accountDetails", viewName);
    }

    @Test
    void withdraw_DeveAdicionarContaESeusDetalhesAoAcontecerExcecaoDuranteSaque() {
        Transaction transaction = new Transaction();
        Account account = new Account();
        Mockito
                .when(accountService.withdraw(transaction))
                .thenThrow(new IllegalArgumentException("Erro ao sacar"));
        Mockito
                .doReturn(account)
                .when(accountService)
                .getAccount(transaction.getFromAccountId());

        String viewName = accountController.withdraw(transaction, model);

        Mockito
                .verify(model)
                .addAttribute("error", "Erro ao sacar");

        Mockito
                .verify(model)
                .addAttribute("account", account);

        Mockito
                .verify(model)
                .addAttribute(eq("transaction"), any(Transaction.class));

        Mockito
                .verify(model)
                .addAttribute("transactions", Collections.emptyList());

        assertEquals("accountDetails", viewName);
    }

    @Test
    void transfer_DeveAdicionarContaESeusDetalhesAoRealizarTransferenciaComSucesso() {
        Transaction transaction = new Transaction();
        Account account = new Account();
        when(accountService.transfer(transaction)).thenReturn(account);

        String viewName = accountController.transfer(transaction, model);

        Mockito
                .verify(model)
                .addAttribute("account", account);

        Mockito
                .verify(model)
                .addAttribute(eq("transaction"), any(Transaction.class));

        Mockito
                .verify(model)
                .addAttribute("transactions", Collections.emptyList());

        assertEquals("accountDetails", viewName);
    }

    @Test
    void transfer_DeveAdicionarContaESeusDetalhesAoAcontecerExcecaoDuranteTransFerencia() {
        Transaction transaction = new Transaction();
        Account account = new Account();
        Mockito
                .when(accountService.transfer(transaction))
                .thenThrow(new IllegalArgumentException("Erro ao transferir"));
        Mockito
                .doReturn(account)
                .when(accountService)
                .getAccount(transaction.getFromAccountId());

        String viewName = accountController.transfer(transaction, model);

        Mockito
                .verify(model)
                .addAttribute("error", "Erro ao transferir");

        Mockito
                .verify(model)
                .addAttribute("account", account);

        Mockito
                .verify(model)
                .addAttribute(eq("transaction"), any(Transaction.class));

        Mockito
                .verify(model)
                .addAttribute("transactions", Collections.emptyList());

        assertEquals("accountDetails", viewName);
    }
}
