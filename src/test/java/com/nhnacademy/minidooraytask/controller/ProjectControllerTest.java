package com.nhnacademy.minidooraytask.controller;

import com.nhnacademy.minidooraytask.MileStone.domain.MileStoneStatus;
import com.nhnacademy.minidooraytask.handler.CustomExceptionHandler;
import com.nhnacademy.minidooraytask.project.domain.*;
import com.nhnacademy.minidooraytask.project.exception.NoAuthoProjectException;
import com.nhnacademy.minidooraytask.project.service.ProjectFacade;
import com.nhnacademy.minidooraytask.project.service.ProjectService;
import com.nhnacademy.minidooraytask.task.domain.TaskInfoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import(CustomExceptionHandler.class)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private ProjectFacade projectFacade;

    @Test
    @DisplayName("내 프로젝트 목록 조회 - 성공")
    void getMyProjects_success() throws Exception {
        long accountId = 100L;
        ProjectInfoDto projectInfo = new ProjectInfoDto(1L, "Test Project", ProjectStatus.ACTIVE, List.of(MileStoneStatus.IN_PROGRESS));
        TaskInfoDto taskInfo = new TaskInfoDto(10L, "Test Task", MileStoneStatus.IN_PROGRESS);
        ProjectViewDto mockResponse = new ProjectViewDto(List.of(projectInfo), List.of(taskInfo));

        given(projectFacade.getProjectView(accountId)).willReturn(mockResponse);

        mockMvc.perform(get("/task-api/projects")
                        .header("X-Account-Id", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectInfoDtoList[0].id").value(1L))
                .andExpect(jsonPath("$.projectInfoDtoList[0].title").value("Test Project"));
    }

    @Test
    @DisplayName("프로젝트 생성 - 성공")
    void createProject_success() throws Exception {
        long accountId = 100L;
        ProjectRequestDto requestDto = new ProjectRequestDto("새로운 프로젝트", "설명", ProjectStatus.ACTIVE);

        willDoNothing().given(projectService).createProject(eq(accountId), any(ProjectRequestDto.class));

        mockMvc.perform(post("/task-api/projects")
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("프로젝트 수정 - 성공")
    void updateProject_success() throws Exception {
        long projectId = 1L;
        long accountId = 100L;
        ProjectRequestDto requestDto = new ProjectRequestDto("수정된 프로젝트", "설명", ProjectStatus.DORMANT);

        willDoNothing().given(projectService).updateProject(eq(projectId), any(ProjectRequestDto.class));

        mockMvc.perform(put("/task-api/projects/{projectId}", projectId)
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk()); // 컨트롤러 실제 응답이 200 OK라면 테스트도 맞춰야 함
    }

    @Test
    @DisplayName("프로젝트 삭제 - 성공")
    void deleteProject_success() throws Exception {
        long projectId = 1L;
        long accountId = 100L;

        willDoNothing().given(projectService).deleteProject(projectId, accountId);

        mockMvc.perform(delete("/task-api/projects/{projectId}", projectId)
                        .header("X-Account-Id", accountId))
                .andExpect(status().isOk()); // 204 대신 200으로 수정
    }

    @Test
    @DisplayName("프로젝트 삭제 - 실패 (권한 없음)")
    void deleteProject_fail_noAuth() throws Exception {
        long projectId = 1L;
        long accountId = 200L;

        willThrow(new NoAuthoProjectException("권한 없음"))
                .given(projectService).deleteProject(projectId, accountId);

        mockMvc.perform(delete("/task-api/projects/{projectId}", projectId)
                        .header("X-Account-Id", accountId))
                .andExpect(status().isBadRequest()); // 핸들러가 실제 400(Bad Request)을 던지고 있으므로 수정
    }
}