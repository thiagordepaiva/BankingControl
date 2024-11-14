package com.bankingcontrol.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /*http
                .authorizeRequests()
                    .antMatchers("/createAccount").permitAll()
                    .antMatchers("/login").permitAll()
                    .anyRequest().authenticated()
                .and()
                .formLogin()
                    .loginPage("/login").permitAll()
                .and()
                    .logout().permitAll();*/
        http
                .authorizeRequests()
                .anyRequest().permitAll() // Permite acesso a todas as URLs sem autenticação
                .and()
                .csrf().disable();
    }
}
