package io.proj3ct.HabitTestingBot.service;


import io.proj3ct.HabitTestingBot.domain.*;
import io.proj3ct.HabitTestingBot.dto.UserRequest;
import io.proj3ct.HabitTestingBot.repository.AnswerRepository;
import io.proj3ct.HabitTestingBot.repository.GroupRepository;
import io.proj3ct.HabitTestingBot.repository.TestRepository;
import io.proj3ct.HabitTestingBot.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;

    private final GroupRepository groupRepository;

    public void save(Question question, Answer answer) {
        answer.setQuestion(question);
        answerRepository.save(answer);
    }


}
