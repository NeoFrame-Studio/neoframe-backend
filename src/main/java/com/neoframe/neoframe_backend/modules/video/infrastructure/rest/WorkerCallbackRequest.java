package com.neoframe.neoframe_backend.modules.video.infrastructure.rest;

import java.util.UUID;

record WorkerCallbackRequest(UUID jobId, String status, String videoUrl) {}