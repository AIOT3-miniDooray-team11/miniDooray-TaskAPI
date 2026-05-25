package com.nhnacademy.minidooraytask.service;

import com.nhnacademy.minidooraytask.MileStone.domain.MileStone;
import com.nhnacademy.minidooraytask.MileStone.domain.MileStoneStatus;
import com.nhnacademy.minidooraytask.MileStone.domain.MilestoneRequestDto;
import com.nhnacademy.minidooraytask.MileStone.exception.MileStoneIsExistException;
import com.nhnacademy.minidooraytask.MileStone.exception.MileStoneIsNotExistException;
import com.nhnacademy.minidooraytask.MileStone.repository.MileStoneRepository;
import com.nhnacademy.minidooraytask.MileStone.service.MileStoneService;
import com.nhnacademy.minidooraytask.task.domain.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MileStoneServiceTest {

    @InjectMocks
    private MileStoneService mileStoneService;

    @Mock
    private MileStoneRepository mileStoneRepository;

    @Test
    @DisplayName("마일스톤 생성 실패")
    void createMileStone_fail_alreadyExist() {
        Task mockTask = mock(Task.class);
        given(mockTask.getId()).willReturn(1L);
        given(mileStoneRepository.existsMileStoneByTask_Id(1L)).willReturn(true);
        MilestoneRequestDto requestDto = new MilestoneRequestDto("제목", "내용", MileStoneStatus.PLANNED, LocalDateTime.now());

        assertThatThrownBy(() -> mileStoneService.createMileStone(mockTask, requestDto))
                .isInstanceOf(MileStoneIsExistException.class);
    }

    @Test
    @DisplayName("마일스톤 수정 실패")
    void updateMileStone_fail_notFound() {

        long taskId = 1L;
        given(mileStoneRepository.existsMileStoneByTask_Id(taskId)).willReturn(false);
        MilestoneRequestDto requestDto = new MilestoneRequestDto("제목", "내용", MileStoneStatus.PLANNED, LocalDateTime.now());

        assertThatThrownBy(() -> mileStoneService.updateMileStone(taskId, requestDto))
                .isInstanceOf(MileStoneIsNotExistException.class);
    }
}
