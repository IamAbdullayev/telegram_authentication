package com.tg.authtelegram.controller;

import com.tg.authtelegram.model.TelegramUser;
import com.tg.authtelegram.service.TelegramAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class TelegramAuthController {
    private final TelegramAuthService telegramAuthService;
    @PostMapping("/auth/telegram")
    public ResponseEntity<?> authenticateWithTelegram(@RequestParam("initData") String initData) {
        try {
            log.info(">> Получено initData: {}", initData);
            Map<String, String> userData = telegramAuthService.parseAndValidateInitData(initData);
            TelegramUser user = telegramAuthService.saveUserIfNotExists(userData);

            log.info("Response (user): {}", user);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", user.getId(),
                    "first_name", user.getFirstName(),
                    "last_name", user.getLastName(),
                    "username", user.getUsername()
            ));
        } catch (SecurityException e) {
            log.error("SecurityException: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Ошибка безопасности: недействительные данные"
            ));
        } catch (RuntimeException e) {
            log.error("RuntimeException: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Ошибка сервера"
            ));
        } catch (Exception e) {
            log.error("Ошибка при аутентификации: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Ошибка запроса: неверные данные"
            ));
        }
    }
}
