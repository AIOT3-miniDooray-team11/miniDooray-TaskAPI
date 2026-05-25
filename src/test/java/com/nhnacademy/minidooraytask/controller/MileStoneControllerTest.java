package com.nhnacademy.minidooraytask.controller;

import com.nhnacademy.minidooraytask.MileStone.domain.MileStoneStatus;
import com.nhnacademy.minidooraytask.MileStone.domain.MilestoneRequestDto;
import com.nhnacademy.minidooraytask.MileStone.service.MileStoneFacade;
import com.nhnacademy.minidooraytask.handler.CustomExceptionHandler;
import com.nhnacademy.minidooraytask.task.exception.TaskNotFoundException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MileStoneController.class)
@Import(CustomExceptionHandler.class)
public class MileStoneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MileStoneFacade mileStoneFacade;

    @Test
    @DisplayName("마일스톤 생성 - 성공")
    void createMileStone_success() throws Exception {
        long taskId = 1L;
        long accountId = 100L;
        MilestoneRequestDto requestDto = new MilestoneRequestDto(
                "마일스톤 제목", "설명", MileStoneStatus.IN_PROGRESS, LocalDateTime.now().plusDays(7)
        );

        willDoNothing().given(mileStoneFacade).createMilestone(eq(taskId), eq(accountId), any(MilestoneRequestDto.class));

        mockMvc.perform(post("/task-api/tasks/{taskId}/milestone", taskId)
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("마일스톤 생성 - 실패 (태스크 없음)")
    void createMileStone_fail_taskNotFound() throws Exception {
        long taskId = 999L;
        long accountId = 100L;
        MilestoneRequestDto requestDto = new MilestoneRequestDto(
                "마일스톤 제목", "설명", MileStoneStatus.IN_PROGRESS, LocalDateTime.now().plusDays(7)
        );

        willThrow(new TaskNotFoundException("태스크가 존재하지 않습니다"))
                .given(mileStoneFacade).createMilestone(eq(taskId), eq(accountId), any(MilestoneRequestDto.class));

        mockMvc.perform(post("/task-api/tasks/{taskId}/milestone", taskId)
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("마일스톤 수정 - 성공")
    void updateMileStone_success() throws Exception {
        long taskId = 1L;
        long accountId = 100L;
        MilestoneRequestDto requestDto = new MilestoneRequestDto(
                "수정된 제목", "수정된 설명", MileStoneStatus.COMPLETED, LocalDateTime.now().plusDays(14)
        );

        willDoNothing().given(mileStoneFacade).updateMilestone(eq(taskId), eq(accountId), any(MilestoneRequestDto.class));

        mockMvc.perform(put("/task-api/tasks/{taskId}/milestone", taskId)
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("마일스톤 수정 - 실패 (태스크 없음)")
    void updateMileStone_fail_taskNotFound() throws Exception {
        long taskId = 999L;
        long accountId = 100L;
        MilestoneRequestDto requestDto = new MilestoneRequestDto(
                "수정된 제목", "수정된 설명", MileStoneStatus.COMPLETED, LocalDateTime.now().plusDays(14)
        );

        willThrow(new TaskNotFoundException("태스크가 존재하지 않습니다"))
                .given(mileStoneFacade).updateMilestone(eq(taskId), eq(accountId), any(MilestoneRequestDto.class));

        mockMvc.perform(put("/task-api/tasks/{taskId}/milestone", taskId)
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("마일스톤 삭제 - 성공")
    void deleteMileStone_success() throws Exception {
        long taskId = 1L;
        long accountId = 100L;

        willDoNothing().given(mileStoneFacade).deleteMilestone(eq(taskId), eq(accountId));

        mockMvc.perform(delete("/task-api/tasks/{taskId}/milestone", taskId)
                        .header("X-Account-Id", accountId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("마일스톤 삭제 - 실패 (태스크 없음)")
    void deleteMileStone_fail_taskNotFound() throws Exception {
        long taskId = 999L;
        long accountId = 100L;

        willThrow(new TaskNotFoundException("태스크가 존재하지 않습니다"))
                .given(mileStoneFacade).deleteMilestone(eq(taskId), eq(accountId));

        mockMvc.perform(delete("/task-api/tasks/{taskId}/milestone", taskId)
                        .header("X-Account-Id", accountId))
                .andExpect(status().isBadRequest());
    }
}
