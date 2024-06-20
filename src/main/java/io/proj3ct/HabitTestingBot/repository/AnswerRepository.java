package io.proj3ct.HabitTestingBot.repository;

import io.proj3ct.HabitTestingBot.domain.Answer;
import io.proj3ct.HabitTestingBot.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer,Long> {

}
