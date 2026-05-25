package com.nhnacademy.minidooraytask.facade;

import com.nhnacademy.minidooraytask.member.domain.*;
import com.nhnacademy.minidooraytask.member.exception.ProjectMemberIsNotExistException;
import com.nhnacademy.minidooraytask.member.service.ProjectMemberFacade;
import com.nhnacademy.minidooraytask.member.service.ProjectMemberService;
import com.nhnacademy.minidooraytask.project.domain.Project;
import com.nhnacademy.minidooraytask.project.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectMemberFacadeTest {

    @InjectMocks
    private ProjectMemberFacade projectMemberFacade;

    @Mock
    private ProjectService projectService;

    @Mock
    private ProjectMemberService projectMemberService;

    @Test
    @DisplayName("멤버 목록 조회 - 성공")
    void getMemberInfoList_success() {
        long projectId = 1L;
        long accountId = 100L;

        ProjectMember mockMember = mock(ProjectMember.class);
        given(mockMember.getAccountId()).willReturn(accountId);
        given(mockMember.getId()).willReturn(1L);
        given(mockMember.getAuth()).willReturn(MembersAuth.ADMIN);
        given(mockMember.getJoinedAt()).willReturn(LocalDateTime.now());

        willDoNothing().given(projectMemberService).checkProjectMember(projectId, accountId);
        given(projectMemberService.getMemberMapWithUserIdByProjectId(projectId))
                .willReturn(Map.of("user1", mockMember));

        MemberInfoListDto result = projectMemberFacade.getMemberInfoList(projectId, accountId);

        assertThat(result.memberInfoDtoList()).hasSize(1);
        assertThat(result.memberInfoDtoList().get(0).userId()).isEqualTo("user1");
    }

    @Test
    @DisplayName("멤버 목록 조회 - 실패 (멤버 아님)")
    void getMemberInfoList_fail_notMember() {
        long projectId = 1L;
        long accountId = 999L;

        willThrow(new ProjectMemberIsNotExistException("프로젝트 멤버가 아닙니다"))
                .given(projectMemberService).checkProjectMember(projectId, accountId);

        assertThatThrownBy(() -> projectMemberFacade.getMemberInfoList(projectId, accountId))
                .isInstanceOf(ProjectMemberIsNotExistException.class);
    }

    @Test
    @DisplayName("멤버 추가 - 성공")
    void addProjectMember_success() {
        long projectId = 1L;
        long accountId = 100L;
        MemberRequestDto requestDto = new MemberRequestDto(200L, "user2", MembersAuth.MEMBER);

        Project mockProject = mock(Project.class);

        willDoNothing().given(projectMemberService).checkProjectMember(projectId, accountId);
        willDoNothing().given(projectMemberService).checkIncludedMember(projectId, accountId);
        willDoNothing().given(projectMemberService).checkAdminAuth(projectId, accountId);
        given(projectService.exGetProjectById(projectId)).willReturn(mockProject);
        willDoNothing().given(projectMemberService).addProjectMember(eq(mockProject), any(MemberRequestDto.class));

        projectMemberFacade.addProjectMember(projectId, accountId, requestDto);

        then(projectMemberService).should().addProjectMember(eq(mockProject), any(MemberRequestDto.class));
    }

    @Test
    @DisplayName("멤버 추가 - 실패 (권한 없음)")
    void addProjectMember_fail_notAdmin() {
        long projectId = 1L;
        long accountId = 100L;
        MemberRequestDto requestDto = new MemberRequestDto(200L, "user2", MembersAuth.MEMBER);

        willDoNothing().given(projectMemberService).checkProjectMember(projectId, accountId);
        willDoNothing().given(projectMemberService).checkIncludedMember(projectId, accountId);
        willThrow(new IllegalArgumentException("권한이 없습니다"))
                .given(projectMemberService).checkAdminAuth(projectId, accountId);

        assertThatThrownBy(() -> projectMemberFacade.addProjectMember(projectId, accountId, requestDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("멤버 권한 변경 - 성공")
    void updateMember_success() {
        long projectId = 1L;
        long memberId = 2L;
        long accountId = 100L;
        MemberRequestDto requestDto = new MemberRequestDto(null, null, MembersAuth.ADMIN);

        willDoNothing().given(projectMemberService).checkProjectMemberWithAuth(projectId, accountId, MembersAuth.ADMIN);
        willDoNothing().given(projectMemberService).updateProjectMember(memberId, requestDto);

        projectMemberFacade.updateMember(projectId, memberId, accountId, requestDto);

        then(projectMemberService).should().updateProjectMember(memberId, requestDto);
    }

    @Test
    @DisplayName("멤버 권한 변경 - 실패 (권한 없음)")
    void updateMember_fail_notAdmin() {
        long projectId = 1L;
        long memberId = 2L;
        long accountId = 999L;
        MemberRequestDto requestDto = new MemberRequestDto(null, null, MembersAuth.MEMBER);

        willThrow(new ProjectMemberIsNotExistException("권한이 없습니다"))
                .given(projectMemberService).checkProjectMemberWithAuth(projectId, accountId, MembersAuth.ADMIN);

        assertThatThrownBy(() -> projectMemberFacade.updateMember(projectId, memberId, accountId, requestDto))
                .isInstanceOf(ProjectMemberIsNotExistException.class);
    }
}
