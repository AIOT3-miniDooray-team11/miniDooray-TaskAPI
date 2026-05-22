package com.nhnacademy.minidooraytask.task.service;


import com.nhnacademy.minidooraytask.client.AccountApiClient;
import com.nhnacademy.minidooraytask.client.dto.account.AccountResp;
import com.nhnacademy.minidooraytask.member.domain.MemberRequestDto;
import com.nhnacademy.minidooraytask.member.domain.MembersAuth;
import com.nhnacademy.minidooraytask.member.domain.ProjectMember;
import com.nhnacademy.minidooraytask.member.exception.ProjectMemberIsNotExistException;
import com.nhnacademy.minidooraytask.member.repository.ProjectMemberRepository;
import com.nhnacademy.minidooraytask.member.service.ProjectMemberService;
import com.nhnacademy.minidooraytask.project.domain.Project;
import com.nhnacademy.minidooraytask.project.domain.ProjectResponseDto;
import com.nhnacademy.minidooraytask.project.respository.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ProjectMemberService.class)
class ProjectMemberTest {

    @Autowired
    private ProjectMemberService projectMemberService;

    @MockitoBean
    private ProjectMemberRepository projectMemberRepository;

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private AccountApiClient accountApiClient;


    @Test
    @DisplayName("프로젝트 멤버 유무 확인 - 성공")
    void checkProjectMember_success() {

        Long projectId = 1L;
        Long accountId = 2L;

        given(projectMemberRepository.existsByProject_IdAndAccountId(projectId,accountId))
                .willReturn(true);

        assertThatCode(() -> projectMemberService.checkProjectMember(projectId,accountId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("프로젝트 멤버 유무 확인 - 실패")
    void checkProjectMember_fail_notExist() {

        Long projectId = 1L;
        Long accountId = 2L;

        given(projectMemberRepository.existsByProject_IdAndAccountId(projectId, accountId))
                .willReturn(false);

        assertThatThrownBy(() -> projectMemberService.checkProjectMember(projectId, accountId))
                .isInstanceOf(ProjectMemberIsNotExistException.class)
                .hasMessageContaining("존재하지 않은 멤버 조회입니다");
    }

    /*---------------------------------*/

    @Test
    @DisplayName("checkProjectMember(3개 파라미터) - 성공")
    void checkProjectMember_3params_success() {
        long projectId = 1L;
        long memberId = 50L;
        long accountId = 100L;
        given(projectMemberRepository.existsByProject_IdAndIdAndAccountId(projectId, memberId, accountId)).willReturn(true);

        assertThatCode(() -> projectMemberService.checkProjectMember(projectId, memberId, accountId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("checkProjectMember(3개 파라미터) - 실패 (존재하지 않는 멤버)")
    void checkProjectMember_3params_fail() {
        long projectId = 1L;
        long memberId = 50L;
        long accountId = 100L;
        given(projectMemberRepository.existsByProject_IdAndIdAndAccountId(projectId, memberId, accountId)).willReturn(false);

        assertThatThrownBy(() -> projectMemberService.checkProjectMember(projectId, memberId, accountId))
                .isInstanceOf(ProjectMemberIsNotExistException.class);
    }

    /*---------------------------------*/

    @Test
    @DisplayName("getActiveMember - 성공")
    void getActiveMember_success() {
        long projectId = 1L;
        long accountId = 100L;
        ProjectMember mockMember = mock(ProjectMember.class);
        given(mockMember.isDeleted()).willReturn(false);
        given(projectMemberRepository.findByProject_IdAndAccountId(projectId, accountId)).willReturn(Optional.of(mockMember));

        ProjectMember result = projectMemberService.getActiveMember(projectId, accountId);

        assertThat(result).isEqualTo(mockMember);
    }

    @Test
    @DisplayName("getActiveMember - 실패 (삭제된 멤버)")
    void getActiveMember_fail_deleted() {
        long projectId = 1L;
        long accountId = 100L;
        ProjectMember mockMember = mock(ProjectMember.class);
        given(mockMember.isDeleted()).willReturn(true);
        given(projectMemberRepository.findByProject_IdAndAccountId(projectId, accountId)).willReturn(Optional.of(mockMember));

        assertThatThrownBy(() -> projectMemberService.getActiveMember(projectId, accountId))
                .isInstanceOf(ProjectMemberIsNotExistException.class);
    }

    /*---------------------------------*/
    //public void checkAdminAuth(long projectId, long accountId)

    @Test
    @DisplayName("checkAdminAuth - 성공")
    void checkAdminAuth_success() {
        long projectId = 1L;
        long accountId = 100L;

        String auth
    }

    @Test
    @DisplayName("checkAdminAuth - 실패 (권한 부족)")
    void checkAdminAuth_fail() {
        MemberRequestDto requestDto = new MemberRequestDto(100L, "user", MembersAuth.MEMBER);

        assertThatThrownBy(() -> projectMemberService.checkAdminAuth(requestDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /*---------------------------------*/

    @Test
    @DisplayName("checkProjectMemberWithAuth - 성공")
    void checkProjectMemberWithAuth_success() {
        long projectId = 1L;
        long accountId = 100L;
        MembersAuth auth = MembersAuth.ADMIN;
        given(projectMemberRepository.existsByProject_IdAndAccountIdAndAuth(projectId, accountId, auth)).willReturn(true);

        assertThatCode(() -> projectMemberService.checkProjectMemberWithAuth(projectId, accountId, auth))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("checkProjectMemberWithAuth - 실패")
    void checkProjectMemberWithAuth_fail() {
        long projectId = 1L;
        long accountId = 100L;
        MembersAuth auth = MembersAuth.ADMIN;
        given(projectMemberRepository.existsByProject_IdAndAccountIdAndAuth(projectId, accountId, auth)).willReturn(false);

        assertThatThrownBy(() -> projectMemberService.checkProjectMemberWithAuth(projectId, accountId, auth))
                .isInstanceOf(ProjectMemberIsNotExistException.class);
    }

    @Test
    @DisplayName("checkIncludedMember - 성공")
    void checkIncludedMember_success() {
        long projectId = 1L;
        long accountId = 100L;
        ProjectMember mockMember = mock(ProjectMember.class);
        given(projectMemberRepository.findByProject_IdAndAccountId(projectId, accountId)).willReturn(Optional.of(mockMember));

        assertThatCode(() -> projectMemberService.checkIncludedMember(projectId, accountId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("checkIncludedMember - 실패")
    void checkIncludedMember_fail() {
        long projectId = 1L;
        long accountId = 100L;
        given(projectMemberRepository.findByProject_IdAndAccountId(projectId, accountId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> projectMemberService.checkIncludedMember(projectId, accountId))
                .isInstanceOf(ProjectMemberIsNotExistException.class);
    }

    @Test
    @DisplayName("getMemberByTaskId - 조회")
    void getMemberByTaskId_success() {
        long taskId = 10L;
        long accountId = 100L;
        ProjectMember mockMember = mock(ProjectMember.class);
        given(projectMemberRepository.findProjectMemberByTaskIdAndAccountId(taskId, accountId)).willReturn(mockMember);

        ProjectMember result = projectMemberService.getMemberByTaskId(taskId, accountId);

        assertThat(result).isEqualTo(mockMember);
    }

    @Test
    @DisplayName("isMemberIdEqualAccountId - 성공")
    void isMemberIdEqualAccountId_test() {
        long memberId = 50L;
        long accountId = 100L;
        given(projectMemberRepository.existsProjectMemberByIdAndAccountId(memberId, accountId)).willReturn(true);

        boolean result = projectMemberService.isMemberIdEqualAccountId(memberId, accountId);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("getProjectMemberIdByUserId - 정상 아이디 추출")
    void getProjectMemberIdByUserId_success() {
        String userId = "testUser";
        AccountResp accountResp = new AccountResp(100L, userId, "email", "name", null, null);
        given(accountApiClient.getAccountByUserId(userId)).willReturn(accountResp);

        Long result = projectMemberService.getProjectMemberIdByUserId(userId);

        assertThat(result).isEqualTo(100L);
    }

    @Test
    @DisplayName("getProjectMemberIdByUserId - 대상이 없을 때 null 반환")
    void getProjectMemberIdByUserId_null() {
        String userId = "unexistUser";
        given(accountApiClient.getAccountByUserId(userId)).willReturn(null);

        Long result = projectMemberService.getProjectMemberIdByUserId(userId);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getUserIdByAccountId - 성공")
    void getUserIdByAccountId_success() {
        long accountId = 100L;
        AccountResp accountResp = new AccountResp(accountId, "testUser", "email", "name", null, null);
        given(accountApiClient.getAccountById(accountId)).willReturn(accountResp);

        String result = projectMemberService.getUserIdByAccountId(accountId);

        assertThat(result).isEqualTo("testUser");
    }

//    @Test
//    @DisplayName("addProjectMember - 신규 멤버 추가")
//    void addProjectMember
}
