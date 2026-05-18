package com.neoframe.neoframe_backend.core.domain;

public enum Plan {
    STARTER(1),
    PRO(3),
    SAAS(5);

    private final int maxConcurrentJobs;

    Plan(int maxConcurrentJobs) {
        this.maxConcurrentJobs = maxConcurrentJobs;
    }

    public int getMaxConcurrentJobs() {
        return maxConcurrentJobs;
    }
}