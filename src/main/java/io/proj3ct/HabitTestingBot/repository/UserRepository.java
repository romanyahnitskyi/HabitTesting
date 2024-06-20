package io.proj3ct.HabitTestingBot.repository;

import io.proj3ct.HabitTestingBot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
