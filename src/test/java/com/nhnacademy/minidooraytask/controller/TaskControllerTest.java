package com.nhnacademy.minidooraytask.controller;


import com.nhnacademy.minidooraytask.MileStone.domain.MileStoneStatus;
import com.nhnacademy.minidooraytask.handler.CustomExceptionHandler;
import com.nhnacademy.minidooraytask.member.exception.ProjectMemberIsNotExistException;
import com.nhnacademy.minidooraytask.project.domain.ProjectInfoDto;
import com.nhnacademy.minidooraytask.project.domain.ProjectStatus;
import com.nhnacademy.minidooraytask.task.domain.*;
import com.nhnacademy.minidooraytask.task.exception.TaskNotFoundException;
import com.nhnacademy.minidooraytask.task.service.TaskFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import(CustomExceptionHandler.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskFacade taskFacade;

    // ===== GET =====

    @Test
    @DisplayName("Task 목록 조회 - 성공")
    void getTaskResponseDtoList_success() throws Exception {

        long projectId = 1L;
        long accountId = 1L;

        TaskInfoDto taskInfoDto = new TaskInfoDto(1L, "test task", MileStoneStatus.IN_PROGRESS);
        TaskInfoListDto responseDto = new TaskInfoListDto(List.of(taskInfoDto));

        given(taskFacade.getTaskInfoList(projectId, accountId)).willReturn(responseDto);

        mockMvc.perform(get("/task-api/projects/{projectId}/tasks", projectId)
                .header("X-Account-Id", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskInfoDtoList[0].id").value(1L))
                .andExpect(jsonPath("$.taskInfoDtoList[0].title").value("test task"))
                .andExpect(jsonPath("$.taskInfoDtoList[0].status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Task 단건 조회 - 성공")
    void getTaskResponseDto_success() throws Exception {

        long projectId = 1L;
        long accountId = 1L;
        long taskId = 1L;

        TaskResponseDto taskResponseDto = new TaskResponseDto(
                taskId, projectId, accountId, "test task", "content",
                LocalDateTime.now(), LocalDateTime.now(), null, List.of()
        );

        TaskInfoListDto taskInfoListDto = new TaskInfoListDto(List.of());
        ProjectInfoDto projectInfoDto = new ProjectInfoDto(projectId, "project", ProjectStatus.ACTIVE, List.of());
        TaskViewDto taskViewDto = new TaskViewDto(taskResponseDto, taskInfoListDto, projectInfoDto, List.of());

        given(taskFacade.getSpecificTask(taskId, projectId, accountId)).willReturn(taskViewDto);

        mockMvc.perform(get("/task-api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header("X-Account-Id", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskResponseDto.title").value("test task"))
                .andExpect(jsonPath("$.projectInfoDto.title").value("project"));

    }

    @Test
    @DisplayName("Task 단건 조회 - 실패 ")
    void getTaskResponseDto_fail_taskNotFound() throws Exception {
        long projectId = 1L;
        long taskId = 400L;
        long accountId = 100L;

        given(taskFacade.getSpecificTask(taskId, projectId, accountId))
                .willThrow(new TaskNotFoundException("존재하지 않는 태스크입니다"));


        mockMvc.perform(get("/task-api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header("X-Account-Id", accountId))
                .andExpect(status().isBadRequest()); // CustomExceptionHandler에 의해 400 반환 검증
    }

    // ===== POST =====

    @Test
    @DisplayName("Task 생성 - 성공")
    void createTask_success() throws Exception {

        long projectId = 1L;
        long accountId = 100L;
        TaskRequestDto requestDto = new TaskRequestDto("new task", "content", List.of("tag1", "tag2"));

        willDoNothing().given(taskFacade).createTask(eq(projectId), eq(accountId), any(TaskRequestDto.class));

        mockMvc.perform(post("/task-api/projects/{projectId}/tasks", projectId)
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Task 생성 - 실패")
    void createTask_fail_notProjectMember() throws Exception {
        // given
        long projectId = 1L;
        long accountId = 400L;
        TaskRequestDto requestDto = new TaskRequestDto("새로운 태스크", "내용", List.of("tag1"));


        willThrow(new ProjectMemberIsNotExistException("프로젝트 멤버가 아닙니다"))
                .given(taskFacade).createTask(eq(projectId), eq(accountId), any(TaskRequestDto.class));

        mockMvc.perform(post("/task-api/projects/{projectId}/tasks", projectId)
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Task 수정 - 성공")
    void updateTask_success() throws Exception {
        long projectId = 1L;
        long taskId = 1L;
        long accountId = 100L;
        TaskRequestDto requestDto = new TaskRequestDto("modi task", "content", List.of("tag1"));

        TaskResponseDto responseDto = new TaskResponseDto(
                taskId, projectId, accountId, "modi task", "content",
                LocalDateTime.now(), LocalDateTime.now(), null, List.of()
        );

        given(taskFacade.updateTask(eq(projectId), eq(taskId), eq(accountId), any(TaskRequestDto.class)))
                .willReturn(responseDto);

        mockMvc.perform(put("/task-api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("modi task"))
                .andExpect(jsonPath("$.content").value("content"));
    }

    // ===== DELETE =====

    @Test
    @DisplayName("Task 삭제 - 성공")
    void deleteTask_success() throws Exception {

        long projectId = 1L;
        long taskId = 1L;
        long accountId = 100L;

        willDoNothing().given(taskFacade).deleteTask(projectId, accountId, taskId);

        mockMvc.perform(delete("/task-api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header("X-Account-Id", accountId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Task 삭제 - 실패")
    void deleteTask_fail_notTaskMaker() throws Exception {
        long projectId = 1L;
        long taskId = 1L;
        long accountId = 400L;
        willThrow(new TaskNotFoundException("해당 테스크의 작성자가 아닙니다"))
                .given(taskFacade).deleteTask(projectId, accountId, taskId);

        mockMvc.perform(delete("/task-api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header("X-Account-Id", accountId))
                .andExpect(status().isBadRequest());
    }
}
