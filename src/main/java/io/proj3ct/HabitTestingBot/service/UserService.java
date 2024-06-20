package io.proj3ct.HabitTestingBot.service;

import io.proj3ct.HabitTestingBot.dataType.ActivityState;
import io.proj3ct.HabitTestingBot.domain.Group;
import io.proj3ct.HabitTestingBot.domain.User;
import io.proj3ct.HabitTestingBot.dto.UserRequest;
import io.proj3ct.HabitTestingBot.repository.GroupRepository;
import io.proj3ct.HabitTestingBot.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    @Transactional
    public void addUserToGroup(Long userId, Long groupId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));

        group.getUsers().add(user); // Додаємо користувача до групи
        user.getGroups().add(group); // Також додамо групу до користувача для зв'язку з обох боків

        groupRepository.save(group); // Зберігаємо зміни у групі
        userRepository.save(user); // Зберігаємо зміни у користувача
    }
    @Transactional
    public void removeUserFromGroup(Long userId, Long groupId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        user.getGroups().remove(group);
        group.getUsers().remove(user);

        userRepository.save(user);
        groupRepository.save(group);
    }
    public boolean isUserInGroup(Long userId, Long groupId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Group group=groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        return group.getUsers().contains(user);
    }
    public void save(UserRequest userRequest,String userName)
    {
        if (userRepository.findById(userRequest.getChatId()).isEmpty()) {
            User user = new User();
            user.setUserId(userRequest.getChatId());
            user.setUserName(userName);
            user.setState(ActivityState.LOGGED_IN);
            userRepository.save(user);
        }
    }
    public ActivityState getState(UserRequest userRequest)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        return user.getState();
    }
    public void setState(UserRequest userRequest,ActivityState activityState)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        user.setState(activityState);
        userRepository.save(user);
    }
    public String getUserName(UserRequest userRequest)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        return user.getUserName();
    }
    public void setUserName(UserRequest userRequest,String userName)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        user.setUserName(userName);
        userRepository.save(user);
    }
    public String getUserPhoneNumber(UserRequest userRequest)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        return user.getPhoneNumber();
    }
    public void setUserPhoneNumber(UserRequest userRequest,String phoneNumber)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);
    }

    public String getUserEmail(UserRequest userRequest)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        return user.getEmail();
    }
    public void setUserEmail(UserRequest userRequest,String email)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        user.setEmail(email);
        userRepository.save(user);
    }
    public Boolean getUserVerificationStatus(UserRequest userRequest)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        return user.isVerification();
    }
    public void setUserVerificationStatus(UserRequest userRequest,Boolean verification)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        user.setVerification(verification);
        userRepository.save(user);
    }
    public Long getUserSelectedGroupId(UserRequest userRequest)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        return user.getSelectedGroupId();
    }
    public void setUserSelectedGroupId(UserRequest userRequest,long selectedGroupId)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        user.setSelectedGroupId(selectedGroupId);
        userRepository.save(user);
    }
    public Long getUserSelectedTestId(UserRequest userRequest)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        return user.getSelectedTestId();
    }
    public void setUserSelectedTestId(UserRequest userRequest,long selectedTestId)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        user.setSelectedTestId(selectedTestId);
        userRepository.save(user);
    }
    public Integer getUserCurrentQuestion(UserRequest userRequest)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        return user.getCurrentQuestion();
    }
    public void setUserCurrentQuestion (UserRequest userRequest, Integer currentQuestion)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        user.setCurrentQuestion(currentQuestion);
        userRepository.save(user);
    }
    public void nextQuestion (UserRequest userRequest)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        user.setCurrentQuestion(user.getCurrentQuestion()+1);
        userRepository.save(user);
    }
    public Integer getUserGrade(UserRequest userRequest)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        return user.getGrade();
    }
    public void setUserGrade(UserRequest userRequest,Integer grade)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        user.setGrade(grade);
        userRepository.save(user);
    }
    public void GradeAddRight(UserRequest userRequest)
    {
        User user=userRepository.findById(userRequest.getChatId()).get();
        user.setGrade(user.getGrade()+1);
        userRepository.save(user);
    }

}
