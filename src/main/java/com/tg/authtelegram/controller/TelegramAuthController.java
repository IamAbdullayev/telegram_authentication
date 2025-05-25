package com.tg.authtelegram.controller;

import com.tg.authtelegram.model.TelegramUser;
import com.tg.authtelegram.service.TelegramAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class TelegramAuthController {
    private final TelegramAuthService telegramAuthService;

    @PostMapping("/telegram")
    public ResponseEntity<TelegramUser> authenticateWithTelegram(@RequestParam String initData) {
        try {
            Map<String, String> userData = telegramAuthService.parseAndValidateInitData(initData);
            TelegramUser user = telegramAuthService.saveUserIfNotExists(userData);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
