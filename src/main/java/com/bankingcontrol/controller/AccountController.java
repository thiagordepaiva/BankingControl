package com.bankingcontrol.controller;

import com.bankingcontrol.model.Account;
import com.bankingcontrol.model.Transaction;
import com.bankingcontrol.service.AccountService;
import com.bankingcontrol.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("account", new Account());

        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        Account account = accountService.findAccountByUsernameAndPassword(username, password);

        if (account != null) {
            handleReturnAccountDetails(model, account);

            return "accountDetails";
        } else {
            Account accountReturn = new Account();
            accountReturn.setUsername(username);
            accountReturn.setPassword(password);

            model.addAttribute("error", "Usuário ou senha inválidos.");
            model.addAttribute("account", accountReturn);

            return "login";
        }
    }

    @GetMapping("/createAccount")
    public String showCreateAccountPage(Model model) {
        model.addAttribute("account", new Account());

        return "createAccount";
    }

    @PostMapping("/createAccount")
    public String createAccount(@Valid Account account, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "createAccount";
        }

        account = accountService.createAccount(account, model);
        if (account == null) return "createAccount";

        model.addAttribute("accountNumber", account.getId());

        return "createAccount";
    }

    @GetMapping("/accountDetails")
    public String viewAccountDetails() {
        return "accountDetails";
    }

    @PostMapping("/deposit")
    public String deposit(@ModelAttribute Transaction transaction, Model model) {
        try {
            Account account = accountService.deposit(transaction);

            handleReturnAccountDetails(model, account);

            return "accountDetails";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            handleReturnAccountDetails(model, accountService.getAccount(transaction.getFromAccountId()));

            return "accountDetails";
        }
    }

    @PostMapping("/withdraw")
    public String withdraw(@ModelAttribute Transaction transaction, Model model) {
        try {
            Account account = accountService.withdraw(transaction);

            handleReturnAccountDetails(model, account);

            return "accountDetails";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            handleReturnAccountDetails(model, accountService.getAccount(transaction.getFromAccountId()));

            return "accountDetails";
        }
    }

    @PostMapping("/transfer")
    public String transfer(@ModelAttribute Transaction transaction, Model model) {
        try {
            Account account = accountService.transfer(transaction);

            handleReturnAccountDetails(model, account);

            return "accountDetails";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            handleReturnAccountDetails(model, accountService.getAccount(transaction.getFromAccountId()));

            return "accountDetails";
        }
    }

    private void handleReturnAccountDetails(Model model, Account account) {
        model.addAttribute("account", account);
        model.addAttribute("transaction", new Transaction());
        model.addAttribute("transactions", transactionService.getTransactions(account));
    }
}
