package com.example.taskapi.domain;

/**
 * Task lifecycle values, stored as strings in the database
 * ({@code @Enumerated(EnumType.STRING)} on {@link Task}).
 * <p>
 * Java 21 switch expressions work nicely with enums — see the week 2 learning tests.
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}
