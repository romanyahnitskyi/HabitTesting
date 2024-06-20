package io.proj3ct.HabitTestingBot.service;

import io.proj3ct.HabitTestingBot.domain.Group;
import io.proj3ct.HabitTestingBot.domain.User;
import io.proj3ct.HabitTestingBot.dto.UserRequest;
import io.proj3ct.HabitTestingBot.repository.GroupRepository;
import io.proj3ct.HabitTestingBot.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;


    public List<Group> getGroupByUser(Long userId) {
        List<Group> ownedGroups = groupRepository.findByOwnerUserId(userId);
        List<Group> memberGroups = groupRepository.findByUsersUserId(userId);

        return Stream.concat(ownedGroups.stream(), memberGroups.stream())
                .collect(Collectors.toList());
    }

    public List<User> getGroupMembers(Long groupId) {
        Group group = groupRepository.findById(groupId).orElse(null);
        if (group != null) {
            return new ArrayList<User>(group.getUsers());
        }
        return Collections.emptyList(); // або кидайте виняток, залежно від вашої бізнес-логіки
    }

    public void save(UserRequest userRequest, Group group) {
        group.setOwner(userRepository.findById(userRequest.getChatId()).get());
        groupRepository.save(group);
    }
    @Transactional
    public void deleteGroupAndClearRelations(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Очищення зв'язків з учасниками
        group.getUsers().forEach(user -> user.getGroups().remove(group));
        group.getUsers().clear();
        group.getOwner().getGroups().remove(group);
        group.setOwner(null);
        groupRepository.save(group);
        groupRepository.delete(group);
    }
}
