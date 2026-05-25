package com.nhnacademy.minidooraytask.controller;

import com.nhnacademy.minidooraytask.comment.domain.CommentRequestDto;
import com.nhnacademy.minidooraytask.comment.exception.CommentNotFoundException;
import com.nhnacademy.minidooraytask.comment.service.CommentFacade;
import com.nhnacademy.minidooraytask.handler.CustomExceptionHandler;
import com.nhnacademy.minidooraytask.member.exception.ProjectMemberIsNotExistException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@Import(CustomExceptionHandler.class)
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentFacade commentFacade;

    @Test
    @DisplayName("댓글 생성 - 성공")
    void createComment_success() throws Exception {
        long taskId = 1L;
        long accountId = 100L;
        CommentRequestDto requestDto = new CommentRequestDto("댓글 내용");

        willDoNothing().given(commentFacade).createComment(eq(taskId), eq(accountId), any(CommentRequestDto.class));

        mockMvc.perform(post("/task-api/tasks/{taskId}/comments", taskId)
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 생성 - 실패 (프로젝트 멤버 아님)")
    void createComment_fail_notMember() throws Exception {
        long taskId = 1L;
        long accountId = 999L;
        CommentRequestDto requestDto = new CommentRequestDto("댓글 내용");

        willThrow(new ProjectMemberIsNotExistException("프로젝트 멤버가 아닙니다"))
                .given(commentFacade).createComment(eq(taskId), eq(accountId), any(CommentRequestDto.class));

        mockMvc.perform(post("/task-api/tasks/{taskId}/comments", taskId)
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 수정 - 성공")
    void updateComment_success() throws Exception {
        long taskId = 1L;
        long commentId = 1L;
        long accountId = 100L;
        CommentRequestDto requestDto = new CommentRequestDto("수정된 댓글");

        willDoNothing().given(commentFacade).updateComment(eq(taskId), eq(commentId), eq(accountId), any(CommentRequestDto.class));

        mockMvc.perform(post("/task-api/tasks/{taskId}/comments/{commentId}", taskId, commentId)
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 수정 - 실패 (댓글 없음)")
    void updateComment_fail_notFound() throws Exception {
        long taskId = 1L;
        long commentId = 999L;
        long accountId = 100L;
        CommentRequestDto requestDto = new CommentRequestDto("수정된 댓글");

        willThrow(new CommentNotFoundException("댓글이 존재하지 않습니다"))
                .given(commentFacade).updateComment(eq(taskId), eq(commentId), eq(accountId), any(CommentRequestDto.class));

        mockMvc.perform(post("/task-api/tasks/{taskId}/comments/{commentId}", taskId, commentId)
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 삭제 - 성공")
    void deleteComment_success() throws Exception {
        long taskId = 1L;
        long commentId = 1L;
        long accountId = 100L;

        willDoNothing().given(commentFacade).deleteComment(eq(taskId), eq(commentId), eq(accountId));

        mockMvc.perform(delete("/task-api/tasks/{taskId}/comments/{commentId}", taskId, commentId)
                        .header("X-Account-Id", accountId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 삭제 - 실패 (댓글 없음)")
    void deleteComment_fail_notFound() throws Exception {
        long taskId = 1L;
        long commentId = 999L;
        long accountId = 100L;

        willThrow(new CommentNotFoundException("댓글이 존재하지 않습니다"))
                .given(commentFacade).deleteComment(eq(taskId), eq(commentId), eq(accountId));

        mockMvc.perform(delete("/task-api/tasks/{taskId}/comments/{commentId}", taskId, commentId)
                        .header("X-Account-Id", accountId))
                .andExpect(status().isBadRequest());
    }
}
