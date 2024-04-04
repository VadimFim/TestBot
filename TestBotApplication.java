package com.example.TestBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

@Component
@SpringBootApplication
public class TestBotApplication extends TelegramLongPollingBot {

	private final Map<Integer, String> cities = Map.of(
			1, "Львів",
			2, "Київ",
			3, "Одеса",
			4, "Харків",
			5, "Рівне"
	);

	private final Map<String, Map<Integer, String>> cityTrains = new HashMap<>();
	{
		Map<Integer, String> lvivTrains = new HashMap<>();
		lvivTrains.put(1, "Львівський експрес");
		lvivTrains.put(2, "Львів-Київ");
		cityTrains.put("Львів", lvivTrains);

		Map<Integer, String> kyivTrains = new HashMap<>();
		kyivTrains.put(1, "Київський експрес");
		kyivTrains.put(2, "Київ-Львів");
		cityTrains.put("Київ", kyivTrains);

		Map<Integer, String> odesaTrains = new HashMap<>();
		odesaTrains.put(1, "Київський експрес");
		odesaTrains.put(2, "Київ-Львів");
		cityTrains.put("Київ", odesaTrains);

		Map<Integer, String> harkivTrains = new HashMap<>();
		harkivTrains.put(1, "Київський експрес");
		harkivTrains.put(2, "Київ-Львів");
		cityTrains.put("Київ", harkivTrains);

		Map<Integer, String> rivneTrains = new HashMap<>();
		rivneTrains.put(1, "Київський експрес");
		rivneTrains.put(2, "Київ-Львів");
		cityTrains.put("Київ", rivneTrains);
	}

	private final Map<Long, String> userCityChoice = new HashMap<>();
	private final Map<Long, Integer> userPreviousTrainChoice = new HashMap<>();
	private final Map<Long, Integer> userTicketChoice = new HashMap<>();
	private final int totalSeats = 60; // Загальна кількість місць
	private final int ticketPrice = 100; // Ціна квитка

	public static void main(String[] args) {
		SpringApplication.run(TestBotApplication.class, args);
	}

	@Override
	public String getBotUsername() {
		return "VadZag_bot";
	}

	@Override
	public String getBotToken() {
		return "7089151092:AAFdz-0OLvZ4FnRi3-0npU7LBkrsRL1QgBk";
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			String messageText = update.getMessage().getText();
			long chatId = update.getMessage().getChatId();

			if ("/start".equals(messageText)) {
				userCityChoice.put(chatId, null); // Попередній вибір міста.
				userPreviousTrainChoice.put(chatId, null); // Повередній вибір потягу.
				sendMsg(chatId, "Виберіть місто:");
				for (Integer cityNumber : cities.keySet()) {
					sendMsg(chatId, cityNumber + ". " + cities.get(cityNumber));
				}
			} else if ("/back".equals(messageText)) {
				handleBackButton(chatId);
			} else {
				processUserChoice(chatId, messageText);
			}
		}
	}

	private void processUserChoice(long chatId, String messageText) {
		try {
			int choice = Integer.parseInt(messageText);
			if (userCityChoice.containsKey(chatId)) {
				if (userCityChoice.get(chatId) == null) { // Вибір міста
					if (cities.containsKey(choice)) {
						userCityChoice.put(chatId, cities.get(choice)); // Збереження інформації про вибір міста.
						userPreviousTrainChoice.put(chatId, null); // Збереження інформації про вибір потягу.
						sendMsg(chatId, "Виберіть потяг у місті " + userCityChoice.get(chatId) + ":");
						Map<Integer, String> cityTrainsMap = cityTrains.get(userCityChoice.get(chatId));
						if (cityTrainsMap != null) {
							for (Integer trainNumber : cityTrainsMap.keySet()) {
								sendMsg(chatId, trainNumber + ". " + cityTrainsMap.get(trainNumber));
							}
						} else {
							sendMsg(chatId, "Для цього міста немає потягів.");
						}
					} else {
						sendMsg(chatId, "Невірний вибір міста. Спробуйте ще раз.");
					}
				} else { // Вибір потягу
					handleTrainSelection(chatId, choice);
				}
			}
		} catch (NumberFormatException e) {
			sendMsg(chatId, "Невірний вибір. Будь ласка, введіть номер міста або потягу.");
		}
	}

	private void handleTrainSelection(long chatId, int choice) {
		String selectedCity = userCityChoice.get(chatId);
		Map<Integer, String> cityTrainsMap = cityTrains.get(selectedCity);
		if (cityTrainsMap != null && cityTrainsMap.containsKey(choice)) {
			userPreviousTrainChoice.put(chatId, choice);
			int availableSeats = totalSeats - userTicketChoice.getOrDefault(chatId, 0);
			sendMsg(chatId, "Місто: " + selectedCity + "\nПотяг: " + cityTrainsMap.get(choice) + "\nВільні місця: " + availableSeats + "\nВартість одного місця: " + ticketPrice + " грн.");
			// Логіка
		} else {
			sendMsg(chatId, "Невірний вибір потягу. Спробуйте ще раз.");
		}
	}

	private void handleBackButton(long chatId) {
		if (userPreviousTrainChoice.containsKey(chatId) && userCityChoice.containsKey(chatId)) {
			userPreviousTrainChoice.put(chatId, null);
			sendMsg(chatId, "Виберіть місто:");
			for (Integer cityNumber : cities.keySet()) {
				sendMsg(chatId, cityNumber + ". " + cities.get(cityNumber));
			}
		} else {
			sendMsg(chatId, "Немає попереднього вибору.");
		}
	}

	private void sendMsg(long chatId, String message) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(String.valueOf(chatId));
		sendMessage.setText(message);

		try {
			execute(sendMessage);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}
