package com.nhnacademy.minidooraytask.controller;

import com.nhnacademy.minidooraytask.comment.domain.CommentListDto;
import com.nhnacademy.minidooraytask.comment.service.CommentFacade;
import com.nhnacademy.minidooraytask.task.domain.TaskInfoListDto;
import com.nhnacademy.minidooraytask.task.service.TaskFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/task-api/mypage")
@RestController
@RequiredArgsConstructor
public class MypageController {

    private final TaskFacade taskFacade;
    private final CommentFacade commentFacade;

    @GetMapping("/tasks")
    public ResponseEntity<TaskInfoListDto> getMyTasks(@RequestHeader("X-Account-Id") Long accountId) {
        TaskInfoListDto responseDto = taskFacade.getMyTasks(accountId);
        return ResponseEntity.ok().body(responseDto);
    }

    //마이페이지에서 내가 작성한 Comment 목록 (GET)
    @GetMapping("/comments")
    public ResponseEntity<CommentListDto> getCommentResponseDtoList(@RequestHeader("X-Account-Id") Long accountId) {
        CommentListDto commentListDto = commentFacade.getCommentList(accountId);
        return ResponseEntity.ok().body(commentListDto);
    }
}