package io.proj3ct.HabitTestingBot.repository;

import io.proj3ct.HabitTestingBot.domain.Group;
import io.proj3ct.HabitTestingBot.domain.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestRepository extends JpaRepository<Test,Long> {
    List<Test> findByGroup(Group group);

}
