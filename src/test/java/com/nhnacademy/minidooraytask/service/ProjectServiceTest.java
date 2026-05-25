package com.nhnacademy.minidooraytask.service;

import com.nhnacademy.minidooraytask.member.domain.ProjectMember;
import com.nhnacademy.minidooraytask.member.repository.ProjectMemberRepository;
import com.nhnacademy.minidooraytask.project.domain.Project;
import com.nhnacademy.minidooraytask.project.domain.ProjectRequestDto;
import com.nhnacademy.minidooraytask.project.domain.ProjectResponseDto;
import com.nhnacademy.minidooraytask.project.domain.ProjectStatus;
import com.nhnacademy.minidooraytask.project.exception.NoAuthoProjectException;
import com.nhnacademy.minidooraytask.project.exception.ProjectNotFoundException;
import com.nhnacademy.minidooraytask.project.respository.ProjectRepository;
import com.nhnacademy.minidooraytask.project.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.mockito.ArgumentMatchers.anyBoolean;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ProjectService.class)
class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private ProjectMemberRepository projectMemberRepository;

    // ====== GET ======

    @Test
    @DisplayName("프로젝트 단건 조회 - 성공")
    void getProject_success() {
        Long projectId = 1L;
        Project mockProject = mock(Project.class);
        given(projectRepository.findById(projectId)).willReturn(Optional.of(mockProject));

        Project result = projectService.getProject(projectId);

        assertThat(result).isEqualTo(mockProject);
    }

    @Test
    @DisplayName("프로젝트 단건 조회 - 실패")
    void getProject_fail_notFound() {
        Long projectId = 1L;
        given(projectRepository.findById(projectId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProject(projectId))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    @DisplayName("내 프로젝트 목록 조회 - 성공")
    void getMyProjects_success() {
        Long accountId = 100L;
        Project mockProject = new Project("ttl", "ct", accountId);

        given(projectRepository.findAllByAccountIdByQuery(any(Long.class), anyBoolean()))
                .willReturn(List.of(mockProject));

        List<ProjectResponseDto> result = projectService.getMyProjects(accountId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("ttl");
    }

    @Test
    @DisplayName("프로젝트 관리자 권한 확인 - 성공")
    void checkProjectAdmin_success() {
        Long projectId = 1L;
        Long accountId = 100L;
        given(projectRepository.existsProjectByIdAndCreateAccountId(projectId, accountId)).willReturn(true);

        projectService.checkProjectAdmin(projectId, accountId); // 예외가 발생하지 않으면 성공
    }

    @Test
    @DisplayName("프로젝트 관리자 권한 확인 실패 - 권한 없음")
    void checkProjectAdmin_fail_noAuth() {
        Long projectId = 1L;
        Long accountId = 100L;
        given(projectRepository.existsProjectByIdAndCreateAccountId(projectId, accountId)).willReturn(false);

        assertThatThrownBy(() -> projectService.checkProjectAdmin(projectId, accountId))
                .isInstanceOf(NoAuthoProjectException.class);
    }

    // ====== POST ======

    @Test
    @DisplayName("프로젝트 생성 - 성공 (프로젝트 및 관리자 멤버 저장)")
    void createProject_success() {
        Long accountId = 100L;
        ProjectRequestDto request = new ProjectRequestDto("new Pj", "descrip", ProjectStatus.ACTIVE);
        Project savedProject = mock(Project.class);

        given(projectRepository.save(any(Project.class))).willReturn(savedProject);
        given(projectMemberRepository.save(any(ProjectMember.class))).willReturn(mock(ProjectMember.class));

        projectService.createProject(accountId, request);

        then(projectRepository).should().save(any(Project.class));
        then(projectMemberRepository).should().save(any(ProjectMember.class));
    }

    @Test
    @DisplayName("프로젝트 수정 - 성공")
    void updateProject_success() {
        Long projectId = 1L;
        Project project = new Project("ttl", "descrip", 100L);
        ProjectRequestDto request = new ProjectRequestDto("modi ttl", "modi descrip", ProjectStatus.DORMANT);

        given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
        given(projectRepository.save(project)).willReturn(project);

        projectService.updateProject(projectId, request);

        assertThat(project.getTitle()).isEqualTo("modi ttl");
        assertThat(project.getDescription()).isEqualTo("modi descrip");
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.DORMANT);
    }

    // ===== DELETE =====

    @Test
    @DisplayName("프로젝트 삭제 - 성공 (소프트 삭제)")
    void deleteProject_success() {
        Long projectId = 1L;
        Long accountId = 100L;
        Project project = new Project("ttl", "descrip", accountId);

        given(projectRepository.findProjectByIdAndCreateAccountId(eq(projectId), eq(accountId)))
                .willReturn(Optional.of(project));

        given(projectRepository.existsProjectByIdAndCreateAccountId(eq(projectId), eq(accountId)))
                .willReturn(true);

        projectService.deleteProject(projectId, accountId);

        assertThat(project.isDeleted()).isTrue();
        then(projectRepository).should().save(project);
    }

    @Test
    @DisplayName("프로젝트 삭제 - 실패")
    void deleteProject_fail_notFoundOrNotCreator() {
        Long projectId = 1L;
        Long accountId = 100L;

        given(projectRepository.findProjectByIdAndCreateAccountId(projectId, accountId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.deleteProject(projectId, accountId))
                .isInstanceOf(ProjectNotFoundException.class);
    }
}
