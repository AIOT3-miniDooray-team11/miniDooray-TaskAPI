package com.nhnacademy.minidooraytask.service;

import com.nhnacademy.minidooraytask.MileStone.domain.MileStone;
import com.nhnacademy.minidooraytask.MileStone.domain.MileStoneStatus;
import com.nhnacademy.minidooraytask.MileStone.repository.MileStoneRepository;
import com.nhnacademy.minidooraytask.member.domain.MembersAuth;
import com.nhnacademy.minidooraytask.member.domain.ProjectMember;
import com.nhnacademy.minidooraytask.member.repository.ProjectMemberRepository;
import com.nhnacademy.minidooraytask.project.domain.Project;
import com.nhnacademy.minidooraytask.project.respository.ProjectRepository;
import com.nhnacademy.minidooraytask.tag.domain.Tag;
import com.nhnacademy.minidooraytask.tag.domain.TaskTag;
import com.nhnacademy.minidooraytask.tag.repository.TagRepository;
import com.nhnacademy.minidooraytask.tag.repository.TaskTagRepository;
import com.nhnacademy.minidooraytask.task.domain.Task;
import com.nhnacademy.minidooraytask.task.domain.TaskRequestDto;
import com.nhnacademy.minidooraytask.task.domain.TaskResponseDto;
import com.nhnacademy.minidooraytask.task.exception.TaskNotFoundException;
import com.nhnacademy.minidooraytask.task.repository.TaskRepository;
import com.nhnacademy.minidooraytask.task.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TaskService.class)
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @MockitoBean
    private TaskRepository taskRepository;

    @MockitoBean
    private TaskTagRepository taskTagRepository;

    @MockitoBean
    private TagRepository tagRepository;

    @MockitoBean
    private MileStoneRepository milestoneRepository;

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private ProjectMemberRepository projectMemberRepository;

    @Test
    @DisplayName("프로젝트 ID로 Task 목록 조회 - 성공")
    void getTasks_success() {
        Long projectId = 1L;
        Task task1 = mock(Task.class);
        Task task2 = mock(Task.class);
        given(taskRepository.findAllByProject_Id(projectId)).willReturn(List.of(task1, task2));

        List<Task> result = taskService.getTasks(projectId);

        assertThat(result).hasSize(2);
        then(taskRepository).should().findAllByProject_Id(projectId);
    }

    @Test
    @DisplayName("프로젝트에 Task가 없으면 빈 리스트 반환")
    void getTasks_empty() {
        Long projectId = 1L;
        given(taskRepository.findAllByProject_Id(projectId)).willReturn(List.of());

        List<Task> result = taskService.getTasks(projectId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Task 단건 조회 - 성공")
    void getSpecificTask_success() {
        Long taskId = 1L;
        Long projectId = 1L;
        Task task = mock(Task.class);
        given(taskRepository.findByIdAndProject_Id(taskId, projectId)).willReturn(Optional.of(task));

        Task result = taskService.getSpecificTask(taskId, projectId);

        assertThat(result).isEqualTo(task);
        then(taskRepository).should().findByIdAndProject_Id(taskId, projectId);
    }

    @Test
    @DisplayName("Task 단건 조회 - 존재하지 않으면 TaskNotFoundException 발생")
    void getSpecificTask_notFound() {
        Long taskId = 999L;
        Long projectId = 1L;
        given(taskRepository.findByIdAndProject_Id(taskId, projectId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getSpecificTask(taskId, projectId))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("Task 작성자 확인 - 작성자가 맞으면 예외 없음")
    void checkTaskMaker_success() {
        long memberId = 1L;
        long taskId = 1L;
        given(taskRepository.existsByIdAndAccountId(taskId, memberId)).willReturn(true);

        assertThatNoException().isThrownBy(() -> taskService.checkTaskMaker(memberId, taskId));
    }

    @Test
    @DisplayName("Task 작성자 확인 - 작성자가 아니면 TaskNotFoundException 발생")
    void checkTaskMaker_notMaker() {
        Long memberId = 2L;
        Long taskId = 1L;
        given(taskRepository.existsByIdAndAccountId(taskId, memberId)).willReturn(false);

        assertThatThrownBy(() -> taskService.checkTaskMaker(memberId, taskId))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("AccountId로 Task 작성자 확인 - 작성자가 맞으면 예외 없음")
    void checkTaskMakerByAccountId_success() {
        long accountId = 1L;
        long taskId = 1L;
        given(taskRepository.existsTaskByProjectMember_AccountIdAndId(accountId, taskId)).willReturn(true);

        assertThatNoException().isThrownBy(() -> taskService.checkTaskMakerByAccountId(accountId, taskId));
    }

    @Test
    @DisplayName("AccountId로 Task 작성자 확인 - 작성자가 아니면 TaskNotFoundException 발생")
    void checkTaskMakerByAccountId_notMaker() {
        long accountId = 2L;
        long taskId = 1L;
        given(taskRepository.existsTaskByProjectMember_AccountIdAndId(accountId, taskId)).willReturn(false);

        assertThatThrownBy(() -> taskService.checkTaskMakerByAccountId(accountId, taskId))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("ID로 Task 조회")
    void getTaskById_success() {
        long taskId = 1L;
        Task task = mock(Task.class);
        given(taskRepository.findTaskById(taskId)).willReturn(task);

        Task result = taskService.getTaskById(taskId);

        assertThat(result).isEqualTo(task);
        then(taskRepository).should().findTaskById(taskId);
    }

    @Test
    @DisplayName("Task 생성 - 성공")
    void createTask_success() {
        Project project = new Project("test project", "desc", 1L);
        ProjectMember projectMember = new ProjectMember(project, 1L, MembersAuth.MEMBER);
        TaskRequestDto request = new TaskRequestDto("title", "content", List.of());
        Task savedTask = mock(Task.class);
        given(taskRepository.save(any(Task.class))).willReturn(savedTask);

        Task result = taskService.createTask(project, projectMember, request);

        assertThat(result).isEqualTo(savedTask);
        then(taskRepository).should().save(any(Task.class));
    }

    @Test
    @DisplayName("Task 수정 - 제목과 내용이 변경됨")
    void updateTask_titleAndContentChanged() {
        Project project = new Project("project", "desc", 1L);
        ProjectMember projectMember = new ProjectMember(project, 1L, MembersAuth.MEMBER);
        Task task = new Task(project, projectMember, "old title", "old content");
        TaskRequestDto request = new TaskRequestDto("new title", "new content", List.of());
        given(taskRepository.save(task)).willReturn(task);

        Task result = taskService.updateTask(task, request);

        assertThat(result.getTitle()).isEqualTo("new title");
        assertThat(result.getContent()).isEqualTo("new content");
        then(taskRepository).should().save(task);
    }

    @Test
    @DisplayName("Task 수정 - 제목과 내용이 동일하면 save만 호출")
    void updateTask_sameValues_saveStillCalled() {
        Project project = new Project("project", "desc", 1L);
        ProjectMember projectMember = new ProjectMember(project, 1L, MembersAuth.MEMBER);
        Task task = new Task(project, projectMember, "same title", "same content");
        TaskRequestDto request = new TaskRequestDto("same title", "same content", List.of());
        given(taskRepository.save(task)).willReturn(task);

        Task result = taskService.updateTask(task, request);

        assertThat(result.getTitle()).isEqualTo("same title");
        assertThat(result.getContent()).isEqualTo("same content");
        then(taskRepository).should().save(task);
    }

    @Test
    @DisplayName("Task 삭제 - 성공")
    void deleteTask_success() {
        Task task = mock(Task.class);
        willDoNothing().given(taskRepository).delete(task);

        taskService.deleteTask(task);

        then(taskRepository).should().delete(task);
    }

    @Test
    @DisplayName("TaskResponseDto 빌드 - 마일스톤과 태그가 있는 경우")
    void buildTaskResponseDto_withMilestoneAndTags() {
        Task task = mock(Task.class);
        Project project = mock(Project.class);
        ProjectMember projectMember = mock(ProjectMember.class);
        MileStone mileStone = mock(MileStone.class);
        Tag tag = mock(Tag.class);
        TaskTag taskTag = mock(TaskTag.class);
        LocalDateTime now = LocalDateTime.now();

        given(task.getId()).willReturn(1L);
        given(task.getProject()).willReturn(project);
        given(task.getProjectMember()).willReturn(projectMember);
        given(task.getTitle()).willReturn("test title");
        given(task.getContent()).willReturn("test content");
        given(task.getCreatedAt()).willReturn(now);
        given(task.getUpdatedAt()).willReturn(now);
        given(task.getMilestone()).willReturn(mileStone);
        given(task.getTaskTagList()).willReturn(List.of(taskTag));

        given(project.getId()).willReturn(1L);
        given(projectMember.getId()).willReturn(1L);

        given(mileStone.getId()).willReturn(10L);
        given(mileStone.getTitle()).willReturn("milestone title");
        given(mileStone.getDescription()).willReturn("milestone desc");
        given(mileStone.getStatus()).willReturn(MileStoneStatus.IN_PROGRESS);
        given(mileStone.getDueDate()).willReturn(now);
        given(mileStone.getCreatedAt()).willReturn(now);
        given(mileStone.getUpdatedAt()).willReturn(now);

        given(taskTag.getTag()).willReturn(tag);
        given(tag.getId()).willReturn(100L);
        given(tag.getName()).willReturn("bug");

        TaskResponseDto result = taskService.buildTaskResponseDto(task);

        assertThat(result.taskId()).isEqualTo(1L);
        assertThat(result.projectId()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("test title");
        assertThat(result.milestoneResponseDto()).isNotNull();
        assertThat(result.milestoneResponseDto().id()).isEqualTo(10L);
        assertThat(result.milestoneResponseDto().status()).isEqualTo(MileStoneStatus.IN_PROGRESS);
        assertThat(result.tags()).hasSize(1);
        assertThat(result.tags().getFirst().name()).isEqualTo("bug");
    }

    @Test
    @DisplayName("TaskResponseDto 빌드 - 마일스톤 없는 경우 milestoneResponseDto는 null")
    void buildTaskResponseDto_withoutMilestone() {
        Task task = mock(Task.class);
        Project project = mock(Project.class);
        ProjectMember projectMember = mock(ProjectMember.class);
        LocalDateTime now = LocalDateTime.now();

        given(task.getId()).willReturn(1L);
        given(task.getProject()).willReturn(project);
        given(task.getProjectMember()).willReturn(projectMember);
        given(task.getTitle()).willReturn("test title");
        given(task.getContent()).willReturn("test content");
        given(task.getCreatedAt()).willReturn(now);
        given(task.getUpdatedAt()).willReturn(now);
        given(task.getMilestone()).willReturn(null);
        given(task.getTaskTagList()).willReturn(List.of());

        given(project.getId()).willReturn(1L);
        given(projectMember.getId()).willReturn(1L);

        TaskResponseDto result = taskService.buildTaskResponseDto(task);

        assertThat(result.milestoneResponseDto()).isNull();
        assertThat(result.tags()).isEmpty();
    }

    @Test
    @DisplayName("TaskResponseDto 빌드 - 태그가 여러 개인 경우")
    void buildTaskResponseDto_withMultipleTags() {
        Task task = mock(Task.class);
        Project project = mock(Project.class);
        ProjectMember projectMember = mock(ProjectMember.class);
        LocalDateTime now = LocalDateTime.now();

        Tag tag1 = mock(Tag.class);
        Tag tag2 = mock(Tag.class);
        TaskTag taskTag1 = mock(TaskTag.class);
        TaskTag taskTag2 = mock(TaskTag.class);

        given(task.getId()).willReturn(1L);
        given(task.getProject()).willReturn(project);
        given(task.getProjectMember()).willReturn(projectMember);
        given(task.getTitle()).willReturn("title");
        given(task.getContent()).willReturn("content");
        given(task.getCreatedAt()).willReturn(now);
        given(task.getUpdatedAt()).willReturn(now);
        given(task.getMilestone()).willReturn(null);
        given(task.getTaskTagList()).willReturn(List.of(taskTag1, taskTag2));

        given(project.getId()).willReturn(1L);
        given(projectMember.getId()).willReturn(1L);

        given(taskTag1.getTag()).willReturn(tag1);
        given(tag1.getId()).willReturn(1L);
        given(tag1.getName()).willReturn("feature");

        given(taskTag2.getTag()).willReturn(tag2);
        given(tag2.getId()).willReturn(2L);
        given(tag2.getName()).willReturn("bug");

        TaskResponseDto result = taskService.buildTaskResponseDto(task);

        assertThat(result.tags()).hasSize(2);
        assertThat(result.tags()).extracting("name").containsExactlyInAnyOrder("feature", "bug");
    }
}