package com.nhnacademy.minidooraytask.repository;

import com.nhnacademy.minidooraytask.member.domain.ProjectMember;
import com.nhnacademy.minidooraytask.project.domain.Project;
import com.nhnacademy.minidooraytask.project.respository.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EnableJpaAuditing
class ProjectRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("계정 ID로 프로젝트 목록 조회")
    void findAllByAccountIdByQuery_success() {
        Long accountId = 100L;

        Project project = new Project("조인 테스트 프로젝트", "설명입니다.", accountId);
        entityManager.persist(project);

        ProjectMember member = new ProjectMember(project, accountId);
        entityManager.persist(member);

        entityManager.flush();
        entityManager.clear();

        List<Project> result = projectRepository.findAllByAccountIdByQuery(accountId, false);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("조인 테스트 프로젝트");
    }

    @Test
    @DisplayName("생성자 ID와 프로젝트 ID로 프로젝트 존재 확인")
    void existsProjectByIdAndCreateAccountId() {
        Long creatorId = 999L;
        Project project = new Project("소유권 테스트", "설명", creatorId);
        entityManager.persist(project);

        entityManager.flush();
        entityManager.clear();

        boolean isExist = projectRepository.existsProjectByIdAndCreateAccountId(project.getId(), creatorId);
        boolean isNotExist = projectRepository.existsProjectByIdAndCreateAccountId(project.getId(), 111L);

        assertThat(isExist).isTrue();
        assertThat(isNotExist).isFalse();
    }

    @Test
    @DisplayName("생성자 ID, 프로젝트 ID로 프로젝트 조회")
    void findProjectByIdAndCreateAccountId() {
        Long creatorId = 555L;
        Project project = new Project("단건 조회 테스트", "설명", creatorId);
        entityManager.persist(project);

        entityManager.flush();
        entityManager.clear();

        Optional<Project> foundProject = projectRepository.findProjectByIdAndCreateAccountId(project.getId(), creatorId);
        Optional<Project> notFoundProject = projectRepository.findProjectByIdAndCreateAccountId(project.getId(), 777L);

        assertThat(foundProject).isPresent();
        assertThat(foundProject.get().getTitle()).isEqualTo("단건 조회 테스트");
        assertThat(notFoundProject).isEmpty();
    }
}