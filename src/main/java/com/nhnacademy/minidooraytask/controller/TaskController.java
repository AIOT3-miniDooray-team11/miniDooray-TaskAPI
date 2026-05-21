package com.nhnacademy.minidooraytask.controller;

import com.nhnacademy.minidooraytask.task.domain.*;
import com.nhnacademy.minidooraytask.task.service.TaskFacade;
import com.nhnacademy.minidooraytask.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/task-api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskFacade taskFacade;

    //조회시, 필요한 관계가 있으면 같이 겸사겸사 기져오기
    @GetMapping
    public ResponseEntity<TaskInfoListDto> getTaskResponseDtoList(@PathVariable long projectId,
                                                                  @RequestHeader("X-Account-Id") Long accountId) {
        TaskInfoListDto responseDto = taskFacade.getTaskInfoList(projectId, accountId);
        return ResponseEntity.ok().body(responseDto);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskViewDto> getTaskResponseDto(@PathVariable long projectId,
                                                          @PathVariable long taskId,
                                                          @RequestHeader("X-Account-Id") Long accountId) {

        TaskViewDto responseDto = taskFacade.getSpecificTask(taskId,projectId,accountId);
        return ResponseEntity.ok().body(responseDto);
    }

    @PostMapping
    public ResponseEntity<Void> createTask(@PathVariable long projectId,
                                           @RequestHeader("X-Account-Id") Long accountId,
                                           @RequestBody TaskRequestDto requestDto) {

        taskFacade.createTask(projectId, accountId, requestDto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponseDto> updateTask(@PathVariable long projectId,
                                           @PathVariable long taskId,
                                           @RequestHeader("X-Account-Id") Long accountId,
                                           @RequestBody TaskRequestDto taskRequestDto) {

        TaskResponseDto responseDto = taskFacade.updateTask(projectId, taskId, accountId, taskRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable long projectId,
                                           @PathVariable long taskId,
                                           @RequestHeader("X-Account-Id") Long accountId) {

        taskFacade.deleteTask(projectId,accountId,taskId);
        return ResponseEntity.ok().build();
    }

}
