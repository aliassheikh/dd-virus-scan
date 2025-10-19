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

import nl.knaw.dans.lib.dataverse.DataverseException;
import nl.knaw.dans.lib.dataverse.model.file.FileMeta;
import nl.knaw.dans.virusscan.api.PrePublishWorkflowPayloadDto;
import nl.knaw.dans.virusscan.core.model.DatasetResumeTaskPayload;
import nl.knaw.dans.virusscan.core.service.DatasetResumeTaskFactory;
import nl.knaw.dans.virusscan.core.service.DataverseApiService;
import nl.knaw.dans.virusscan.core.service.DataverseApiServiceImpl;
import nl.knaw.dans.virusscan.core.service.VirusScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class DatasetScanTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(DataverseApiServiceImpl.class);
    private final DataverseApiService dataverseApiService;
    private final VirusScanner virusScanner;
    private final PrePublishWorkflowPayloadDto payload;
    private final DatasetResumeTaskFactory datasetResumeTaskFactory;

    public DatasetScanTask(DataverseApiService dataverseApiService, VirusScanner virusScanner, PrePublishWorkflowPayloadDto payload, DatasetResumeTaskFactory datasetResumeTaskFactory) {
        this.dataverseApiService = dataverseApiService;
        this.virusScanner = virusScanner;
        this.payload = payload;
        this.datasetResumeTaskFactory = datasetResumeTaskFactory;
    }

    @Override
    public void run() {
        try {
            runTask();
        }
        catch (Exception e) {
            log.error("Error checking files", e);
        }
    }

    void runTask() throws IOException, DataverseException {
        // fetch all files by ID
        var files = dataverseApiService.listFiles(payload.getGlobalId(), payload.getInvocationId(), String.format("%s.%s", payload.getMajorVersion(), payload.getMinorVersion()));
        var fileMatches = new HashMap<FileMeta, List<String>>();

        for (var file : files) {
            log.debug("Checking status for file {}", file.getDataFile().getFilename());
            var viruses = dataverseApiService.getFile(file.getDataFile().getId(), response -> virusScanner.scanForVirus(response.getEntity().getContent()));

            if (!viruses.isEmpty()) {
                log.warn("Found {} viruses in file {}", viruses.size(), file);

                for (var virus : viruses) {
                    log.warn(" - {}", virus);
                }

                fileMatches.put(file, viruses);
            }
        }

        var resultPayload = new DatasetResumeTaskPayload();
        resultPayload.setId(payload.getGlobalId());
        resultPayload.setInvocationId(payload.getInvocationId());
        resultPayload.setMatches(fileMatches);

        datasetResumeTaskFactory.completeWorkflow(resultPayload);
    }
}
