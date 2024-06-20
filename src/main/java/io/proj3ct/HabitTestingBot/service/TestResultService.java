package io.proj3ct.HabitTestingBot.service;

import io.proj3ct.HabitTestingBot.domain.Test;
import io.proj3ct.HabitTestingBot.domain.TestResult;
import io.proj3ct.HabitTestingBot.domain.User;
import io.proj3ct.HabitTestingBot.repository.TestResultRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestResultService {
    private final TestResultRepository testResultRepository;

    public TestResultService(TestResultRepository testResultRepository) {
        this.testResultRepository = testResultRepository;
    }

    // Метод для отримання всіх результатів тестів конкретного користувача
    public List<TestResult> getResultsByUser(User user) {
        return testResultRepository.findByUser(user);
    }

    // Метод для збереження результату тесту
    public TestResult saveTestResult(TestResult testResult) {
        return testResultRepository.save(testResult);
    }
    public List<TestResult> getResultsByTest(Test test) {
        return testResultRepository.findByTest(test);
    }
    @Transactional
    public void deleteTestResultsForUser(User user) {
        testResultRepository.deleteByUser(user);
    }
    @Transactional
    public void deleteTestResultsForTest(Test test) {
        testResultRepository.deleteByTest(test);
    }

}

