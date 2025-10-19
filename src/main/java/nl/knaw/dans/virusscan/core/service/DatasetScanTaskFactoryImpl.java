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
package nl.knaw.dans.virusscan.core.service;

import nl.knaw.dans.virusscan.api.PrePublishWorkflowPayloadDto;
import nl.knaw.dans.virusscan.core.task.DatasetScanTask;

import java.util.concurrent.ExecutorService;

public class DatasetScanTaskFactoryImpl implements DatasetScanTaskFactory {

    private final DataverseApiService dataverseApiService;
    private final VirusScanner virusScanner;

    private final ExecutorService executorService;
    private final DatasetResumeTaskFactory datasetResumeTaskFactory;

    public DatasetScanTaskFactoryImpl(DataverseApiService dataverseApiService, VirusScanner virusScanner, ExecutorService executorService, DatasetResumeTaskFactory datasetResumeTaskFactory) {
        this.dataverseApiService = dataverseApiService;
        this.virusScanner = virusScanner;
        this.executorService = executorService;
        this.datasetResumeTaskFactory = datasetResumeTaskFactory;
    }

    @Override
    public void startTask(PrePublishWorkflowPayloadDto payload) {
        var task = new DatasetScanTask(dataverseApiService, virusScanner, payload, datasetResumeTaskFactory);
        executorService.submit(task);
    }
}
