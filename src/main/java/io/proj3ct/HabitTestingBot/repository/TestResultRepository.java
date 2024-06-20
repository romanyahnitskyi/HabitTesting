package io.proj3ct.HabitTestingBot.repository;

import io.proj3ct.HabitTestingBot.domain.Test;
import io.proj3ct.HabitTestingBot.domain.TestResult;
import io.proj3ct.HabitTestingBot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByUser(User user);
    List<TestResult> findByTest(Test test);
    Optional<TestResult> findByUserAndTest(User user, Test test);
    void deleteByUser(User user);

    void deleteByTest(Test test);
}