package com.tg.authtelegram.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class TelegramUser {
    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String photoUrl;
}