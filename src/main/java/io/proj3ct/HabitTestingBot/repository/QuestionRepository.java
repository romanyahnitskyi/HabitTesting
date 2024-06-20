package io.proj3ct.HabitTestingBot.repository;
import io.proj3ct.HabitTestingBot.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface QuestionRepository extends JpaRepository<Question,Long> {

}
