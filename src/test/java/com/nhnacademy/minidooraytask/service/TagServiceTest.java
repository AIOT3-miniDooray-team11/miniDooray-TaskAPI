package com.nhnacademy.minidooraytask.service;

import com.nhnacademy.minidooraytask.member.domain.ProjectMember;
import com.nhnacademy.minidooraytask.member.exception.ProjectMemberIsNotExistException;
import com.nhnacademy.minidooraytask.member.repository.ProjectMemberRepository;
import com.nhnacademy.minidooraytask.project.domain.Project;
import com.nhnacademy.minidooraytask.tag.domain.Tag;
import com.nhnacademy.minidooraytask.tag.domain.TagCreateRequestDto;
import com.nhnacademy.minidooraytask.tag.domain.TagResponseDto;
import com.nhnacademy.minidooraytask.tag.exception.AlreadyTagExistException;
import com.nhnacademy.minidooraytask.tag.repository.TagRepository;
import com.nhnacademy.minidooraytask.tag.repository.TaskTagRepository;
import com.nhnacademy.minidooraytask.tag.service.TagService;
import com.nhnacademy.minidooraytask.task.domain.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @InjectMocks
    private TagService tagService;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private TaskTagRepository taskTagRepository;

    @Test
    @DisplayName("태그 추가 - 성공")
    void addTag_success() {
        Long projectId = 1L;
        Long accountId = 100L;
        TagCreateRequestDto request = new TagCreateRequestDto("백엔드");


        ProjectMember mockMember = mock(ProjectMember.class);
        given(mockMember.isDeleted()).willReturn(false); // 삭제되지 않은 정상 멤버
        given(projectMemberRepository.findByProject_IdAndAccountId(projectId, accountId))
                .willReturn(Optional.of(mockMember));
        given(tagRepository.existsByName(request.name())).willReturn(false);

        Tag savedTag = new Tag("백엔드");
        ReflectionTestUtils.setField(savedTag, "id", 10L);
        given(tagRepository.save(any(Tag.class))).willReturn(savedTag);

        TagResponseDto result = tagService.addTag(projectId, accountId, request);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.name()).isEqualTo("백엔드");

        then(projectMemberRepository).should().findByProject_IdAndAccountId(projectId, accountId);
        then(tagRepository).should().save(any(Tag.class));
    }

    @Test
    @DisplayName("태그 추가 실패 - 중복 태그")
    void addTag_fail_alreadyExist() {

        Long projectId = 1L;
        Long accountId = 100L;
        TagCreateRequestDto request = new TagCreateRequestDto("중복태그");

        ProjectMember mockMember = mock(ProjectMember.class);
        given(mockMember.isDeleted()).willReturn(false);
        given(projectMemberRepository.findByProject_IdAndAccountId(projectId, accountId))
                .willReturn(Optional.of(mockMember));
        given(tagRepository.existsByName(request.name())).willReturn(true);

        assertThatThrownBy(() -> tagService.addTag(projectId, accountId, request))
                .isInstanceOf(AlreadyTagExistException.class);
    }

    @Test
    @DisplayName("권한 검증 실패 - 프로젝트 멤버가 아닐 때")
    void verifyProjectMember_fail() {
        Long projectId = 1L;
        Long accountId = 999L;
        TagCreateRequestDto request = new TagCreateRequestDto("태그");

        given(projectMemberRepository.findByProject_IdAndAccountId(projectId, accountId))
                .willReturn(Optional.empty());
        assertThatThrownBy(() -> tagService.addTag(projectId, accountId, request))
                .isInstanceOf(ProjectMemberIsNotExistException.class);
    }

    @Test
    @DisplayName("Task에 새로운 태그들 연결 검증")
    void connectTag_success_newTags() {
        Task task = new Task(mock(Project.class), mock(ProjectMember.class), "태스크", "내용");
        List<String> inputTags = List.of("신규태그1", "신규태그2");

        given(tagRepository.findAllByNameIn(anySet())).willReturn(new ArrayList<>());

        Tag savedTag1 = new Tag("신규태그1");
        ReflectionTestUtils.setField(savedTag1, "id", 1L);
        Tag savedTag2 = new Tag("신규태그2");
        ReflectionTestUtils.setField(savedTag2, "id", 2L);

        given(tagRepository.saveAll(anyList())).willReturn(List.of(savedTag1, savedTag2));

        given(taskTagRepository.saveAll(anyList())).willReturn(anyList());

        tagService.connectTag(task, inputTags);

        assertThat(task.getTaskTagList()).hasSize(2);

        then(tagRepository).should().findAllByNameIn(anySet());
        then(tagRepository).should().saveAll(anyList());
        then(taskTagRepository).should().saveAll(anyList());
    }
}
