package com.demo.service;

import com.demo.dto.RoleRequestDto;
import com.demo.dto.TaskResponseDto;
import com.demo.dto.UserResponseDto;
import com.demo.exception.ResourceNotFoundException;
import com.demo.mapper.TaskMapper;
import com.demo.mapper.UserMapper;
import com.demo.model.Task;
import com.demo.model.User;
import com.demo.repository.TaskRepository;
import com.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminService
{
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final UserMapper userMapper;
    private final TaskMapper taskMapper;

    //method for getting all users
    public Page<UserResponseDto> getAllUsers(Pageable pageable)
    {
        Page<User> users = userRepository.findAll(pageable);
        return  users.map(userMapper::toResponseDto);
    }
    //method for getting all tasks
    public Page<TaskResponseDto> getAllTasks(Pageable pageable)
    {
        Page<Task> tasks = taskRepository.findAll(pageable);
        return  tasks.map(taskMapper::toResponseDto);
    }// method for deleting a task
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        taskRepository.delete(task);
    }
    //method for changing the Role
    public UserResponseDto updateUserRole(Long id, RoleRequestDto roleDto, Long currentAdminId) {
        // 🛑 منع الأدمن من تعديل رول نفسه
        if (id.equals(currentAdminId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot change your own role!");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setRole(roleDto.role());
        User updatedUser = userRepository.save(user);
        return userMapper.toResponseDto(updatedUser);
    }

}
