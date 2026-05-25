package com.nhnacademy.minidooraytask.facade;

import com.nhnacademy.minidooraytask.MileStone.domain.MileStoneStatus;
import com.nhnacademy.minidooraytask.MileStone.domain.MilestoneRequestDto;
import com.nhnacademy.minidooraytask.MileStone.exception.MileStoneIsExistException;
import com.nhnacademy.minidooraytask.MileStone.service.MileStoneFacade;
import com.nhnacademy.minidooraytask.MileStone.service.MileStoneService;
import com.nhnacademy.minidooraytask.task.domain.Task;
import com.nhnacademy.minidooraytask.task.exception.TaskNotFoundException;
import com.nhnacademy.minidooraytask.task.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class MilestoneFacadeTest {

    @InjectMocks
    private MileStoneFacade mileStoneFacade;

    @Mock
    private MileStoneService mileStoneService;

    @Mock
    private TaskService taskService;

    @Test
    @DisplayName("마일스톤 생성 - 성공")
    void createMilestone_success() {
        long taskId = 1L;
        long accountId = 100L;
        MilestoneRequestDto requestDto = new MilestoneRequestDto(
                "마일스톤", "설명", MileStoneStatus.IN_PROGRESS, LocalDateTime.now().plusDays(7)
        );

        Task mockTask = mock(Task.class);
        willDoNothing().given(taskService).checkTaskMakerByAccountId(accountId, taskId);
        given(taskService.getTaskById(taskId)).willReturn(mockTask);
        willDoNothing().given(mileStoneService).createMileStone(eq(mockTask), any(MilestoneRequestDto.class));

        mileStoneFacade.createMilestone(taskId, accountId, requestDto);

        then(mileStoneService).should().createMileStone(eq(mockTask), any(MilestoneRequestDto.class));
    }

    @Test
    @DisplayName("마일스톤 생성 - 실패 (태스크 없음)")
    void createMilestone_fail_taskNotFound() {
        long taskId = 999L;
        long accountId = 100L;
        MilestoneRequestDto requestDto = new MilestoneRequestDto(
                "마일스톤", "설명", MileStoneStatus.IN_PROGRESS, LocalDateTime.now().plusDays(7)
        );

        willThrow(new TaskNotFoundException("태스크가 존재하지 않습니다"))
                .given(taskService).checkTaskMakerByAccountId(accountId, taskId);

        assertThatThrownBy(() -> mileStoneFacade.createMilestone(taskId, accountId, requestDto))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("마일스톤 생성 - 실패 (이미 존재)")
    void createMilestone_fail_alreadyExists() {
        long taskId = 1L;
        long accountId = 100L;
        MilestoneRequestDto requestDto = new MilestoneRequestDto(
                "마일스톤", "설명", MileStoneStatus.IN_PROGRESS, LocalDateTime.now().plusDays(7)
        );

        Task mockTask = mock(Task.class);
        willDoNothing().given(taskService).checkTaskMakerByAccountId(accountId, taskId);
        given(taskService.getTaskById(taskId)).willReturn(mockTask);
        willThrow(new MileStoneIsExistException("이미 존재하는 마일스톤입니다"))
                .given(mileStoneService).createMileStone(eq(mockTask), any(MilestoneRequestDto.class));

        assertThatThrownBy(() -> mileStoneFacade.createMilestone(taskId, accountId, requestDto))
                .isInstanceOf(MileStoneIsExistException.class);
    }

    @Test
    @DisplayName("마일스톤 수정 - 성공")
    void updateMilestone_success() {
        long taskId = 1L;
        long accountId = 100L;
        MilestoneRequestDto requestDto = new MilestoneRequestDto(
                "수정된 마일스톤", "수정된 설명", MileStoneStatus.COMPLETED, LocalDateTime.now().plusDays(14)
        );

        willDoNothing().given(taskService).checkTaskMakerByAccountId(accountId, taskId);
        willDoNothing().given(mileStoneService).updateMileStone(eq(taskId), any(MilestoneRequestDto.class));

        mileStoneFacade.updateMilestone(taskId, accountId, requestDto);

        then(mileStoneService).should().updateMileStone(eq(taskId), any(MilestoneRequestDto.class));
    }

    @Test
    @DisplayName("마일스톤 삭제 - 성공")
    void deleteMilestone_success() {
        long taskId = 1L;
        long accountId = 100L;

        willDoNothing().given(taskService).checkTaskMakerByAccountId(accountId, taskId);
        willDoNothing().given(mileStoneService).deleteMileStone(taskId);

        mileStoneFacade.deleteMilestone(taskId, accountId);

        then(mileStoneService).should().deleteMileStone(taskId);
    }

    @Test
    @DisplayName("마일스톤 삭제 - 실패 (태스크 없음)")
    void deleteMilestone_fail_taskNotFound() {
        long taskId = 999L;
        long accountId = 100L;

        willThrow(new TaskNotFoundException("태스크가 존재하지 않습니다"))
                .given(taskService).checkTaskMakerByAccountId(accountId, taskId);

        assertThatThrownBy(() -> mileStoneFacade.deleteMilestone(taskId, accountId))
                .isInstanceOf(TaskNotFoundException.class);
    }
}
