package io.proj3ct.HabitTestingBot.service;

import io.proj3ct.HabitTestingBot.config.BotConfig;
import io.proj3ct.HabitTestingBot.dataType.ActivityState;
import io.proj3ct.HabitTestingBot.domain.*;
import io.proj3ct.HabitTestingBot.dto.UserRequest;
import io.proj3ct.HabitTestingBot.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.mail.MessagingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private final UserService userService;
    @Autowired
    private final UserRepository userRepository;

    private final GroupService groupService;
    @Autowired
    private final GroupRepository groupRepository;
    private final TestService testService;
    @Autowired
    private final TestRepository testRepository;
    private final QuestionService questionService;
    @Autowired
    private final QuestionRepository questionRepository;
    private final AnswerService answerService;
    @Autowired
    private final AnswerRepository answerRepository;
    private final TestResultService testResultService;
    @Autowired
    private final TestResultRepository testResultRepository;
    private static final Map<Long, Group> group = new HashMap<>();
    private static final Map<Long, Test> test = new HashMap<>();

    private static final Map<Long, Question> question = new HashMap<>();
    private static final Map<Long, Integer> verificationCode = new HashMap<>();
    private static final Map<Long, Integer> numberOfAnswers = new HashMap<>();

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            switch (messageText) {
                case "/start":
                    startCommandReceived(update);
                    break;
                default:
                    switch (userService.getState(new UserRequest(update.getMessage().getChatId())).toString()) {
                        case ("USER_NAME_REQUIRED"):
                            userNameRequired(update);
                            break;
                        case ("PHONE_NUMBER_REQUIRED"):
                            phoneNumberRequired(update);
                            break;
                        case ("EMAIL_REQUIRED"):
                            emailRequired(update);
                            break;
                        case ("VERIFICATION_REQUIRED"):
                            verifyProfile(update);
                            break;
                        case ("LOGGED_IN"):
                            switch (messageText) {
                                case ("Мій профіль \uD83D\uDCDD"):
                                    sendProfile(update);
                                    break;
                                case ("Мої команди \uD83C\uDF10"):
                                    groupMain(update);
                                    break;

                            }
                            break;
                        case ("GROUP_NAME_REQUIRED"):
                            groupNameRequired(update);
                            break;
                        case ("GROUP_MENU"):
                            switch (messageText) {
                                case ("Учасники"):
                                    groupMembersList(update);
                                    break;
                                case ("Додати учасника"):
                                  addUser(update);
                                    break;
                                case ("Назад \uD83D\uDD19"):
                                    startCommandReceived(update);
                                    break;
                                case ("Видалити групу \uD83D\uDDD1"):
                                    deleteGroup(update);
                                    break;
                                case ("Список тестів"):
                                    groupTestsList(update);
                                    break;
                            }
                            break;
                        case ("ADD_USER_TO_GROUP"):
                            if (groupRepository.findById(userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId()).get().getOwner().getUserId().longValue() == update.getMessage().getChatId().longValue()) {
                                addUserToGroup(update);
                            }
                            break;
                        case ("TEST_NAME_REQUIRED"):
                          testNameRequired(update);
                        break;
                        case ("TEST_MENU"):
                            switch (messageText) {
                                case ("Редагувати тест ✏"):
                                    changeTest(update);
                                    break;
                                case ("Переглянути результати"):
                                   reviewResult(update);
                                    break;
                                case ("Назад \uD83D\uDD19"):
                                    backToGroupMenu(update);
                                    break;
                                case ("Видалити тест"):
                                    testService.deleteTest(userRepository.findById(update.getMessage().getChatId()).get().getSelectedTestId());
                                    sendMessage(update.getMessage().getChatId(), "Успішно видалено");
                                    backToGroupMenu(update);
                                    break;
                                case ("Призначити тест \uD83D\uDCC4"):
                                   assignTest(update);
                                    break;
                            }
                            break;
                        case ("QUESTION_REQUIRED"):
                          questionRequired(update);
                            break;
                        case ("ANSWER_REQUIRED"):
                            answerRequired(update);
                            break;

                    }


            }

