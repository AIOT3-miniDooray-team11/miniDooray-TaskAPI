package com.nhnacademy.minidooraytask.controller;

import com.nhnacademy.minidooraytask.handler.CustomExceptionHandler;
import com.nhnacademy.minidooraytask.member.domain.*;
import com.nhnacademy.minidooraytask.member.exception.ProjectMemberIsNotExistException;
import com.nhnacademy.minidooraytask.member.service.ProjectMemberFacade;
import com.nhnacademy.minidooraytask.member.service.ProjectMemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectMemberController.class)
@Import(CustomExceptionHandler.class)
public class ProjectMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectMemberFacade projectMemberFacade;

    @MockitoBean
    private ProjectMemberService projectMemberService;

    @Test
    @DisplayName("멤버 목록 조회 - 성공")
    void getProjectMemberList_success() throws Exception {
        long projectId = 1L;
        long accountId = 100L;

        MemberInfoDto memberInfoDto = new MemberInfoDto(100L, 1L, "user1", MembersAuth.ADMIN, LocalDateTime.now());
        MemberInfoListDto responseDto = new MemberInfoListDto(List.of(memberInfoDto));

        given(projectMemberFacade.getMemberInfoList(projectId, accountId)).willReturn(responseDto);

        mockMvc.perform(get("/task-api/projects/{projectId}/members", projectId)
                        .header("X-Account-Id", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberInfoDtoList[0].userId").value("user1"))
                .andExpect(jsonPath("$.memberInfoDtoList[0].auth").value("ADMIN"));
    }

    @Test
    @DisplayName("멤버 목록 조회 - 실패 (멤버 아님)")
    void getProjectMemberList_fail_notMember() throws Exception {
        long projectId = 1L;
        long accountId = 999L;

        given(projectMemberFacade.getMemberInfoList(projectId, accountId))
                .willThrow(new ProjectMemberIsNotExistException("프로젝트 멤버가 아닙니다"));

        mockMvc.perform(get("/task-api/projects/{projectId}/members", projectId)
                        .header("X-Account-Id", accountId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("멤버 추가 - 성공")
    void addProjectMember_success() throws Exception {
        long projectId = 1L;
        long accountId = 100L;
        MemberRequestDto requestDto = new MemberRequestDto(200L, "user2", MembersAuth.MEMBER);

        willDoNothing().given(projectMemberFacade).addProjectMember(eq(projectId), eq(accountId), any(MemberRequestDto.class));

        mockMvc.perform(post("/task-api/projects/{projectId}/members", projectId)
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("멤버 추가 - 실패 (권한 없음)")
    void addProjectMember_fail_notAdmin() throws Exception {
        long projectId = 1L;
        long accountId = 999L;
        MemberRequestDto requestDto = new MemberRequestDto(200L, "user2", MembersAuth.MEMBER);

        willThrow(new ProjectMemberIsNotExistException("권한이 없습니다"))
                .given(projectMemberFacade).addProjectMember(eq(projectId), eq(accountId), any(MemberRequestDto.class));

        mockMvc.perform(post("/task-api/projects/{projectId}/members", projectId)
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("멤버 권한 변경 - 성공")
    void updateProjectMember_success() throws Exception {
        long projectId = 1L;
        long memberId = 2L;
        long accountId = 100L;
        MemberRequestDto requestDto = new MemberRequestDto(null, null, MembersAuth.ADMIN);

        willDoNothing().given(projectMemberFacade).updateMember(eq(projectId), eq(memberId), eq(accountId), any(MemberRequestDto.class));

        mockMvc.perform(put("/task-api/projects/{projectId}/members/{memberId}", projectId, memberId)
                        .header("X-Account-Id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("멤버 삭제 - 성공")
    void deleteProjectMember_success() throws Exception {
        long projectId = 1L;
        long memberId = 2L;
        long accountId = 100L;

        willDoNothing().given(projectMemberService).deleteProjectMember(eq(projectId), eq(memberId));

        mockMvc.perform(delete("/task-api/projects/{projectId}/members/{memberId}", projectId, memberId)
                        .header("X-Account-Id", accountId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("멤버 삭제 - 실패 (멤버 없음)")
    void deleteProjectMember_fail_notFound() throws Exception {
        long projectId = 1L;
        long memberId = 999L;
        long accountId = 100L;

        willThrow(new ProjectMemberIsNotExistException("존재하지 않는 멤버입니다"))
                .given(projectMemberService).deleteProjectMember(eq(projectId), eq(memberId));

        mockMvc.perform(delete("/task-api/projects/{projectId}/members/{memberId}", projectId, memberId)
                        .header("X-Account-Id", accountId))
                .andExpect(status().isBadRequest());
    }
}
