package com.spotify.docker.client.exceptions;

public class TaskNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 4974524646762384518L;

    private final String taskId;

    public TaskNotFoundException(final String taskId, final Throwable cause) {
        super("Task not found: " + taskId, cause);
        this.taskId = taskId;
    }

    public TaskNotFoundException(final String taskId) {
        this(taskId, null);
    }

    public String getTaskId() {
        return taskId;
    }
}
