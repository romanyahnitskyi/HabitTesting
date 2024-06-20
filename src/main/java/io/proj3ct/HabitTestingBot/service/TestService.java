package io.proj3ct.HabitTestingBot.service;


import io.proj3ct.HabitTestingBot.domain.Group;
import io.proj3ct.HabitTestingBot.domain.Question;
import io.proj3ct.HabitTestingBot.domain.Test;
import io.proj3ct.HabitTestingBot.domain.User;
import io.proj3ct.HabitTestingBot.dto.UserRequest;
import io.proj3ct.HabitTestingBot.repository.GroupRepository;
import io.proj3ct.HabitTestingBot.repository.QuestionRepository;
import io.proj3ct.HabitTestingBot.repository.TestRepository;
import io.proj3ct.HabitTestingBot.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestService {
    private final TestRepository testRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final GroupRepository groupRepository;
    @Autowired
    private final QuestionRepository questionRepository;

    public void save(UserRequest userRequest, Test test)
    {
        test.setGroup(groupRepository.findById(userRepository.findById(userRequest.getChatId()).get().getSelectedGroupId()).get());
        testRepository.save(test);
    }


    @Transactional
    public Test createTest(String testName, Long groupId) {
        Optional<Group> groupOptional = groupRepository.findById(groupId);
        if (!groupOptional.isPresent()) {
            throw new RuntimeException("Група з ID " + groupId + " не знайдена");
        }
        Group group = groupOptional.get();

        Test test = new Test();
        test.setName(testName);
        test.setGroup(group);

        return testRepository.save(test);
    }
    @Transactional
    public void addQuestionToTest(Long questionId, Long testId) {
        Test test = testRepository.findById(testId).orElseThrow(() -> new RuntimeException("Test not found"));
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new RuntimeException("Question not found"));
        question.setTest(test);
        test.getQuestions().add(question);

        questionRepository.save(question);
        testRepository.save(test);
    }
    @Transactional
    public void deleteTest(Long testId) {
        testRepository.deleteById(testId);
    }
    @Transactional
    public List<Question> getQuestionsByTestId(Long testId) {
        Test test = testRepository.findById(testId).orElse(null);
        if (test != null) {
            return new ArrayList<Question> (test.getQuestions());
        }
        return Collections.emptyList(); // або кидайте виняток, залежно від вашої бізнес-логіки
    }
}