//================================Callback==========================
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            switch (callbackData.split(":")[0]) {
                case ("CHANGE_PROFILE"):
                    changeProfile(update);
                    break;
                case("VERIFY_PROFILE"):
                    EditMessageText editMessageText=new EditMessageText();
                    editMessageText.setChatId(update.getCallbackQuery().getMessage().getChatId());
                    editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                    editMessageText.setText("Уведіть електронну почту для подальшої верифікаціїї");
                    try {
                        execute(editMessageText);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    userService.setState(new UserRequest(update.getCallbackQuery().getMessage().getChatId()),ActivityState.EMAIL_REQUIRED);
                    break;
                case ("CREATE_GROUP"):
                    createGroup(update);
                    break;
                case ("SELECT_GROUP"):
                    selectGroup(update);
                    break;
                case ("DELETE_USER_FROM_GROUP"):
                    if (groupRepository.findById(userRepository.findById(update.getCallbackQuery().getMessage().getChatId()).get().getSelectedGroupId()).get().getOwner().getUserId().longValue() == update.getCallbackQuery().getMessage().getChatId().longValue()) {
                        deleteUserFromGroup(update);
                    }
                    break;
                case ("CREATE_TEST"):
                    if (groupRepository.findById(Long.parseLong(update.getCallbackQuery().getData().split(":")[1])).get().getOwner().getUserId().longValue() == update.getCallbackQuery().getMessage().getChatId().longValue()) {
                        sendMessage(update.getCallbackQuery().getMessage().getChatId(), "Введіть назву для тесту");
                        userService.setState(new UserRequest(update.getCallbackQuery().getMessage().getChatId()), ActivityState.TEST_NAME_REQUIRED);
                        test.put(update.getCallbackQuery().getMessage().getChatId(),new Test());
                    }
                    break;
                case ("CHOOSE_TEST"):
                    chooseTest(update);
                    break;
                case ("GET_RESULT"):
                    getResult(update);
                    break;
                case ("CREATE_QUESTION"):
                    createQuestion(update);
                    break;
                case ("DELETE_QUESTION"):
                    deleteQuestionFunc(update);
                    break;
                case ("START_TEST"):
                    startTest(update);
                    break;
                case ("ANSWER"):
                    testInProgres(update);
                    break;
                case ("REQUEST_TO_ASSIGN"):
                    requestToAssign(update);
                    break;
                case ("APPROVE_ASSIGN"):
                    approveAssign(update);
                    break;
            }
        }
    }


    private void reviewResult(Update update) {
        List<TestResult> testResults=testResultService.getResultsByTest(testRepository.findById(userRepository.findById(update.getMessage().getChatId()).get().getSelectedTestId()).get());
        Workbook workbook = new XSSFWorkbook(); // Створення нового Excel документу
        Sheet sheet = workbook.createSheet("Результати"); // Створення нового листа
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("User ID");
        headerRow.createCell(1).setCellValue("User Name");
        headerRow.createCell(2).setCellValue("Score");
        int i=1;
        for (TestResult testResult:testResults)
        {
            Row dataRow = sheet.createRow(i);
            dataRow.createCell(0).setCellValue(testResult.getUser().getUserId());
            dataRow.createCell(1).setCellValue(testResult.getUser().getUserName());
            dataRow.createCell(2).setCellValue(testResult.getScore());
            i++;
        }
        try (FileOutputStream outputStream = new FileOutputStream("Test_result.xlsx")) {
            workbook.write(outputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(update.getMessage().getChatId());
        sendDocumentRequest.setDocument(new InputFile(new File("Test_result.xlsx")));
        sendDocumentRequest.setCaption("Ось ваші результати:");
        try {
            execute(sendDocumentRequest);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    //start==========================================================
    private void sendMessage(long chatId, String textToSend) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            {
                execute(message);
            }
        } catch (TelegramApiException e) {
            System.out.println("Error");
        }
    }

    private ReplyKeyboardMarkup mainBar() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row.add("Мій профіль \uD83D\uDCDD");
        row2.add("Мої команди \uD83C\uDF10");
        keyboardRows.add(row);
        keyboardRows.add(row2);
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    private void startCommandReceived(Update update) {
        if (userRepository.findById(update.getMessage().getChatId()).isEmpty()) {

            userService.save(new UserRequest(update.getMessage().getChatId()), "");
            userService.setState(new UserRequest(update.getMessage().getChatId()), ActivityState.USER_NAME_REQUIRED);
            sendMessage(update.getMessage().getChatId(), "Привіт, як вас звати?");
        } else {
            if (userRepository.findById(update.getMessage().getChatId()).get().isVerification()) {
                userService.setState(new UserRequest(update.getMessage().getChatId()), ActivityState.LOGGED_IN);
                String answer = "Вітаю, " + userService.getUserName(new UserRequest(update.getMessage().getChatId()));
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(update.getMessage().getChatId()));
                message.setText(answer);
                message.setReplyMarkup(mainBar());
                try {
                    {
                        execute(message);
                    }
                } catch (TelegramApiException e) {
                    System.out.println("Error");
                }
            }
            else
            {
                SendMessage sendMessage=new SendMessage();
                sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
                sendMessage.setText("Ви не верифіковані");
                sendMessage.setReplyMarkup(verifyProfile());
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    //profile========================================================

    private InlineKeyboardMarkup changeProfile() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var addButton = new InlineKeyboardButton();
        addButton.setText("Редагувати профіль");
        addButton.setCallbackData("CHANGE_PROFILE");
        rowInLine.add(addButton);
        rowsInLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }
    private InlineKeyboardMarkup verifyProfile() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var verifyButton = new InlineKeyboardButton();
        verifyButton.setText("Пройти верифікацію");
        verifyButton.setCallbackData("VERIFY_PROFILE");
        rowInLine.add(verifyButton);
        rowsInLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }
    private InlineKeyboardMarkup backToEmailRequest() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var backButton = new InlineKeyboardButton();
        backButton.setText("Назад \uD83D\uDD19");
        backButton.setCallbackData("VERIFY_PROFILE");
        rowInLine.add(backButton);
        rowsInLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    private void sendProfile(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(update.getMessage().getChatId()));
        message.setText("\uD83D\uDCDD\n" +
                "ID: `" + update.getMessage().getChatId() + "`\n" +
                "Ім'я: `" + userService.getUserName(new UserRequest(update.getMessage().getChatId())) + "`\n" +
                "Номер телефону: " + userService.getUserPhoneNumber(new UserRequest(update.getMessage().getChatId())) + "\n");
        message.setParseMode(ParseMode.MARKDOWN);
        message.setReplyMarkup(mainBar());
        message.setReplyMarkup(changeProfile());
        try {
            {
                execute(message);
            }
        } catch (TelegramApiException e) {
            System.out.println("Error");
        }
    }
    private void emailRequired(Update update) {
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        if (update.getMessage().getText().matches(emailPattern)) {
            userService.setUserEmail(new UserRequest(update.getMessage().getChatId()),update.getMessage().getText());
            verificationCode.put(update.getMessage().getChatId(), new Random().nextInt(8999)+1000);
            try {
                EmailSender.sendEmail(update.getMessage().getText(),"Підтвередження акаунта",userRepository.findById(update.getMessage().getChatId()).get().getUserName(),verificationCode.get(update.getMessage().getChatId()));
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
            SendMessage sendMessage=new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId());
            sendMessage.setText("Уведіть число відправлене на вашу електронну пошту");
            sendMessage.setReplyMarkup(backToEmailRequest());

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            userService.setState(new UserRequest(update.getMessage().getChatId()),ActivityState.VERIFICATION_REQUIRED);
        } else {
            sendMessage(update.getMessage().getChatId(), "Невідомий формат почти, спробуйте ще раз");
        }
    }
    private void verifyProfile(Update update) {
        if(Objects.equals(update.getMessage().getText(), String.valueOf(verificationCode.get(update.getMessage().getChatId()))))
        {
            userService.setUserVerificationStatus(new UserRequest(update.getMessage().getChatId()),true);
            sendMessage(update.getMessage().getChatId(),"Успішно верифіковано");
            startCommandReceived(update);
        }
        else
        {
            sendMessage(update.getMessage().getChatId(),"Введений вами код недійсний\nПеревірте правильність написання електронної почти та спробуйте щераз");
            startCommandReceived(update);
        }
    }
    private void changeProfile(Update update) {
        sendMessage(update.getCallbackQuery().getMessage().getChatId(), "Яке ваше ім'я?");
        userService.setState(new UserRequest(update.getCallbackQuery().getMessage().getChatId()), ActivityState.USER_NAME_REQUIRED);
        EditMessageText message = new EditMessageText();
        message.setChatId(update.getCallbackQuery().getMessage().getChatId());
        message.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        message.setText("Заповність інформацію:");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void userNameRequired(Update update) {
        userService.setUserName(new UserRequest(update.getMessage().getChatId()), update.getMessage().getText());
        userService.setState(new UserRequest(update.getMessage().getChatId()), ActivityState.PHONE_NUMBER_REQUIRED);
        sendMessage(update.getMessage().getChatId(), "Введіть ваш номер");
    }

    private void phoneNumberRequired(Update update) {
        String patterns
                = "^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$"
                + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?){2}\\d{3}$"
                + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?)(\\d{2}[ ]?){2}\\d{2}$";
        if (update.getMessage().getText().matches(patterns)) {
            userService.setUserPhoneNumber(new UserRequest(update.getMessage().getChatId()), update.getMessage().getText());
            if (userRepository.findById(update.getMessage().getChatId()).get().isVerification())
            {
                userService.setState(new UserRequest(update.getMessage().getChatId()), ActivityState.LOGGED_IN);
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(update.getMessage().getChatId()));
                message.setText("Успішно збережено");
                message.setReplyMarkup(mainBar());
                try {
                    {
                        execute(message);
                    }
                } catch (TelegramApiException e) {
                    System.out.println("Error");
                }
            }
            else
            {
                SendMessage sendMessage=new SendMessage();
                sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
                sendMessage.setText("Ви не верифіковані");
                sendMessage.setReplyMarkup(verifyProfile());
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }

        } else {
            sendMessage(update.getMessage().getChatId(), "Невідомий формат номера, спробуйте ще раз");
        }
    }

    //group===============================================================
    private InlineKeyboardMarkup createGroup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var addButton = new InlineKeyboardButton();
        addButton.setText("Створити групу ➕");
        addButton.setCallbackData("CREATE_GROUP");
        rowInLine.add(addButton);
        rowsInLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    private void createGroup(Update update)
    {
        sendMessage(update.getCallbackQuery().getMessage().getChatId(), "Як назвем групу?");
        userService.setState(new UserRequest(update.getCallbackQuery().getMessage().getChatId()), ActivityState.GROUP_NAME_REQUIRED);
        group.put(update.getCallbackQuery().getMessage().getChatId(),new Group());
    }
    private void addUser(Update update) {
        if (groupRepository.findById(userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId()).get().getOwner().getUserId().longValue() == update.getMessage().getChatId().longValue()) {
            sendMessage(update.getMessage().getChatId(), "Введіть ID користувача");
            userService.setState(new UserRequest(update.getMessage().getChatId()), ActivityState.ADD_USER_TO_GROUP);
        }
    }
    private void groupNameRequired(Update update) {
        group.get(update.getMessage().getChatId()).setName(update.getMessage().getText());
        groupService.save(new UserRequest(update.getMessage().getChatId()),  group.get(update.getMessage().getChatId()));
        sendMessage(update.getMessage().getChatId(), "Успішно створено");
        userService.setState(new UserRequest(update.getMessage().getChatId()), ActivityState.LOGGED_IN);
    }
    private void deleteGroup(Update update) {
        if (groupRepository.findById(userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId()).get().getOwner().getUserId().longValue() == update.getMessage().getChatId().longValue()) {
            List<Test> tests = testRepository.findByGroup(groupRepository.findById(userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId()).get());
            for (Test test : tests) {
                testResultService.deleteTestResultsForTest(test);
                testService.deleteTest(test.getTestId());
            }
            groupService.deleteGroupAndClearRelations(userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId());
            sendMessage(update.getMessage().getChatId(), "Видалено");
            startCommandReceived(update);
        }
    }
    private InlineKeyboardMarkup selectGroup(long groupId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var deleteButton = new InlineKeyboardButton();
        deleteButton.setText("Перейти до групи");
        deleteButton.setCallbackData("SELECT_GROUP:" + groupId);
        rowInLine.add(deleteButton);
        rowsInLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    private void groupMain(Update update) {
        List<Group> groups = groupService.getGroupByUser(update.getMessage().getChatId());
        if (groups.isEmpty()) {
            sendMessage(update.getMessage().getChatId(), "У вас немає груп");
        } else {
            sendMessage(update.getMessage().getChatId(), "Ваші групи:");
            for (Group group : groups) {
                String out = "• " + group.getName() + " \n";
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(update.getMessage().getChatId()));
                message.setText(out);
                message.setReplyMarkup(selectGroup(group.getGroupId()));
                try {
                    {
                        execute(message);
                    }
                } catch (TelegramApiException e) {
                    System.out.println("Error");
                }


            }
        }
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(update.getMessage().getChatId()));
        message.setText("Бажаєте створити групу?");
        message.setReplyMarkup(createGroup());
        try {
            {
                execute(message);
            }
        } catch (TelegramApiException e) {
            System.out.println("Error");
        }

    }

    private void selectGroup(Update update) {
        Long groupId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        userService.setUserSelectedGroupId(new UserRequest(update.getCallbackQuery().getMessage().getChatId()), groupId);
        userService.setState(new UserRequest(update.getCallbackQuery().getMessage().getChatId()), ActivityState.GROUP_MENU);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
        sendMessage.setText("Ви обрали групу " + groupRepository.findById(groupId).get().getName());
        if (groupRepository.findById(groupId).get().getOwner().getUserId().longValue() == update.getCallbackQuery().getMessage().getChatId().longValue())
            sendMessage.setReplyMarkup(ownerGroupBar());
        else
            sendMessage.setReplyMarkup(memberGroupBar()); //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        try {
            {
                execute(sendMessage);
            }
        } catch (TelegramApiException e) {
            System.out.println("Error");
        }
    }

    private void groupMembersList(Update update) {
        sendMessage(update.getMessage().getChatId(), "Власник:" + groupRepository.findById(userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId()).get().getOwner().getUserName());
        List<User> users = groupService.getGroupMembers(userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId());
        for (User user : users) {
            String out = "• " + user.getUserName() + " \n";
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(update.getMessage().getChatId()));
            message.setText(out);
            if (groupRepository.findById(userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId()).get().getOwner().getUserId().longValue() == update.getMessage().getChatId().longValue())
                message.setReplyMarkup(deleteUserFromGroup(user.getUserId()));
            try {
                {
                    execute(message);
                }
            } catch (TelegramApiException e) {
                System.out.println("Error");
            }


        }
    }

    private void groupTestsList(Update update) {
        List<Test> tests = testRepository.findByGroup(groupRepository.findById(userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId()).get());
        for (Test test : tests) {
            String out = "• " + test.getName() + " \n";
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(update.getMessage().getChatId()));
            message.setText(out);
            if (groupRepository.findById(userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId()).get().getOwner().getUserId().longValue() == update.getMessage().getChatId().longValue()) {
                message.setReplyMarkup(choseTest(test.getTestId()));
            }
            else
            {
                message.setReplyMarkup(testResult(test.getTestId()));
            }
            try {
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        if (groupRepository.findById(userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId()).get().getOwner().getUserId().longValue() == update.getMessage().getChatId().longValue()) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(update.getMessage().getChatId()));
            message.setText("Бажаєте створити новий тест?");
            message.setReplyMarkup(createTest(userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId()));
            try {
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }



    //============================== ownerGroupFunction
    private ReplyKeyboardMarkup ownerGroupBar() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();
        row.add("Список тестів");
        row2.add("Учасники");
        row2.add("Додати учасника");
        row3.add("Редагувати групу ✏");
        row3.add("Видалити групу \uD83D\uDDD1");
        row4.add("Назад \uD83D\uDD19");
        keyboardRows.add(row);
        keyboardRows.add(row2);
        keyboardRows.add(row3);
        keyboardRows.add(row4);
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup ownerTestBar() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();
        row.add("Призначити тест \uD83D\uDCC4");
        row1.add("Переглянути результати");
        row2.add("Редагувати тест ✏");
        row3.add("Видалити тест");
        row4.add("Назад \uD83D\uDD19");
        keyboardRows.add(row);
        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardRows.add(row3);
        keyboardRows.add(row4);
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    private InlineKeyboardMarkup deleteUserFromGroup(long userId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var deleteButton = new InlineKeyboardButton();
        deleteButton.setText("Видалити учасника");
        deleteButton.setCallbackData("DELETE_USER_FROM_GROUP:" + userId);
        rowInLine.add(deleteButton);
        rowsInLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup choseTest(long testId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var choseTest = new InlineKeyboardButton();
        choseTest.setText("Обрати");
        choseTest.setCallbackData("CHOOSE_TEST:" + testId);
        rowInLine.add(choseTest);
        rowsInLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup createTest(long groupId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var createTest = new InlineKeyboardButton();
        createTest.setText("Створити тестування ➕");
        createTest.setCallbackData("CREATE_TEST:" + groupId);
        rowInLine.add(createTest);
        rowsInLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup createQuestionMarkup(long testId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var createTest = new InlineKeyboardButton();
        createTest.setText("Додати запитання ➕");
        createTest.setCallbackData("CREATE_QUESTION:" + testId);
        rowInLine.add(createTest);
        rowsInLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup deleteQuestion(Long questionId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var deleteButton = new InlineKeyboardButton();
        deleteButton.setText("Видалити питання");
        deleteButton.setCallbackData("DELETE_QUESTION:" + questionId);
        rowInLine.add(deleteButton);
        rowsInLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }
    private InlineKeyboardMarkup approveAssign(Long testId, Long userId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var approveButton = new InlineKeyboardButton();
        approveButton.setText("Дозволити");
        approveButton.setCallbackData("APPROVE_ASSIGN:"+true+":"+testId+":"+userId);
        rowInLine.add(approveButton);
        var denyButton = new InlineKeyboardButton();
        denyButton.setText("Відмовити");
        denyButton.setCallbackData("APPROVE_ASSIGN:"+false+":"+testId+":"+userId);
        rowInLine.add(denyButton);
        rowsInLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    private void deleteUserFromGroup(Update update) {
        Long userId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        Long groupId = userRepository.findById(update.getCallbackQuery().getMessage().getChatId()).get().getSelectedGroupId();
        if (userService.isUserInGroup(userId, groupId)) {
            userService.removeUserFromGroup(userId, groupId);
            testResultService.deleteTestResultsForUser(userRepository.findById(userId).get());
            EditMessageText message = new EditMessageText();
            message.setChatId(update.getCallbackQuery().getMessage().getChatId());
            message.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
            message.setText("Учасника видалено");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            sendMessage(userId, "Вас видалено з групи " + groupRepository.findById(groupId).get().getName());
        } else {
            EditMessageText message = new EditMessageText();
            message.setChatId(update.getCallbackQuery().getMessage().getChatId());
            message.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
            message.setText("Не є учасником групи");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addUserToGroup(Update update) {
        String messageText = update.getMessage().getText();
        try {

            if (userRepository.findById(Long.parseLong(messageText)).orElse(null) == null) {
                sendMessage(update.getMessage().getChatId(), "Користувача не знайдено");
            } else {
                userService.addUserToGroup(Long.parseLong(messageText), userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId());
                sendMessage(update.getMessage().getChatId(), "Успішно додано");
                sendMessage(Long.parseLong(messageText), "Вас додблано до групи " + groupRepository.findById(userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId()).get().getName());
            }
        } catch (NumberFormatException e) {
            sendMessage(update.getMessage().getChatId(), "Користувача не знайдено");
        }
        userService.setState(new UserRequest(update.getMessage().getChatId()), ActivityState.GROUP_MENU);
    }

    private void chooseTest(Update update) {
        Long testId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        userService.setUserSelectedTestId(new UserRequest(update.getCallbackQuery().getMessage().getChatId()), testId);
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
        message.setText("Ви обрали " + testRepository.findById(testId).get().getName());
        message.setReplyMarkup(ownerTestBar());
        try {
            execute(message);
            userService.setState(new UserRequest(update.getCallbackQuery().getMessage().getChatId()), ActivityState.TEST_MENU);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    private void changeTest(Update update) {
        List<Question> questions = testService.getQuestionsByTestId(userRepository.findById(update.getMessage().getChatId()).get().getSelectedTestId());
        for (Question quest : questions) {
            String out = "• " + quest.getText() + " \n";
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(update.getMessage().getChatId()));
            message.setText(out);
            if (groupRepository.findById(userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId()).get().getOwner().getUserId().longValue() == update.getMessage().getChatId().longValue())
                message.setReplyMarkup(deleteQuestion(quest.getQuestionId()));
            try {
                {
                    execute(message);
                }
            } catch (TelegramApiException e) {
                System.out.println("Error");
            }
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Бажаєте додати запитання?");
        sendMessage.setReplyMarkup(createQuestionMarkup(userRepository.findById(update.getMessage().getChatId()).get().getSelectedTestId()));
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void backToGroupMenu(Update update) {
        userService.setState(new UserRequest(update.getMessage().getChatId()), ActivityState.GROUP_MENU);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.setText("Обрана група " + groupRepository.findById(userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId()).get().getName());
        if (groupRepository.findById(userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId()).get().getOwner().getUserId().longValue() == update.getMessage().getChatId().longValue())
            sendMessage.setReplyMarkup(ownerGroupBar());
        else
            sendMessage.setReplyMarkup(memberGroupBar()); //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        try {
            {
                execute(sendMessage);
            }
        } catch (TelegramApiException e) {
            System.out.println("Error");
        }

    }

    private void createQuestion(Update update) {
        Long testId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        question.put(update.getCallbackQuery().getMessage().getChatId(),new Question());
        question.get(update.getCallbackQuery().getMessage().getChatId()).setTest(testRepository.findById(testId).get());
        sendMessage(update.getCallbackQuery().getMessage().getChatId(), "Сформулюйте запитання");
        userService.setState(new UserRequest(update.getCallbackQuery().getMessage().getChatId()), ActivityState.QUESTION_REQUIRED);
    }

    private void deleteQuestionFunc(Update update) {
        Long questionId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        questionService.deleteQuestion(questionId);
        EditMessageText message = new EditMessageText();
        message.setChatId(update.getCallbackQuery().getMessage().getChatId());
        message.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        message.setText("Успішно видалено");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    //============================= memberGroupFunction
    private ReplyKeyboardMarkup memberGroupBar() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        row.add("Список тестів");
        row2.add("Учасники");
        row3.add("Назад \uD83D\uDD19");
        keyboardRows.add(row);
        keyboardRows.add(row2);
        keyboardRows.add(row3);
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }
    private InlineKeyboardMarkup testResult(Long testId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var getResultButton = new InlineKeyboardButton();
        getResultButton.setText("Переглянути мій результат");
        getResultButton.setCallbackData("GET_RESULT:" + testId);
        rowInLine.add(getResultButton);
        rowsInLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;

    }
    private InlineKeyboardMarkup requestToAssign(Long testId, Long userId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var getResultButton = new InlineKeyboardButton();
        getResultButton.setText("Запит на перездачу");
        getResultButton.setCallbackData("REQUEST_TO_ASSIGN:" + testId+":"+userId);
        rowInLine.add(getResultButton);
        rowsInLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }
    private void getResult(Update update) {
        EditMessageText editMessageText=new EditMessageText();
        editMessageText.setChatId(update.getCallbackQuery().getMessage().getChatId());
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        Long testId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        TestResult testResult=testResultRepository.findByUserAndTest(userRepository.findById(update.getCallbackQuery().getMessage().getChatId()).get(),testRepository.findById(testId).get()).orElse(null);
        if(testResult!=null)
        {
            editMessageText.setText("Ви здали цей тест на "+testResult.getScore()+" балів");
        }
        else
        {
            editMessageText.setText("Ви ще не проходили цей тест");
        }
        editMessageText.setReplyMarkup(requestToAssign(testId,update.getCallbackQuery().getMessage().getChatId()));
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    private void requestToAssign(Update update)
    {
        Long testId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        Long userId = Long.parseLong(update.getCallbackQuery().getData().split(":")[2]);
        EditMessageText editMessageText=new EditMessageText();
        editMessageText.setChatId(update.getCallbackQuery().getMessage().getChatId());
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessageText.setText("Ви зробили заявку на перепроходження тесту\nОчікуйте відповіді");
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        SendMessage sendMessage=new SendMessage();
        sendMessage.setChatId(testRepository.findById(testId).get().getGroup().getOwner().getUserId());
        sendMessage.setReplyMarkup(approveAssign(testId,userId));
        sendMessage.setText("Користувач "+userRepository.findById(userId).get().getUserName()+" бажає перепройти тест "+testRepository.findById(testId).get().getName());
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    //Test================================================================
    private void testNameRequired(Update update) {
        test.get(update.getMessage().getChatId()).setName(update.getMessage().getText());
        testService.save(new UserRequest(update.getMessage().getChatId()), test.get(update.getMessage().getChatId()));
        sendMessage(update.getMessage().getChatId(), "Успішно створено");
        userService.setState(new UserRequest(update.getMessage().getChatId()), ActivityState.GROUP_MENU);

    }
    private void answerRequired(Update update) {
        if (numberOfAnswers.get(update.getMessage().getChatId()) == 0) {
            question.put(update.getMessage().getChatId(),questionRepository.save(question.get(update.getMessage().getChatId())));
            testService.addQuestionToTest(question.get(update.getMessage().getChatId()).getQuestionId(), testRepository.findById(userRepository.findById(update.getMessage().getChatId()).get().getSelectedTestId()).get().getTestId());
            Answer answer = new Answer();
            answer.setText(update.getMessage().getText());
            answer.setCorrect(true);
            answer = answerRepository.save(answer);
            questionService.addAnswerToQuestion(answer.getAnswerId(),question.get(update.getMessage().getChatId()).getQuestionId());
            numberOfAnswers.put(update.getMessage().getChatId(),numberOfAnswers.get(update.getMessage().getChatId())+1);
            sendMessage(update.getMessage().getChatId(), "Уведіть три неправильні відповіді");
            sendMessage(update.getMessage().getChatId(), "Уведіть " + numberOfAnswers.get(update.getMessage().getChatId()) + " неправильну відповідь");
        } else {
            Answer answer = new Answer();
            answer.setText(update.getMessage().getText());
            answer.setCorrect(false);
            answer = answerRepository.save(answer);
            questionService.addAnswerToQuestion(answer.getAnswerId(), question.get(update.getMessage().getChatId()).getQuestionId());
            numberOfAnswers.put(update.getMessage().getChatId(),numberOfAnswers.get(update.getMessage().getChatId())+1);
            if (numberOfAnswers.get(update.getMessage().getChatId()) != 4)
                sendMessage(update.getMessage().getChatId(), "Уведіть " + numberOfAnswers.get(update.getMessage().getChatId()) + " неправильну відповідь");
            else {
                group.put(update.getMessage().getChatId(),new Group());
                question.put(update.getMessage().getChatId(),new Question());
                sendMessage(update.getMessage().getChatId(), "Питання успішно додане");
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(update.getMessage().getChatId());
                sendMessage.setText("Бажаєте додати запитання?");
                userService.setState(new UserRequest(update.getMessage().getChatId()), ActivityState.TEST_MENU);
                sendMessage.setReplyMarkup(createQuestionMarkup(userRepository.findById(update.getMessage().getChatId()).get().getSelectedTestId()));
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }

            }
        }

    }
    private void assignTest(Update update) {
        List<User> users = groupService.getGroupMembers(userRepository.findById(update.getMessage().getChatId()).get().getSelectedGroupId());
        if (!testService.getQuestionsByTestId(userRepository.findById(update.getMessage().getChatId()).get().getSelectedTestId()).isEmpty()) {
            for (User user : users) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(user.getUserId());
                sendMessage.setText("Вам призначений тест " + testRepository.findById(userRepository.findById(update.getMessage().getChatId()).get().getSelectedTestId()).get().getName() + "\n Бажаєте розпочати?");
                sendMessage.setReplyMarkup(beginTest(userRepository.findById(update.getMessage().getChatId()).get().getSelectedTestId()));
                try {
                    Message sentMessage = execute(sendMessage);

                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        else
        {
            sendMessage(update.getMessage().getChatId(),"У цьому тесті не міститься запитань");
        }
    }

    private void questionRequired(Update update) {
        question.get(update.getMessage().getChatId()).setText(update.getMessage().getText());
        userService.setState(new UserRequest(update.getMessage().getChatId()), ActivityState.ANSWER_REQUIRED);
        sendMessage(update.getMessage().getChatId(), "Уведіть правильну відповідь");
        numberOfAnswers.put(update.getMessage().getChatId(),0);
    }
    private InlineKeyboardMarkup AnswerMarkup(Question currentQuestion) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine2 = new ArrayList<>();
        var answer1 = new InlineKeyboardButton();
        var answer2 = new InlineKeyboardButton();
        var answer3 = new InlineKeyboardButton();
        var answer4 = new InlineKeyboardButton();
        List<Answer> answers = questionService.getAnswerByQuestionId(currentQuestion.getQuestionId());
        Collections.shuffle(answers);
        answer1.setText(answers.get(0).getText());
        answer2.setText(answers.get(1).getText());
        answer3.setText(answers.get(2).getText());
        answer4.setText(answers.get(3).getText());
        answer1.setCallbackData("ANSWER:" + String.valueOf(answers.get(0).isCorrect()));
        answer2.setCallbackData("ANSWER:" + String.valueOf(answers.get(1).isCorrect()));
        answer3.setCallbackData("ANSWER:" + String.valueOf(answers.get(2).isCorrect()));
        answer4.setCallbackData("ANSWER:" + String.valueOf(answers.get(3).isCorrect()));
        rowInLine.add(answer1);
        rowInLine.add(answer2);
        rowInLine2.add(answer3);
        rowInLine2.add(answer4);
        rowsInLine.add(rowInLine);
        rowsInLine.add(rowInLine2);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup beginTest(Long testId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var deleteButton = new InlineKeyboardButton();
        deleteButton.setText("Розпочати");
        deleteButton.setCallbackData("START_TEST:" + testId + ":" + System.currentTimeMillis());
        rowInLine.add(deleteButton);
        rowsInLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    private void startTest(Update update) {
        Long testId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        Long time = Long.parseLong(update.getCallbackQuery().getData().split(":")[2]);
        if (System.currentTimeMillis() - time < 1800000) {
            userService.setState(new UserRequest(update.getCallbackQuery().getMessage().getChatId()), ActivityState.TEST_PASSAGE);
            userService.setUserGrade(new UserRequest(update.getCallbackQuery().getMessage().getChatId()), 0);
            userService.setUserCurrentQuestion(new UserRequest(update.getCallbackQuery().getMessage().getChatId()), 0);
            userService.setUserSelectedTestId(new UserRequest(update.getCallbackQuery().getMessage().getChatId()), testId);
            List<Question> questions = testService.getQuestionsByTestId(testId);
            Question currentQuestion = questions.get(userService.getUserCurrentQuestion(new UserRequest(update.getCallbackQuery().getMessage().getChatId())));
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText(currentQuestion.getText());
            sendMessage.setReplyMarkup(AnswerMarkup(currentQuestion));
            sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
            deleteMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
            try {
                execute(deleteMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } else {
            EditMessageText message = new EditMessageText();
            message.setChatId(update.getCallbackQuery().getMessage().getChatId());
            message.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
            message.setText(testRepository.findById(testId).get().getName() + " протерміновано");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void testInProgres(Update update) {
        Boolean answerTrueOrFalse = Boolean.parseBoolean(update.getCallbackQuery().getData().split(":")[1]);
        if (answerTrueOrFalse) {
            userService.GradeAddRight(new UserRequest(update.getCallbackQuery().getMessage().getChatId()));
        }
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        deleteMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        userService.nextQuestion(new UserRequest(update.getCallbackQuery().getMessage().getChatId()));
        if (testService.getQuestionsByTestId(userService.getUserSelectedTestId(new UserRequest(update.getCallbackQuery().getMessage().getChatId()))).size() > userService.getUserCurrentQuestion(new UserRequest(update.getCallbackQuery().getMessage().getChatId()))) {
            List<Question> questions = testService.getQuestionsByTestId(userService.getUserSelectedTestId(new UserRequest(update.getCallbackQuery().getMessage().getChatId())));
            Question currentQuestion = questions.get(userService.getUserCurrentQuestion(new UserRequest(update.getCallbackQuery().getMessage().getChatId())));
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText(currentQuestion.getText());
            sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
            sendMessage.setReplyMarkup(AnswerMarkup(currentQuestion));
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }

        } else {
            String answer = "Ви здали " + testRepository.findById(userRepository.findById(update.getCallbackQuery().getMessage().getChatId()).get().getSelectedTestId()).get().getName() + " на " + userService.getUserGrade(new UserRequest(update.getCallbackQuery().getMessage().getChatId())) + " балів із " + testService.getQuestionsByTestId(userRepository.findById(update.getCallbackQuery().getMessage().getChatId()).get().getSelectedTestId()).size() + " можливих";
            SendMessage message = new SendMessage();
            message.setChatId(update.getCallbackQuery().getMessage().getChatId());
            message.setText(answer);
            message.setReplyMarkup(mainBar());
            try {
                {
                    execute(message);
                }
            } catch (TelegramApiException e) {
                System.out.println("Error");
            }
            userService.setState(new UserRequest(update.getCallbackQuery().getMessage().getChatId()), ActivityState.LOGGED_IN);
            //збереження в бд
            TestResult previousResult=testResultRepository.findByUserAndTest(userRepository.findById(update.getCallbackQuery().getMessage().getChatId()).get(), testRepository.findById(userRepository.findById(update.getCallbackQuery().getMessage().getChatId()).get().getSelectedTestId()).get()).orElse(null);
            if (previousResult!=null)
            {
                previousResult.setScore(userService.getUserGrade(new UserRequest(update.getCallbackQuery().getMessage().getChatId())));
            }
            else {
                previousResult = new TestResult();
                previousResult.setTest(testRepository.findById(userRepository.findById(update.getCallbackQuery().getMessage().getChatId()).get().getSelectedTestId()).get());
                previousResult.setUser(userRepository.findById(update.getCallbackQuery().getMessage().getChatId()).get());
                previousResult.setScore(userService.getUserGrade(new UserRequest(update.getCallbackQuery().getMessage().getChatId())));
            }
            testResultService.saveTestResult(previousResult);
            message = new SendMessage();
            message.setChatId(testRepository.findById(userRepository.findById(update.getCallbackQuery().getMessage().getChatId()).get().getSelectedTestId()).get().getGroup().getOwner().getUserId());
            answer = userRepository.findById(update.getCallbackQuery().getMessage().getChatId()).get().getUserName() + " здав тест " + testRepository.findById(userRepository.findById(update.getCallbackQuery().getMessage().getChatId()).get().getSelectedTestId()).get().getName() + " на " + userService.getUserGrade(new UserRequest(update.getCallbackQuery().getMessage().getChatId())) + " балів із " + testService.getQuestionsByTestId(userRepository.findById(update.getCallbackQuery().getMessage().getChatId()).get().getSelectedTestId()).size() + " можливих";
            message.setText(answer);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void approveAssign(Update update)
    {
        boolean approvedOrDenied=Boolean.parseBoolean(update.getCallbackQuery().getData().split(":")[1]);
        Long testId = Long.parseLong(update.getCallbackQuery().getData().split(":")[2]);
        Long userId = Long.parseLong(update.getCallbackQuery().getData().split(":")[3]);
        if(approvedOrDenied)
        {
            EditMessageText editMessageText=new EditMessageText();
            editMessageText.setChatId(update.getCallbackQuery().getMessage().getChatId());
            editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
            editMessageText.setText("Ви одобрили перепроходження "+testRepository.findById(testId).get().getName()+" для "+userRepository.findById(userId).get().getUserName());
            try {
                execute(editMessageText);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(userId);
            sendMessage.setText("Вам одобрено перепроходження тесту " + testRepository.findById(testId).get().getName() + "\n Бажаєте розпочати?");
            sendMessage.setReplyMarkup(beginTest(testId));
            try {
                execute(sendMessage);

            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        else
        {
            EditMessageText editMessageText=new EditMessageText();
            editMessageText.setChatId(update.getCallbackQuery().getMessage().getChatId());
            editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
            editMessageText.setText("Ви відмовили у перепроходженні "+testRepository.findById(testId).get().getName()+" для "+userRepository.findById(userId).get().getUserName());
            try {
                execute(editMessageText);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(userId);
            sendMessage.setText("Вам відмовлено у перепроходженні тесту " + testRepository.findById(testId).get().getName());
            try {
                execute(sendMessage);

            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }



}
