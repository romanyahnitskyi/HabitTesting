package io.proj3ct.HabitTestingBot.repository;

import io.proj3ct.HabitTestingBot.domain.Group;
import io.proj3ct.HabitTestingBot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group,Long> {
    List<Group> findByOwnerUserId(Long ownerId);

    List<Group> findByUsersUserId(Long userId);


}
