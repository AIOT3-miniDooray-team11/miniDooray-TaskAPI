package com.nhnacademy.minidooraytask.project.service;


import com.nhnacademy.minidooraytask.MileStone.domain.MileStone;
import com.nhnacademy.minidooraytask.member.domain.ProjectMember;
import com.nhnacademy.minidooraytask.member.service.ProjectMemberService;
import com.nhnacademy.minidooraytask.project.domain.ProjectInfoDto;
import com.nhnacademy.minidooraytask.project.domain.ProjectViewDto;
import com.nhnacademy.minidooraytask.task.domain.Task;
import com.nhnacademy.minidooraytask.task.domain.TaskInfoDto;
import com.nhnacademy.minidooraytask.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class ProjectFacade {

    private final ProjectService projectService;
    private final TaskService taskService;
    private final ProjectMemberService projectMemberService;

    @Transactional
    public ProjectViewDto getProjectView(Long accountId) {


        //사용자가 참여중인 프로젝트 멤버 리스트 가져오기
        List<ProjectMember> projectMemberList = projectMemberService.getProjectMemberByAccountId(accountId);

//        projectInfoDto랑 TaskInfoDto를 가져와서 ProjetViewDto에 넣어야함
        List<ProjectInfoDto> projectInfoDtos = projectMemberList.stream()
                .map(ProjectMember::getProject)
                .filter(project -> !project.isDeleted())  // ← 이 줄 추가
                .map(project -> new ProjectInfoDto(
                        project.getId(),
                        project.getTitle(),
                        project.getStatus(),
                        project.getTaskList().stream()
                                .map(Task::getMilestone)
                                .filter(Objects::nonNull)
                                .map(MileStone::getStatus)
                                .toList()
                ))
                .toList();

        // task -> taskinfoDto
        List<TaskInfoDto> taskInfoDtos = projectMemberList.stream()
                .map(ProjectMember::getTaskList)
                .flatMap(Collection::stream)
                .map(task -> new TaskInfoDto(
                        task.getId(),
                        task.getTitle(),
                        Objects.nonNull(task.getMilestone()) ? task.getMilestone().getStatus() : null
                ))
                .toList();


        return new ProjectViewDto(projectInfoDtos,taskInfoDtos);
    }

}
