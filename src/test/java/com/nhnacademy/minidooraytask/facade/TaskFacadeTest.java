package com.nhnacademy.minidooraytask.facade;


import com.nhnacademy.minidooraytask.member.domain.ProjectMember;
import com.nhnacademy.minidooraytask.member.service.ProjectMemberService;
import com.nhnacademy.minidooraytask.project.domain.Project;
import com.nhnacademy.minidooraytask.project.service.ProjectService;
import com.nhnacademy.minidooraytask.tag.service.TagService;
import com.nhnacademy.minidooraytask.task.domain.Task;
import com.nhnacademy.minidooraytask.task.domain.TaskRequestDto;
import com.nhnacademy.minidooraytask.task.domain.TaskResponseDto;
import com.nhnacademy.minidooraytask.task.service.TaskFacade;
import com.nhnacademy.minidooraytask.task.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TaskFacade.class)
public class TaskFacadeTest {

    @Autowired
    private TaskFacade taskFacade;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private ProjectMemberService projectMemberService;

    @MockitoBean
    private TagService tagService;


    // ===== GET =====

    @Test
    @DisplayName("Facade : Task 생성 흐름 검증 - 성공")
    void createdTask_success() {

        long projectId = 1L;
        long accountId = 2L;

        TaskRequestDto taskRequestDto = new TaskRequestDto("title", "content", List.of("tag1", "tag2"));

        Project mockProject = mock(Project.class);
        ProjectMember mockPmember = mock(ProjectMember.class);
        Task mockTask = mock(Task.class);

        given(projectService.exGetProjectById(projectId)).willReturn(mockProject);
        given(projectMemberService.getActiveMember(projectId, accountId)).willReturn(mockPmember);
        given(taskService.createTask(mockProject, mockPmember, taskRequestDto)).willReturn(mockTask);
        willDoNothing().given(tagService).connectTag(mockTask, taskRequestDto.tagNameList());

        taskFacade.createTask(projectId, accountId, taskRequestDto);

        then(projectService).should().exGetProjectById(projectId);
        then(projectMemberService).should().getActiveMember(projectId, accountId);
        then(taskService).should().createTask(mockProject, mockPmember, taskRequestDto);
        then(tagService).should().connectTag(mockTask, taskRequestDto.tagNameList());
    }

    @Test
    @DisplayName("Facade: Task 수정 - 성공")
    void updateTask_success() {
        long projectId = 1L;
        long taskId = 10L;
        long accountId = 100L;
        long memberId = 50L;
        TaskRequestDto requestDto = new TaskRequestDto("modititle", "modicontent", List.of("newTag"));

        ProjectMember mockMember = mock(ProjectMember.class);
        Task verifiedTask = mock(Task.class);
        Task updatedTask = mock(Task.class);
        TaskResponseDto mockResponseDto = mock(TaskResponseDto.class);

        given(projectMemberService.getActiveMember(projectId, accountId)).willReturn(mockMember);
        given(mockMember.getId()).willReturn(memberId);

        willDoNothing().given(taskService).checkTaskMaker(memberId, taskId);

        given(taskService.getTaskById(taskId)).willReturn(verifiedTask);
        given(taskService.updateTask(verifiedTask, requestDto)).willReturn(updatedTask);
        willDoNothing().given(tagService).connectTag(updatedTask, requestDto.tagNameList());
        given(taskService.buildTaskResponseDto(updatedTask)).willReturn(mockResponseDto);

        TaskResponseDto result = taskFacade.updateTask(projectId, taskId, accountId, requestDto);

        assertThat(result).isEqualTo(mockResponseDto);
        then(projectMemberService).should().getActiveMember(projectId, accountId);
        then(taskService).should().checkTaskMaker(memberId, taskId);
        then(taskService).should().updateTask(verifiedTask, requestDto);
        then(tagService).should().connectTag(updatedTask, requestDto.tagNameList());
    }


}
