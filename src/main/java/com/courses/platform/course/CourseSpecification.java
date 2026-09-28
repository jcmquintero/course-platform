package com.courses.platform.course;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

public final class CourseSpecification {

    private CourseSpecification() {
    }

    public static Specification<Course> hasCategory(UUID categoryId) {
        return (root, query, builder) ->
                categoryId == null
                        ? builder.conjunction()
                        : builder.equal(
                                root.get("category").get("id"),
                                categoryId);
    }

    public static Specification<Course> hasLevel(CourseLevel level) {
        return (root, query, builder) ->
                level == null
                        ? builder.conjunction()
                        : builder.equal(root.get("level"), level);
    }

    public static Specification<Course> titleContains(String title) {
        return (root, query, builder) -> {
            if (title == null || title.isBlank()) {
                return builder.conjunction();
            }

            return builder.like(
                    builder.lower(root.get("title")),
                    "%" + title.toLowerCase() + "%");
        };
    }

    public static Specification<Course> priceAtLeast(BigDecimal minPrice) {
        return (root, query, builder) ->
                minPrice == null
                        ? builder.conjunction()
                        : builder.greaterThanOrEqualTo(
                                root.get("price"),
                                minPrice);
    }

    public static Specification<Course> priceAtMost(BigDecimal maxPrice) {
        return (root, query, builder) ->
                maxPrice == null
                        ? builder.conjunction()
                        : builder.lessThanOrEqualTo(
                                root.get("price"),
                                maxPrice);
    }

    public static Specification<Course> hasAvailability(Boolean available) {
        return (root, query, builder) -> {
            if (available == null) {
                return builder.conjunction();
            }

            if (available) {
                return builder.lessThan(
                        root.get("occupiedSeats"),
                        root.get("maxSeats"));
            }

            return builder.greaterThanOrEqualTo(
                    root.get("occupiedSeats"),
                    root.get("maxSeats"));
        };
    }
}