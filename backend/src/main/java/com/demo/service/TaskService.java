package com.demo.service;

import com.demo.dto.TaskRequestDto;
import com.demo.dto.TaskResponseDto;
import com.demo.exception.ResourceNotFoundException;
import com.demo.mapper.TaskMapper;
import com.demo.model.Task;
import com.demo.model.User;
import com.demo.repository.TaskRepository;
import com.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;

    private User getManagedUser(UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername());
        if (user == null) {
            throw new ResourceNotFoundException("User not found with username: " + currentUser.getUsername());
        }
        return user;
    }

    // 1. إنشاء تاسك وربطها باليوزر الحالي
    @Transactional
    public TaskResponseDto createTask(TaskRequestDto newTaskDto, UserDetails currentUser) {
        User managedUser = getManagedUser(currentUser);
        Task task = taskMapper.toEntity(newTaskDto);
        task.setUser(managedUser); // ربط التاسك بمالكها
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponseDto(savedTask);
    }

    // 2. جلب جميع تاسكات اليوزر الحالي فقط (مع Pagination)
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getAllTasksForCurrentUser(UserDetails currentUser, int pageNo, int pageSize, String sortBy, String sortDir) {
        User managedUser = getManagedUser(currentUser);
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        // جلب مهام المستخدم الحالي فقط
        Page<Task> tasks = taskRepository.findByUserId(managedUser.getId(), pageable);

        return tasks.map(taskMapper::toResponseDto);
    }

    // 3. جلب تاسك واحدة بالـ ID مع التأكد إنها ملك لليوزر الحالي
    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(Long taskId, UserDetails currentUser) {
        User managedUser = getManagedUser(currentUser);
        Task task = taskRepository.findByIdAndUserId(taskId, managedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        return taskMapper.toResponseDto(task);
    }

    // 4. تعديل تاسك مع التأكد من الملكية
    @Transactional
    public TaskResponseDto updateTask(Long taskId, TaskRequestDto taskRequestDto, UserDetails currentUser) {
        User managedUser = getManagedUser(currentUser);
        Task task = taskRepository.findByIdAndUserId(taskId, managedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        task.setTitle(taskRequestDto.title());
        task.setDescription(taskRequestDto.description());

        Task updatedTask = taskRepository.save(task);
        return taskMapper.toResponseDto(updatedTask);
    }

    // 5. حذف تاسك مع التأكد من الملكية
    @Transactional
    public void deleteTask(Long taskId, UserDetails currentUser) {
        User managedUser = getManagedUser(currentUser);
        Task task = taskRepository.findByIdAndUserId(taskId, managedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        taskRepository.delete(task);
    }

    // 6. تعديل حالة التاسك
    @Transactional
    public TaskResponseDto toggleTaskStatus(Long taskId, UserDetails currentUser) {
        User managedUser = getManagedUser(currentUser);
        Task task = taskRepository.findByIdAndUserId(taskId, managedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        // عكس الحالة الحالية
        task.setCompleted(!task.isCompleted());

        return taskMapper.toResponseDto(taskRepository.save(task));
    }
}
