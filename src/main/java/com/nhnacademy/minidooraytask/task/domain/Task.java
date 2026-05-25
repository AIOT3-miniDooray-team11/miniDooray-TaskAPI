package com.nhnacademy.minidooraytask.task.domain;


import com.nhnacademy.minidooraytask.MileStone.domain.MileStone;
import com.nhnacademy.minidooraytask.comment.domain.Comment;
import com.nhnacademy.minidooraytask.member.domain.ProjectMember;
import com.nhnacademy.minidooraytask.project.domain.Project;
import com.nhnacademy.minidooraytask.tag.domain.Tag;
import com.nhnacademy.minidooraytask.tag.domain.TaskTag;
import com.nhnacademy.minidooraytask.task.exception.TaskValidInputException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "create_member_id", nullable = false)
    private ProjectMember projectMember;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Setter
    @OneToOne
    @JoinColumn(name = "milestone_id")
    private MileStone milestone;

    @OneToMany(mappedBy = "task")
    private List<TaskTag> taskTagList;

    @OneToMany(mappedBy = "task")
    private List<Comment> commentList;

    //생성자
    public Task(Project project, ProjectMember projectMember, String title, String content) {
        this.project = project;
        this.projectMember = projectMember;
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();

        this.taskTagList = new ArrayList<>();
        this.commentList = new ArrayList<>();
    }

    // Task 내용 수정
    public void updateTask(String title, String content) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void setTitle(String title) {
        if(Objects.isNull(title) || title.isBlank()) {
            throw new TaskValidInputException("[task] 입력값이 null");
        }
        this.title = title;
    }

    public void setContent(String content) {
        if(Objects.isNull(content) || content.isBlank()) {
            throw new TaskValidInputException("[task] 입력값이 null");
        }
        this.content = content;
    }

}
