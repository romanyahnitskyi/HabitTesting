package io.proj3ct.HabitTestingBot.domain;

import io.proj3ct.HabitTestingBot.dataType.ActivityState;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name="users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private Long userId;
    private String userName;
    private String phoneNumber;
    private String email;
    private boolean verification =false;
    private ActivityState state;
    private Long selectedGroupId;
    private Long selectedTestId;
    private Integer currentQuestion;
    private Integer grade;
    @ManyToMany(mappedBy = "users")
    private Set<Group> groups=new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
