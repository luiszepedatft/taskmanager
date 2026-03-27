package com.example.taskmanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "contexts", uniqueConstraints = {
        // this makes that the combination of name of the context
        // and owner of the context are unique. Making it so that
        // any user can only have 1 context with a particular name.
        // but context names can repeat across users.
        @UniqueConstraint(columnNames = {"name", "user_id"})
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Context {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name",  nullable = false)
    private String name;

    @Column(name="description",  nullable = false )
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "context")
    private List<Task> tasks;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
