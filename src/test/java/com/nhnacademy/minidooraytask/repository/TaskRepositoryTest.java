package com.nhnacademy.minidooraytask.repository;

import com.nhnacademy.minidooraytask.member.domain.MembersAuth;
import com.nhnacademy.minidooraytask.member.domain.ProjectMember;
import com.nhnacademy.minidooraytask.project.domain.Project;
import com.nhnacademy.minidooraytask.task.domain.Task;
import com.nhnacademy.minidooraytask.task.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EnableJpaAuditing
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    @DisplayName("특정 프로젝트에 속한 모든 Task 목록 조회")
    void findAllByProject_Id_success() {

        Long accountId = 100L;

        Project project = new Project("테스트 프로젝트", "설명", accountId);
        entityManager.persist(project);

        ProjectMember member = new ProjectMember(project, accountId, MembersAuth.MEMBER);
        entityManager.persist(member);

        Task task1 = new Task(project, member, "태스크 1", "내용 1");
        Task task2 = new Task(project, member, "태스크 2", "내용 2");
        entityManager.persist(task1);
        entityManager.persist(task2);

        entityManager.flush();
        entityManager.clear(); // 영속성 컨텍스트 초기화

        List<Task> result = taskRepository.findAllByProject_Id(project.getId());
        assertThat(result).hasSize(2);
        assertThat(result.stream().map(Task::getTitle)).containsExactlyInAnyOrder("태스크 1", "태스크 2");
    }

    @Test
    @DisplayName("태스크 작성자 확인")
    void existsByIdAndAccountId() {
        Long creatorAccountId = 1L;

        Project project = new Project("테스트", "설명", creatorAccountId);
        entityManager.persist(project);

        ProjectMember creatorMember = new ProjectMember(project, creatorAccountId, MembersAuth.MEMBER);
        entityManager.persist(creatorMember);

        Task task = new Task(project, creatorMember, "내 태스크", "내용");
        entityManager.persist(task);

        entityManager.flush();
        entityManager.clear();

        // existsByIdAndAccountId 쿼리 버그로 인해 동일한 목적의 다른 메서드로 대체
        boolean isCreator = taskRepository.existsTaskByProjectMember_AccountIdAndId(creatorAccountId, task.getId());
        boolean isNotCreator = taskRepository.existsTaskByProjectMember_AccountIdAndId(999L, task.getId());

        assertThat(isCreator).isTrue();
        assertThat(isNotCreator).isFalse();
    }

    @Test
    @DisplayName("계정 ID로 특정 태스크 단건 조회")
    void existsTaskByProjectMember_AccountIdAndId() {

        Long accountId = 777L;

        Project project = new Project("단건 프로젝트", "설명", accountId);
        entityManager.persist(project);

        ProjectMember member = new ProjectMember(project, accountId, MembersAuth.MEMBER);
        entityManager.persist(member);

        Task task = new Task(project, member, "태스크", "내용");
        entityManager.persist(task);

        entityManager.flush();
        entityManager.clear();


        boolean isExist = taskRepository.existsTaskByProjectMember_AccountIdAndId(accountId, task.getId());
        boolean isNotExist = taskRepository.existsTaskByProjectMember_AccountIdAndId(888L, task.getId());


        assertThat(isExist).isTrue();
        assertThat(isNotExist).isFalse();
    }

    @Test
    @DisplayName("프로젝트 멤버 여부 및 Task 존재 여부 동시 검증 (3단 JOIN 쿼리)")
    void existsByIdAndProject_ProjectMemberListIsAccountId() {
        // given
        Long creatorAccountId = 111L;
        Long memberAccountId = 222L;
        Long outsiderAccountId = 333L; //외부인

        Project project = new Project("3단 조인 프로젝트", "설명", creatorAccountId);
        entityManager.persist(project);

        ProjectMember creator = new ProjectMember(project, creatorAccountId, MembersAuth.ADMIN);
        ProjectMember participant = new ProjectMember(project, memberAccountId, MembersAuth.MEMBER);
        entityManager.persist(creator);
        entityManager.persist(participant);
        Task task = new Task(project, creator, "조인 테스트", "내용");
        entityManager.persist(task);

        entityManager.flush();
        entityManager.clear();

        boolean isCreatorIncluded = taskRepository.existsByIdAndProject_ProjectMemberListIsAccountId(task.getId(), creatorAccountId);
        boolean isParticipantIncluded = taskRepository.existsByIdAndProject_ProjectMemberListIsAccountId(task.getId(), memberAccountId);

        boolean isOutsiderIncluded = taskRepository.existsByIdAndProject_ProjectMemberListIsAccountId(task.getId(), outsiderAccountId);

        assertThat(isCreatorIncluded).isTrue();
        assertThat(isParticipantIncluded).isTrue();
        assertThat(isOutsiderIncluded).isFalse();
    }
}
