package com.nhnacademy.minidooraytask.controller;

import com.nhnacademy.minidooraytask.comment.domain.CommentListDto;
import com.nhnacademy.minidooraytask.comment.domain.CommentResponseDto;
import com.nhnacademy.minidooraytask.comment.service.CommentFacade;
import com.nhnacademy.minidooraytask.handler.CustomExceptionHandler;
import com.nhnacademy.minidooraytask.task.domain.TaskInfoDto;
import com.nhnacademy.minidooraytask.task.domain.TaskInfoListDto;
import com.nhnacademy.minidooraytask.task.service.TaskFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MypageController.class)
@Import(CustomExceptionHandler.class)
public class MyPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskFacade taskFacade;

    @MockitoBean
    private CommentFacade commentFacade;

    @Test
    @DisplayName("내 태스크 조회 - 성공")
    void getMyTasks_success() throws Exception {
        long accountId = 100L;

        TaskInfoDto taskInfoDto = new TaskInfoDto(1L, "내 태스크", null);
        TaskInfoListDto responseDto = new TaskInfoListDto(List.of(taskInfoDto));

        given(taskFacade.getMyTasks(accountId)).willReturn(responseDto);

        mockMvc.perform(get("/task-api/mypage/tasks")
                        .header("X-Account-Id", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskInfoDtoList[0].title").value("내 태스크"));
    }

    @Test
    @DisplayName("내 태스크 조회 - 빈 목록")
    void getMyTasks_empty() throws Exception {
        long accountId = 100L;

        TaskInfoListDto responseDto = new TaskInfoListDto(List.of());
        given(taskFacade.getMyTasks(accountId)).willReturn(responseDto);

        mockMvc.perform(get("/task-api/mypage/tasks")
                        .header("X-Account-Id", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskInfoDtoList").isEmpty());
    }

    @Test
    @DisplayName("내 댓글 조회 - 성공")
    void getMyComments_success() throws Exception {
        long accountId = 100L;

        CommentResponseDto commentResponseDto = new CommentResponseDto(
                1L, accountId, "user1", "댓글 내용", LocalDateTime.now(), LocalDateTime.now()
        );
        CommentListDto responseDto = new CommentListDto(List.of(commentResponseDto));

        given(commentFacade.getCommentList(accountId)).willReturn(responseDto);

        mockMvc.perform(get("/task-api/mypage/comments")
                        .header("X-Account-Id", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentResponseList[0].content").value("댓글 내용"))
                .andExpect(jsonPath("$.commentResponseList[0].userId").value("user1"));
    }

    @Test
    @DisplayName("내 댓글 조회 - 빈 목록")
    void getMyComments_empty() throws Exception {
        long accountId = 100L;

        CommentListDto responseDto = new CommentListDto(List.of());
        given(commentFacade.getCommentList(accountId)).willReturn(responseDto);

        mockMvc.perform(get("/task-api/mypage/comments")
                        .header("X-Account-Id", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentResponseList").isEmpty());
    }
}