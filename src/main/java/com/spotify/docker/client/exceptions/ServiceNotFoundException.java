package com.spotify.docker.client.exceptions;

public class ServiceNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 124167900943701078L;

    private final String serviceId;

    public ServiceNotFoundException(final String serviceId, final Throwable cause) {
        super("Service not found: " + serviceId, cause);
        this.serviceId = serviceId;
    }

    public ServiceNotFoundException(final String serviceId) {
        this(serviceId, null);
    }

    public String getServiceId() {
        return serviceId;
    }
}
