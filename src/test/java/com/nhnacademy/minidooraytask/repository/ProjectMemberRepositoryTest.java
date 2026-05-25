package com.nhnacademy.minidooraytask.repository;

import com.nhnacademy.minidooraytask.member.domain.MembersAuth;
import com.nhnacademy.minidooraytask.member.domain.ProjectMember;
import com.nhnacademy.minidooraytask.member.repository.ProjectMemberRepository;
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
class ProjectMemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Test
    @DisplayName("계정 ID와 권한(Auth)으로 멤버 존재 여부 확인")
    void existsByProject_IdAndAccountIdAndAuth() {

        Long adminAccountId = 111L;
        Project project = new Project("권한 테스트 프로젝트", "설명", adminAccountId);
        entityManager.persist(project);
        ProjectMember adminMember = new ProjectMember(project, adminAccountId, MembersAuth.ADMIN);
        entityManager.persist(adminMember);

        entityManager.flush();
        entityManager.clear();

        boolean isAdminCorrect = projectMemberRepository.existsByProject_IdAndAccountIdAndAuth(project.getId(), adminAccountId, MembersAuth.ADMIN);
        boolean isMemberIncorrect = projectMemberRepository.existsByProject_IdAndAccountIdAndAuth(project.getId(), adminAccountId, MembersAuth.MEMBER);


        assertThat(isAdminCorrect).isTrue();
        assertThat(isMemberIncorrect).isFalse();
    }

    @Test
    @DisplayName("특정 Task ID와 Account ID로 멤버 역추적 조회 (3단 조인 쿼리)")
    void findProjectMemberByTaskIdAndAccountId() {

        Long accountId = 777L;
        Project project = new Project("조인 테스트 프로젝트", "설명", accountId);
        entityManager.persist(project);

        ProjectMember member = new ProjectMember(project, accountId, MembersAuth.MEMBER);
        entityManager.persist(member);

        Task task = new Task(project, member, "멤버 역추적 태스크", "내용");
        entityManager.persist(task);

        entityManager.flush();
        entityManager.clear();
        ProjectMember foundMember = projectMemberRepository.findProjectMemberByTaskIdAndAccountId(task.getId(), accountId);
        ProjectMember notFoundMember = projectMemberRepository.findProjectMemberByTaskIdAndAccountId(task.getId(), 999L);

        assertThat(foundMember).isNotNull();
        assertThat(foundMember.getAccountId()).isEqualTo(accountId);

        assertThat(notFoundMember).isNull();
    }

    @Test
    @DisplayName("멤버 ID로 특정 계정이 해당 멤버의 소유자인지 확인")
    void existsProjectMemberByIdAndAccountId() {
        Long accountId = 123L;
        Project project = new Project("소유권 프로젝트", "설명", accountId);
        entityManager.persist(project);

        ProjectMember member = new ProjectMember(project, accountId, MembersAuth.MEMBER);
        entityManager.persist(member);

        entityManager.flush();
        entityManager.clear();

        boolean isOwner = projectMemberRepository.existsProjectMemberByIdAndAccountId(member.getId(), accountId);
        boolean isNotOwner = projectMemberRepository.existsProjectMemberByIdAndAccountId(member.getId(), 456L);
        assertThat(isOwner).isTrue();
        assertThat(isNotOwner).isFalse();
    }
}