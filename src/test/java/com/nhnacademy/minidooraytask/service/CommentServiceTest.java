package com.nhnacademy.minidooraytask.service;

import com.nhnacademy.minidooraytask.comment.exception.CommentNotFoundException;
import com.nhnacademy.minidooraytask.comment.repository.CommentRepository;
import com.nhnacademy.minidooraytask.comment.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Test
    @DisplayName("댓글 수정/삭제할 때 본인 확인")
    void checkTaskComment_fail_notAuthorized() {

        long taskId = 1L;
        long commentId = 100L;
        long accountId = 999L; // 다른 사람ID


        given(commentRepository.existProjectMemberByTaskIdAndCommentIdANdAccountId(taskId, commentId, accountId))
                .willReturn(true);
        assertThatThrownBy(() -> commentService.checkTaskComment(taskId, commentId, accountId))
                .isInstanceOf(CommentNotFoundException.class);
    }
}
