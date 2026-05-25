package com.nhnacademy.minidooraytask.repository;

import com.nhnacademy.minidooraytask.MileStone.domain.MileStone;
import com.nhnacademy.minidooraytask.MileStone.domain.MileStoneStatus;
import com.nhnacademy.minidooraytask.MileStone.repository.MileStoneRepository;
import com.nhnacademy.minidooraytask.member.domain.ProjectMember;
import com.nhnacademy.minidooraytask.project.domain.Project;
import com.nhnacademy.minidooraytask.task.domain.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MileStoneRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MileStoneRepository mileStoneRepository;

    @Test
    @DisplayName("프로젝트 ID로 마일스톤 리스트 조회 검증")
    void getMileStoneListByProjectId() {
        Long accountId = 1L;
        Project project = new Project("ttl", "des", accountId);
        ProjectMember member = new ProjectMember(project, accountId);
        Task task = new Task(project, member, "task", "content");

        MileStone mileStone = new MileStone(task, "ttl", "content", MileStoneStatus.PLANNED, LocalDateTime.now());

        entityManager.persist(project);
        entityManager.persist(member);
        entityManager.persist(task);
        entityManager.persist(mileStone);
        entityManager.flush();
        entityManager.clear();

        List<MileStone> mileStoneList = mileStoneRepository.getMileStoneByProjectId(project.getId());
        assertThat(mileStoneList).hasSize(1);
        assertThat(mileStoneList.get(0).getTitle()).isEqualTo("ttl");
        assertThat(mileStoneList.get(0).getTask().getProject().getId()).isEqualTo(project.getId());
    }
}
