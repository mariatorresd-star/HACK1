package com.example.hack1base.JWT.web;

import com.example.hack1base.JWT.domain.Account;
import lombok.Getter;

@Getter
public class AccountDto {
    private Long id;
    private String email;
    private String role;
    private String branch;
    private String createdAt;

    public static AccountDto from(Account a) {
        AccountDto d = new AccountDto();
        d.id = a.getId();
        d.email = a.getEmail();
        d.role = a.getRole();
        d.branch = a.getBranch();
        d.createdAt = a.getCreatedAt().toString();
        return d;
    }
}
