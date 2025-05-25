package com.tg.authtelegram.controller;

import com.tg.authtelegram.model.TelegramUser;
import com.tg.authtelegram.service.TelegramAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class TelegramAuthController {
    private final TelegramAuthService telegramAuthService;
    @PostMapping("/telegram")
    public ResponseEntity<TelegramUser> authenticateWithTelegram(@RequestParam String initData) {
        try {
            log.info(">> Получено initData: {}", initData);
            Map<String, String> userData = telegramAuthService.parseAndValidateInitData(initData);
            TelegramUser user = telegramAuthService.saveUserIfNotExists(userData);
            return ResponseEntity.ok(user);
        } catch (SecurityException e) {
            log.error("SecurityException: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        catch (RuntimeException e) {
            log.error("RuntimeException: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            log.error("Ошибка при аутентификации: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
