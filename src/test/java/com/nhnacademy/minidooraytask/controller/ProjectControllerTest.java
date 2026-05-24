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

    // ===== GET =====

    @Test
    @DisplayName("내 프로젝트 목록 조회 - 성공")
    void getMyProjects_success() throws Exception {
        // given
        long accountId = 100L;

        ProjectInfoDto projectInfo = new ProjectInfoDto(1L, "Test Project", ProjectStatus.ACTIVE, List.of(MileStoneStatus.IN_PROGRESS));
        TaskInfoDto taskInfo = new TaskInfoDto(10L, "Test Task", MileStoneStatus.IN_PROGRESS);
        ProjectViewDto mockResponse = new ProjectViewDto(List.of(projectInfo), List.of(taskInfo));

        given(projectFacade.getProjectView(accountId)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/task-api/projects")
                        .header("X-Account-Id", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectInfoDtoList[0].id").value(1L))
                .andExpect(jsonPath("$.projectInfoDtoList[0].title").value("Test Project"))
                .andExpect(jsonPath("$.projectInfoDtoList[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.taskInfoDtoList[0].id").value(10L))
                .andExpect(jsonPath("$.taskInfoDtoList[0].title").value("Test Task"));
    }

    // ===== POST =====

    @Test
    @DisplayName("프로젝트 생성 - 성공")
    void createProject_success() throws Exception {
        long accountId = 100L;
        ProjectRequestDto requestDto = new ProjectRequestDto("새로운 프로젝트", "프로젝트 설명입니다.", ProjectStatus.ACTIVE);

        willDoNothing().given(projectService).createProject(eq(accountId), any(ProjectRequestDto.class));

        mockMvc.perform(post("/task-api/projects")
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());
    }

    // ===== PUT =====

    @Test
    @DisplayName("프로젝트 수정 - 성공")
    void updateProject_success() throws Exception {
        long projectId = 1L;
        long accountId = 100L;
        ProjectRequestDto requestDto = new ProjectRequestDto("수정된 프로젝트", "수정된 설명입니다.", ProjectStatus.DORMANT);

        willDoNothing().given(projectService).updateProject(eq(projectId), any(ProjectRequestDto.class));

        mockMvc.perform(put("/task-api/projects/{projectId}", projectId)
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNoContent()); // 컨트롤러에서 ResponseEntity.noContent()를 반환
    }

    // ===== DELETE =====

    @Test
    @DisplayName("프로젝트 삭제 - 성공")
    void deleteProject_success() throws Exception {

        long projectId = 1L;
        long accountId = 100L;

        willDoNothing().given(projectService).deleteProject(projectId, accountId);


        mockMvc.perform(delete("/task-api/projects/{projectId}", projectId)
                        .header("X-Account-Id", accountId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("프로젝트 삭제 - 실패 (관리자 권한 없음)")
    void deleteProject_fail_noAuth() throws Exception {

        long projectId = 1L;
        long accountId = 200L;

        willThrow(new NoAuthoProjectException("프로젝트 삭제 권한이 없습니다"))
                .given(projectService).deleteProject(projectId, accountId);


        mockMvc.perform(delete("/task-api/projects/{projectId}", projectId)
                        .header("X-Account-Id", accountId))
                .andExpect(status().isForbidden()); // CustomExceptionHandler에 의해 403 Forbidden 반환 검증
    }
}
