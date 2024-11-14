package com.bankingcontrol.model;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    private String password;

    @Transient
    private String confirmPassword;

    private BigDecimal balance = BigDecimal.ZERO;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotEmpty(message = "O nome de usuário é obrigatório") String getUsername() {
        return username;
    }

    public void setUsername(@NotEmpty(message = "O nome de usuário é obrigatório") String username) {
        this.username = username;
    }

    public @NotEmpty(message = "A senha é obrigatória") @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres") String getPassword() {
        return password;
    }

    public void setPassword(@NotEmpty(message = "A senha é obrigatória") @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres") String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
