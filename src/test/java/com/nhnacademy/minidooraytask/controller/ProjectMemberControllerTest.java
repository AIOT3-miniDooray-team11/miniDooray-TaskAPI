package com.nhnacademy.minidooraytask.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.minidooraytask.handler.CustomExceptionHandler;
import com.nhnacademy.minidooraytask.member.domain.*;
import com.nhnacademy.minidooraytask.member.exception.AlreadyProjectMemberExistException;
import com.nhnacademy.minidooraytask.member.exception.ProjectMemberIsNotExistException;
import com.nhnacademy.minidooraytask.member.service.ProjectMemberFacade;
import com.nhnacademy.minidooraytask.member.service.ProjectMemberService;
import com.nhnacademy.minidooraytask.project.exception.NoAuthoProjectException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(ProjectMemberController.class)
@Import(CustomExceptionHandler.class)
class ProjectMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProjectMemberFacade projectMemberFacade;

    @MockitoBean
    private ProjectMemberService projectMemberService;

    // ===== GET =====

    @Test
    @DisplayName("멤버 목록 조회 - 200 OK")
    void getProjectMemberList_success() throws Exception {
        MemberInfoDto memberInfoDto = new MemberInfoDto(
                1L, 1L, "testUser", MembersAuth.MEMBER, LocalDateTime.now()
        );
        MemberInfoListDto responseDto = new MemberInfoListDto(List.of(memberInfoDto));
        given(projectMemberFacade.getMemberInfoList(1L, 1L)).willReturn(responseDto);

        var result = mockMvc.perform(get("/task-api/projects/1/members")
                        .header("X-Account-Id", 1L))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(result.getResponse().getContentAsString()).contains("testUser");
    }

    @Test
    @DisplayName("멤버 목록 조회 - 프로젝트 멤버가 아니면 404")
    void getProjectMemberList_notMember() throws Exception {
        given(projectMemberFacade.getMemberInfoList(1L, 1L))
                .willThrow(new ProjectMemberIsNotExistException("존재하지 않는 멤버입니다"));

        var result = mockMvc.perform(get("/task-api/projects/1/members")
                        .header("X-Account-Id", 1L))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("멤버 추가 - 201 Created")
    void addProjectMember_success() throws Exception {
        MemberRequestDto requestDto = new MemberRequestDto(2L, "targetUser", MembersAuth.MEMBER);
        willDoNothing().given(projectMemberFacade)
                .addProjectMember(eq(1L), eq(1L), any(MemberRequestDto.class));

        var result = mockMvc.perform(post("/task-api/projects/1/members")
                        .header("X-Account-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(201);
    }

    @Test
    @DisplayName("멤버 추가 - 권한 없으면 403")
    void addProjectMember_noAuth() throws Exception {
        MemberRequestDto requestDto = new MemberRequestDto(2L, "targetUser", MembersAuth.MEMBER);
        willThrow(new NoAuthoProjectException("관리자 권한이 필요합니다"))
                .given(projectMemberFacade)
                .addProjectMember(eq(1L), eq(1L), any(MemberRequestDto.class));

        var result = mockMvc.perform(post("/task-api/projects/1/members")
                        .header("X-Account-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("멤버 추가 - 이미 존재하는 멤버면 409")
    void addProjectMember_alreadyExist() throws Exception {
        MemberRequestDto requestDto = new MemberRequestDto(2L, "targetUser", MembersAuth.MEMBER);
        willThrow(new AlreadyProjectMemberExistException("이미 존재하는 멤버입니다"))
                .given(projectMemberFacade)
                .addProjectMember(eq(1L), eq(1L), any(MemberRequestDto.class));

        var result = mockMvc.perform(post("/task-api/projects/1/members")
                        .header("X-Account-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("멤버 권한 변경 - 204 No Content")
    void updateProjectMember_success() throws Exception {
        MemberRequestDto requestDto = new MemberRequestDto(2L, "targetUser", MembersAuth.ADMIN);
        willDoNothing().given(projectMemberFacade)
                .updateMember(eq(1L), eq(1L), eq(1L), any(MemberRequestDto.class));

        var result = mockMvc.perform(put("/task-api/projects/1/members/1")
                        .header("X-Account-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(204);
    }

    @Test
    @DisplayName("멤버 권한 변경 - 관리자 아니면 403")
    void updateProjectMember_noAdminAuth() throws Exception {
        MemberRequestDto requestDto = new MemberRequestDto(2L, "targetUser", MembersAuth.ADMIN);
        willThrow(new NoAuthoProjectException("관리자 권한이 필요합니다"))
                .given(projectMemberFacade)
                .updateMember(eq(1L), eq(1L), eq(1L), any(MemberRequestDto.class));

        var result = mockMvc.perform(put("/task-api/projects/1/members/1")
                        .header("X-Account-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("멤버 삭제 - 204 No Content")
    void deleteProjectMember_success() throws Exception {
        willDoNothing().given(projectMemberFacade).deleteMember(anyLong(), anyLong(), anyLong());

        var result = mockMvc.perform(delete("/task-api/projects/1/members/1")
                        .header("X-Account-Id", 1L))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(204);
    }

    @Test
    @DisplayName("멤버 삭제 - 권한 없으면 403")
    void deleteProjectMember_noAuth() throws Exception {
        doThrow(new NoAuthoProjectException("관리자 권한이 필요합니다"))
                .when(projectMemberFacade).deleteMember(anyLong(), anyLong(), anyLong());

        var result = mockMvc.perform(delete("/task-api/projects/1/members/1")
                        .header("X-Account-Id", 1L))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("멤버 삭제 - 존재하지 않는 멤버면 404")
    void deleteProjectMember_notFound() throws Exception {
        doThrow(new ProjectMemberIsNotExistException("존재하지 않는 멤버입니다"))
                .when(projectMemberFacade).deleteMember(anyLong(), anyLong(), anyLong());

        var result = mockMvc.perform(delete("/task-api/projects/1/members/1")
                        .header("X-Account-Id", 1L))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }
}