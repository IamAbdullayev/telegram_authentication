package com.tg.authtelegram.bot;

import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;


public class MyTelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.token}")
    private  String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                SendMessage message = createWebAppButton(chatId);
                try {
                    execute(message);  // отправляем сообщение
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public SendMessage createWebAppButton(long chatId) {
        // Создаем кнопку с WebApp
        InlineKeyboardButton webAppButton = new InlineKeyboardButton();
        webAppButton.setText("Войти через Telegram");

        // Указываем URL WebApp
        // Это URL должен быть HTTPS и вести на твое приложение
        webAppButton.setWebApp(new WebAppInfo("https://7b30-37-61-120-53.ngrok-free.app"));

        // Клавиатура с одной кнопкой
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        keyboardButtonsRow.add(webAppButton);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);

        // Создаем сообщение с клавиатурой
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Нажмите кнопку ниже для входа через Telegram WebApp:");
        message.setReplyMarkup(inlineKeyboardMarkup);

        return message;
    }
}
