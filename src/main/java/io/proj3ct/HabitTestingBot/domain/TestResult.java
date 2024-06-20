package io.proj3ct.HabitTestingBot.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="test_results")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="test_id")
    private Test test;

    private Integer score; // Результат тесту, наприклад, кількість правильних відповідей

    // Додайте інші поля за потребою, наприклад, дату проходження тесту
}
