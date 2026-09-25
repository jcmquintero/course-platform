package com.courses.platform.course;

import com.courses.platform.category.Category;
import com.courses.platform.instructor.Instructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "estimated_hours", nullable = false)
    private int estimatedHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseLevel level;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "max_seats", nullable = false)
    private int maxSeats;

    @Column(name = "occupied_seats", nullable = false)
    private int occupiedSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;

    protected Course() {
    }

    public Course(
            String title,
            String description,
            int estimatedHours,
            CourseLevel level,
            BigDecimal price,
            int maxSeats,
            Category category,
            Instructor instructor) {

        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.estimatedHours = estimatedHours;
        this.level = level;
        this.price = price;
        this.maxSeats = maxSeats;
        this.occupiedSeats = 0;
        this.status = CourseStatus.DRAFT;
        this.category = category;
        this.instructor = instructor;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getEstimatedHours() {
        return estimatedHours;
    }

    public CourseLevel getLevel() {
        return level;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getMaxSeats() {
        return maxSeats;
    }

    public int getOccupiedSeats() {
        return occupiedSeats;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public Category getCategory() {
        return category;
    }

    public Instructor getInstructor() {
        return instructor;
    }
}