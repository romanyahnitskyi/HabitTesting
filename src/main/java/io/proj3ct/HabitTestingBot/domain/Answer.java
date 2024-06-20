package io.proj3ct.HabitTestingBot.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Objects;

@Entity
@Table(name="answers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;
    private String text;
    private boolean correct;
    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;
    @Override
    public int hashCode() {
        return Objects.hash(answerId);
    }

}
