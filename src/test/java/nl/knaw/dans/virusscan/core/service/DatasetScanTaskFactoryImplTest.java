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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.ExecutorService;

class DatasetScanTaskFactoryImplTest {

    @Test
    void startTask() {
        var dataverseApiService = Mockito.mock(DataverseApiService.class);
        var virusScanner = Mockito.mock(VirusScanner.class);
        var executorService = Mockito.mock(ExecutorService.class);
        var datasetResumeTaskFactory = Mockito.mock(DatasetResumeTaskFactory.class);

        var task = new DatasetScanTaskFactoryImpl(dataverseApiService, virusScanner, executorService, datasetResumeTaskFactory);

        var payload = new PrePublishWorkflowPayloadDto();
        task.startTask(payload);

        Mockito.verify(executorService).submit(Mockito.any(DatasetScanTask.class));
    }
}