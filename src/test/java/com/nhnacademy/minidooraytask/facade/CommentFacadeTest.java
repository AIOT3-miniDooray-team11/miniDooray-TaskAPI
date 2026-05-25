package com.nhnacademy.minidooraytask.facade;

import com.nhnacademy.minidooraytask.comment.domain.*;
import com.nhnacademy.minidooraytask.comment.service.CommentFacade;
import com.nhnacademy.minidooraytask.comment.service.CommentService;
import com.nhnacademy.minidooraytask.member.domain.ProjectMember;
import com.nhnacademy.minidooraytask.member.service.ProjectMemberService;
import com.nhnacademy.minidooraytask.task.domain.Task;
import com.nhnacademy.minidooraytask.task.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentFacadeTest {

    @InjectMocks
    private CommentFacade commentFacade;

    @Mock
    private CommentService commentService;

    @Mock
    private ProjectMemberService memberService;

    @Mock
    private TaskService taskService;

    @Test
    @DisplayName("댓글 생성 - 성공")
    void createComment_success() {
        long taskId = 1L;
        long accountId = 100L;
        CommentRequestDto requestDto = new CommentRequestDto("댓글 내용");

        Task mockTask = mock(Task.class);
        ProjectMember mockMember = mock(ProjectMember.class);

        willDoNothing().given(taskService).checkProjectMember(taskId, accountId);
        given(taskService.getTaskById(taskId)).willReturn(mockTask);
        given(memberService.getMemberByTaskId(taskId, accountId)).willReturn(mockMember);
        willDoNothing().given(commentService).createComment(eq(mockTask), eq(mockMember), any(CommentRequestDto.class));

        commentFacade.createComment(taskId, accountId, requestDto);

        then(commentService).should().createComment(eq(mockTask), eq(mockMember), any(CommentRequestDto.class));
    }

    @Test
    @DisplayName("내 댓글 목록 조회 - 성공")
    void getCommentList_success() {
        long accountId = 100L;

        Comment mockComment = mock(Comment.class);
        given(mockComment.getId()).willReturn(1L);
        given(mockComment.getContent()).willReturn("댓글 내용");
        given(mockComment.getCreatedAt()).willReturn(LocalDateTime.now());
        given(mockComment.getUpdatedAt()).willReturn(LocalDateTime.now());

        given(commentService.getCommentsByAccountId(accountId)).willReturn(List.of(mockComment));
        given(memberService.getUserIdByAccountId(accountId)).willReturn("user1");

        CommentListDto result = commentFacade.getCommentList(accountId);

        assertThat(result.commentResponseList()).hasSize(1);
        assertThat(result.commentResponseList().get(0).content()).isEqualTo("댓글 내용");
        assertThat(result.commentResponseList().get(0).userId()).isEqualTo("user1");
    }

    @Test
    @DisplayName("댓글 수정 - 성공")
    void updateComment_success() {
        long taskId = 1L;
        long commentId = 1L;
        long accountId = 100L;
        CommentRequestDto requestDto = new CommentRequestDto("수정된 댓글");

        willDoNothing().given(commentService).checkTaskComment(taskId, commentId, accountId);
        willDoNothing().given(commentService).updateComment(commentId, requestDto);

        commentFacade.updateComment(taskId, commentId, accountId, requestDto);

        then(commentService).should().updateComment(commentId, requestDto);
    }

    @Test
    @DisplayName("댓글 삭제 - 성공")
    void deleteComment_success() {
        long taskId = 1L;
        long commentId = 1L;
        long accountId = 100L;

        willDoNothing().given(commentService).checkTaskComment(taskId, commentId, accountId);
        willDoNothing().given(commentService).deleteComment(commentId);

        commentFacade.deleteComment(taskId, commentId, accountId);

        then(commentService).should().deleteComment(commentId);
    }
}
