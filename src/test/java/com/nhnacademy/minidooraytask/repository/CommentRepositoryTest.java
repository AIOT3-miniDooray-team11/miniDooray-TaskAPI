package com.nhnacademy.minidooraytask.repository;

import com.nhnacademy.minidooraytask.comment.domain.Comment;
import com.nhnacademy.minidooraytask.comment.repository.CommentRepository;
import com.nhnacademy.minidooraytask.member.domain.ProjectMember;
import com.nhnacademy.minidooraytask.project.domain.Project;
import com.nhnacademy.minidooraytask.task.domain.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EnableJpaAuditing
class CommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("댓글 작성자 확인")
    void existProjectMemberByTaskIdAndCommentIdANdAccountId() {
        Long accountId = 1L;
        Project project = new Project("테스트", "설명", accountId);
        ProjectMember member = new ProjectMember(project, accountId);
        Task task = new Task(project, member, "테스크", "내용");
        Comment comment = new Comment(task, member, "댓글 내용");

        entityManager.persist(project);
        entityManager.persist(member);
        entityManager.persist(task);
        entityManager.persist(comment);
        entityManager.flush();
        entityManager.clear();
        boolean isExist = commentRepository.findByIdAndTask_Id(comment.getId(), task.getId()).isPresent();
        assertThat(isExist).isTrue();
    }
}
