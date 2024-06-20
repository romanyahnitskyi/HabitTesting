package io.proj3ct.HabitTestingBot.service;


import io.proj3ct.HabitTestingBot.domain.Answer;
import io.proj3ct.HabitTestingBot.domain.Question;
import io.proj3ct.HabitTestingBot.domain.Test;
import io.proj3ct.HabitTestingBot.dto.UserRequest;
import io.proj3ct.HabitTestingBot.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {
    @Autowired
    private final QuestionRepository questionRepository;
    @Autowired
    private final AnswerRepository answerRepository;
    @Autowired
    AnswerService answerService;

    public Question save(Test test, Question question)
    {
        question.setTest(test);
        return questionRepository.save(question);
    }
    @Transactional
    public void addAnswerToQuestion(Long answerId, Long questionId) {
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new RuntimeException("Question not found"));
        Answer answer = answerRepository.findById(answerId).orElseThrow(() -> new RuntimeException("Answer not found"));
        answer.setQuestion(question);
        question.getAnswers().add(answer);
        answerRepository.save(answer);
        questionRepository.save(question);
    }
    @Transactional
    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }

    @Transactional
    public List<Answer> getAnswerByQuestionId(Long questionId) {
        Question question = questionRepository.findById(questionId).orElse(null);
        if (question != null) {
            return new ArrayList<Answer>(question.getAnswers());
        }
        return Collections.emptyList(); // або кидайте виняток, залежно від вашої бізнес-логіки
    }

}
