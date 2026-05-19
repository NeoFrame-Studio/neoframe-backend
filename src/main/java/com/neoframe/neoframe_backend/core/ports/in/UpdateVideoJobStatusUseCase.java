package com.neoframe.neoframe_backend.core.ports.in;

import java.util.UUID;

public interface UpdateVideoJobStatusUseCase {
    /**
     * Updates the status and result of a video job from an external worker.
     *
     * @param jobId The unique ID of the job that was processed
     * @param status The new status (COMPLETED or FAILED)
     * @param videoUrl A final URL of the generated video (null in case of failure)
     */
    void execute(UUID jobId, String status, String videoUrl);
}
