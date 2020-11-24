package telegram;

import com.vavilov.yandex.model.Voice;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendAudio;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

@Slf4j
public class Bot extends TelegramLongPollingBot {

    private final Map<Integer, Instant> coolingDownMap = new HashMap<>();

    private static final int MSG_THRESHOLD_CHAR = 30;
    private static final int COOL_DOWN_SEC = 10;

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotApi = new TelegramBotsApi();
        try {
            telegramBotApi.registerBot(new Bot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    int i;
    public synchronized void sendMsg(long chatId, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(s);
        if (i++ == 0) {
            setButtons(sendMessage);
        }
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public synchronized void sendAudio(@NonNull long chatId, @NonNull ByteArrayInputStream inputStream) {
        try {
            SendAudio sendAudio = new SendAudio();
            sendAudio.setTitle("Title");
            sendAudio.setCaption("Caption");
            sendAudio.setChatId(chatId);
            sendAudio.setNewAudio("VoiceSynthesis", inputStream);
            sendAudio(sendAudio);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }


    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        User userFrom = message.getFrom();

        if (message.getText().length() > MSG_THRESHOLD_CHAR) {
            sendMsg(message.getChatId(), format("Message is grater then %s characters", MSG_THRESHOLD_CHAR));
            return;
        }
        if (!coolingDownMap.getOrDefault(userFrom.getId(), Instant.MIN)
                .isBefore(Instant.now().minusSeconds(COOL_DOWN_SEC))) {
            sendMsg(message.getChatId(), format("Cool down time is %s seconds!", COOL_DOWN_SEC));
            return;
        }
        coolingDownMap.put(userFrom.getId(), Instant.now());

        sendMsg(message.getChatId(), "Something");
//        try {
//            sendAudio(message.getChatId(), YandexVoiceSynthesisService.getInstance().synthesizeVoice(
//                    message.getText(), Language.RUSSIAN, Voice.ALENA));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public synchronized void setButtons(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        KeyboardRow keyboardThirdRow = new KeyboardRow();

        keyboardFirstRow.add(new KeyboardButton(Voice.ALENA.getName()));
        keyboardFirstRow.add(new KeyboardButton(Voice.ALYSS.getName()));
        keyboardFirstRow.add(new KeyboardButton(Voice.ERMIL.getName()));

        keyboardSecondRow.add(new KeyboardButton(Voice.FILIPP.getName()));
        keyboardSecondRow.add(new KeyboardButton(Voice.JANE.getName()));
        keyboardSecondRow.add(new KeyboardButton(Voice.NICK.getName()));

        keyboardThirdRow.add(new KeyboardButton(Voice.OKSANA.getName()));
        keyboardThirdRow.add(new KeyboardButton(Voice.OMAZH.getName()));
        keyboardThirdRow.add(new KeyboardButton(Voice.ZAHAR.getName()));

        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        keyboard.add(keyboardThirdRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    @Override
    public String getBotUsername() {
        return "VoiceBot";
    }

    @Override
    public String getBotToken() {
        return "1351517384:AAFeUN798dLvcQ9EivszIEt7ml8w1MTgii0";
    }

    @Override
    public void onClosing() {
        System.out.println("CLOSING");
    }
}