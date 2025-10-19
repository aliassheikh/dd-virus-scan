/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.virusscan.core.task;

import lombok.NonNull;
import nl.knaw.dans.lib.dataverse.DataverseException;
import nl.knaw.dans.lib.dataverse.model.file.FileMeta;
import nl.knaw.dans.virusscan.config.ResumeTasksConfig;
import nl.knaw.dans.virusscan.core.model.DatasetResumeTaskPayload;
import nl.knaw.dans.virusscan.core.service.DataverseApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DatasetResumeTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(DatasetResumeTask.class);
    private final DataverseApiService dataverseApiService;
    private final DatasetResumeTaskPayload payload;

    private final ResumeTasksConfig resumeTasksConfig;

    public DatasetResumeTask(@NonNull DataverseApiService dataverseApiService, @NonNull DatasetResumeTaskPayload payload, @NonNull ResumeTasksConfig resumeTasksConfig) {
        this.dataverseApiService = dataverseApiService;
        this.payload = payload;
        this.resumeTasksConfig = resumeTasksConfig;
    }

    @Override
    public void run() {
        var maxTries = resumeTasksConfig.getMaxTries();
        var pauseDuration = resumeTasksConfig.getWaitBetweenTries();

        for (var i = 0; i < maxTries; ++i) {
            try {
                // don't sleep on the first run
                if (i > 0) {
                    sleep(pauseDuration);
                }
            }
            catch (InterruptedException e) {
                log.error("Exception was thrown while waiting between resume attempts");
                break;
            }

            // now run the task
            try {
                runTask();
                log.info("Dataset resume task completed with status {}", payload.getMatches().isEmpty() ? "Success" : "Failure");
                break;
            }
            catch (IOException | DataverseException e) {
                log.error("Error completing workflow", e);
            }
        }
    }

    void sleep(Duration duration) throws InterruptedException {
        Thread.sleep(duration.toMillis());
    }

    void runTask() throws IOException, DataverseException {

        if (payload.getMatches().isEmpty()) {
            log.info("Dataset with id {} and invocation ID {} is clean", payload.getId(), payload.getInvocationId());
            dataverseApiService.completeWorkflow(payload.getInvocationId(), "Virus scan workflow completed",
                "An external workflow to scan for viruses has completed and found no threats in the dataset");

        }
        else {
            log.warn("Dataset with id {} and invocation ID {} contains positive matches", payload.getId(), payload.getInvocationId());

            var messages = generatePayload(new ArrayList<>(payload.getMatches().entrySet()));

            payload.getMatches().entrySet().forEach(file -> {
                var filename = file.getKey().getDataFile().getFilename();
                var errors = String.join(" and ", file.getValue());
                log.info("Dataset file {} contains match: {}", filename, errors);
            });

            dataverseApiService.failWorkflow(payload.getInvocationId(), messages, messages);
        }
    }

    String generatePayload(List<Map.Entry<FileMeta, List<String>>> matches) {

        return matches.stream().map(file -> {
            var filename = file.getKey().getDataFile().getFilename();
            var errors = String.join(" and ", file.getValue());
            return String.format("%s -> %s", filename, errors);
        }).collect(Collectors.joining(", "));
    }

}
