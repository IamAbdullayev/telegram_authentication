package com.tg.authtelegram.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.tg.authtelegram.model.TelegramUser;
import com.tg.authtelegram.repository.TelegramUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class TelegramAuthService {
    private final TelegramUserRepository telegramUserRepository;
    @Value("${telegram.bot.token}")
    private String botToken;

    /* Validating data for Third-Party Use

    * To validate data received via the Mini App, one should send the data from the Telegram.WebApp.
    * initData field to the bot's backend. The data is a query string, which is composed of a series of field-value pairs.
    *
    * You can verify the integrity of the data received by comparing the received hash parameter with the hexadecimal
    * representation of the HMAC-SHA-256 signature of the data-check-string with the secret key, which is the HMAC-SHA-256 signature of the bot's token with the constant string WebAppData used as a key.
    *
    * Data-check-string is a chain of all received fields, sorted alphabetically, in the format key=<value> with a
    * line feed character ('\n', 0x0A) used as separator – e.g., 'auth_date=<auth_date>\nquery_id=<query_id>\nuser=<user>'.
    *
    * (https://core.telegram.org/bots/webapps#validating-data-received-via-the-web-app)
    * */

    // Основной метод для проверки валидности initData (полученный от бота)
    public Map<String, String> parseAndValidateInitData(String initData) {
        // Преобразование в json
        // Удаляем ключ "hash" со значением (except hash ...)
        // Получаем строку, которую используем для проверки валидности initData
        // Проверяем на валидность

        Map<String, String> dataMap = parseInitDate(initData); // если initData строка (т.е не преобразована в json)

        log.info("dataMap: {}", dataMap);

        String hash = dataMap.remove("hash");
        String checkString = buildCheckString(dataMap);
        if (!verifyTelegramSignature(botToken, checkString, hash)) {
            throw new SecurityException("Invalid Telegram initData...");
        }
        return dataMap;
    }

    // Превращаем строку в форму { "ключ", "значение" }
    private Map<String, String> parseInitDate(String initData) {
        // Разделяем на части в формате - "ключ", "значение"
        // Кладем их в map (расшифруем полученный ключи и значения (так как мы передали его в шифрованном виде))
        // В итоге получаем множество состоящее из пар ключ-значение

        Map<String, String> map = new HashMap<>();
        if (initData == null || initData.isEmpty()) return map;

        String[] parts = initData.split("&");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                map.put(key, value);
            }
        }
        return map;
    }

    // Вспомогательный метод по созданию необхожимой строки для проверки на валидность
    private String buildCheckString(Map<String, String> data) {
        // Условие - sorted alphabetically, in the format key=<value>.
        // Приведение из формата ключ-значение в строку "ключ=значение"
        // Добавляем в конце каждой строки "\n"
        // В противном случае возвращаем пустую строку

        return data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

    // Всп. метод проверки валидности
    private boolean verifyTelegramSignature(String botToken, String checkString, String receivedHash) {
        // Создаем Secret Key Specification с WebAppData и ключем botToken
        // Создаем объект Mac для работы с алгоритмом HMAC-SHA256
        // В объект Mac передаем keySpec как ключ
        // Вычисляет HmacSHA256 от receivedHash и возвращает результат в виде массива байтов (что по сути является hash)
        // Переводим байт код в hex (шестнадцатеричный), так как телеграм предоставляет нам hash в таком формате

        try {
            // 1. Вычисляем secret_key = HMAC_SHA256(botToken, "WebAppData")
            // 2. Вычисляем HMAC_SHA256(data_check_string, secret_key)

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpecForSecret = new SecretKeySpec("WebAppData".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpecForSecret);
            byte[] secretKey = mac.doFinal(botToken.getBytes(StandardCharsets.UTF_8));

            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
            byte[] hmacBytes = mac.doFinal(checkString.getBytes(StandardCharsets.UTF_8));

            // 3. Преобразуем результат в hex
            // 4. Сравниваем с полученным hash (без учёта регистра)
            String calculatedHash = bytesToHash(hmacBytes);
            log.info("myHash: {}", calculatedHash);
            boolean isValid = calculatedHash.equalsIgnoreCase(receivedHash);
            log.info("isValid: {}", isValid);
            return isValid;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String bytesToHash(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(String.format("%02x", b));
        }
        return stringBuilder.toString();
    }

    // Метод для сохранения в базу данных (елсли его нет там)
    public TelegramUser saveUserIfNotExists(Map<String, String> userData) {
        String userJson = userData.get("user");
        if (userJson == null) {
            throw new IllegalArgumentException("User data is missing");
        }
        log.info("userJson (before mapping): {}", userJson);

        ObjectMapper mapper = new ObjectMapper();
        TelegramUser user;
        try {
            user = mapper.readValue(userJson, TelegramUser.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse user JSON", e);
        }

        Long id = user.getId();
        return telegramUserRepository.findById(id)
                .orElseGet(() -> telegramUserRepository.save(user));
    }


        // ИСПОЛЬЗОВАТЬ в место предыдущей при TELEGRAM LOGIN WIDGET)
//    public TelegramUser saveUserIfNotExists(Map<String, String> userData) {
//        String telegramId = userData.get("id");
//        if (telegramId == null) {
//            throw new IllegalArgumentException("Telegram ID пропущен!");
//        }
//
//        Long id = Long.parseLong(userData.get("id"));
//
//        return telegramUserRepository.findById(id)
//                .orElseGet(() -> {
//                    TelegramUser user = new TelegramUser(
//                            id,
//                            userData.get("first_name"),
//                            userData.get("last_name"),
//                            userData.get("username"),
//                            userData.get("photo_url")
//                    );
//                    return telegramUserRepository.save(user);
//                });
//    }
}
