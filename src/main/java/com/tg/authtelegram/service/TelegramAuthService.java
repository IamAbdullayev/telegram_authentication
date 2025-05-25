package com.tg.authtelegram.service;

import com.tg.authtelegram.model.TelegramUser;
import com.tg.authtelegram.repository.TelegramUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class TelegramAuthService {
    private final TelegramUserRepository telegramUserRepository;
    @Value("${telegram.bot.token}")
    private String botToken;

    /* Validating data for Third-Party Use
    * 1. Prepend the bot_id, followed by : and the constant string WebAppData.
    * 2. Add a line feed character ('\n', 0x0A).
    * 3. Append all received fields (except hash and signature), sorted alphabetically, in the format key=<value>.
    * 4. Separate each key-value pair with a line feed character ('\n', 0x0A).
    *
    * (https://core.telegram.org/bots/webapps#validating-data-received-via-the-web-app)
    *
    * */

    // Основной метод для проверки валидности initData (полученный от бота)
    public Map<String, String> parseAndValidateInitData(String initData) {
        // Парсинг...
        // Удаляем ключ "hash" со значением (except hash ...)
        // Получаем строку, которую используем для проверки валидности initData
        // Проверяем на валидность

        Map<String, String> dataMap = parseInitDate(initData);
        String hash = dataMap.remove("hash");
        String checkString = buildCheckString(dataMap);
        if (!verifyTelegramSignature(botToken, checkString, hash)) {
            throw new SecurityException("Invalid Telegram initData...");
        }
        return dataMap;
    }

    // Превращаем строку в форму { "ключ", "значение" }
    private Map<String, String> parseInitDate(String initData) {
        // Расшифруем полученный initData (так как мы передали его в шифрованном виде)
        // Разделяем на части в формате - "ключ", "значение"
        // Кладем их в map
        // В итоге получаем множество состоящее из пар ключ-значение

        Map<String, String> map = new HashMap<>();
        String decodedInitData = URLDecoder.decode(initData, StandardCharsets.UTF_8);
        String[] parts = decodedInitData.split("&");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
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
        // Создаем Secret Key Specification с ключем botToken
        // Создаем объект Mac для работы с алгоритмом HMAC-SHA256
        // В объект Mac передаем keySpec как ключ
        // Вычисляет HmacSHA256 от receivedHash и возвращает результат в виде массива байтов (что по сути является hash)
        // Переводим байт код в hex (шестнадцатеричный), так как телеграм предоставляет нам hash в таком формате

        try {
            SecretKeySpec keySpec = new SecretKeySpec(botToken.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            byte[] hmacBytes = mac.doFinal(checkString.getBytes(StandardCharsets.UTF_8));
            String calculatedHash = bytesToHash(hmacBytes);
            return calculatedHash.equalsIgnoreCase(receivedHash);
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
        String telegramId = userData.get("id");
        if (telegramId == null) {
            throw new IllegalArgumentException("Telegram ID пропущен!");
        }

        Long id = Long.parseLong(userData.get("id"));

        return telegramUserRepository.findById(id)
                .orElseGet(() -> {
                    TelegramUser user = new TelegramUser(
                            id,
                            userData.get("first_name"),
                            userData.get("last_name"),
                            userData.get("username"),
                            userData.get("photo_url")
                    );
                    return telegramUserRepository.save(user);
                });
    }
}
