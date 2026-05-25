package com.nhnacademy.minidooraytask.facade;

import com.nhnacademy.minidooraytask.MileStone.domain.MileStone;
import com.nhnacademy.minidooraytask.MileStone.domain.MileStoneStatus;
import com.nhnacademy.minidooraytask.member.domain.ProjectMember;
import com.nhnacademy.minidooraytask.member.service.ProjectMemberService;
import com.nhnacademy.minidooraytask.project.domain.Project;
import com.nhnacademy.minidooraytask.project.domain.ProjectViewDto;
import com.nhnacademy.minidooraytask.project.service.ProjectFacade;
import com.nhnacademy.minidooraytask.project.service.ProjectService;
import com.nhnacademy.minidooraytask.task.domain.Task;
import com.nhnacademy.minidooraytask.task.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ProjectFacadeTest {

    @InjectMocks
    private ProjectFacade projectFacade;

    @Mock
    private ProjectService projectService;

    @Mock
    private TaskService taskService;

    @Mock
    private ProjectMemberService projectMemberService;

    @Test
    @DisplayName("Facade: 내 프로젝트 뷰 조회 - 삭제된 프로젝트 필터링 및 마일스톤 매핑 검증")
    void getProjectView_success() {
        long accountId = 1L;

        //프로젝트 생성
        Project activeProject = new Project("활성화된 프로젝트", "설명", accountId);
        ReflectionTestUtils.setField(activeProject, "id", 10L);
        ReflectionTestUtils.setField(activeProject, "isDeleted", false);

        Project deletedProject = new Project("삭제된 프로젝트", "설명", accountId);
        ReflectionTestUtils.setField(deletedProject, "id", 20L);
        ReflectionTestUtils.setField(deletedProject, "isDeleted", true);

        ProjectMember member1 = mock(ProjectMember.class);
        ProjectMember member2 = mock(ProjectMember.class);

        given(member1.getProject()).willReturn(activeProject);
        given(member2.getProject()).willReturn(deletedProject);

        // Task 생성
        Task task1 = new Task(activeProject, member1, "태스크 1", "내용");
        ReflectionTestUtils.setField(task1, "id", 100L);

        Task task2 = new Task(activeProject, member1, "태스크 2", "내용");
        ReflectionTestUtils.setField(task2, "id", 101L);

        //마일스톤
        MileStone mockMilestone = mock(MileStone.class);
        given(mockMilestone.getStatus()).willReturn(MileStoneStatus.IN_PROGRESS);
        task1.setMilestone(mockMilestone);

        List<Task> taskList = List.of(task1, task2);
        activeProject.getTaskList().addAll(taskList);

        given(member1.getTaskList()).willReturn(taskList);
        given(member2.getTaskList()).willReturn(List.of());

        given(projectMemberService.getProjectMemberByAccountId(accountId))
                .willReturn(List.of(member1, member2));

        ProjectViewDto result = projectFacade.getProjectView(accountId);

        assertThat(result.projectInfoDtoList()).hasSize(1);
        assertThat(result.projectInfoDtoList().getFirst().id()).isEqualTo(10L);

        assertThat(result.taskInfoDtoList()).hasSize(2);

        assertThat(result.taskInfoDtoList().get(0).id()).isEqualTo(100L);
        assertThat(result.taskInfoDtoList().get(0).status()).isEqualTo(MileStoneStatus.IN_PROGRESS);

        assertThat(result.taskInfoDtoList().get(1).id()).isEqualTo(101L);
        assertThat(result.taskInfoDtoList().get(1).status()).isNull();
    }
}