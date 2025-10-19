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
import nl.knaw.dans.lib.dataverse.model.file.DataFile;
import nl.knaw.dans.lib.dataverse.model.file.FileMeta;
import nl.knaw.dans.virusscan.config.ResumeTasksConfig;
import nl.knaw.dans.virusscan.core.model.DatasetResumeTaskPayload;
import nl.knaw.dans.virusscan.core.service.DataverseApiService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DatasetResumeTaskTest {
    private final DataverseApiService dataverseApiServiceMock = Mockito.mock(DataverseApiService.class);

    @Test
    void runSuccessful() throws InterruptedException, IOException, DataverseException {
        var config = new ResumeTasksConfig(3, Duration.of(3, ChronoUnit.SECONDS));
        var datasetResumeTask = new DatasetResumeTask(dataverseApiServiceMock, new DatasetResumeTaskPayload(), config);
        var task = Mockito.spy(datasetResumeTask);
        Mockito.doNothing().when(task).sleep(Mockito.any());
        Mockito.doNothing().when(task).runTask();

        task.run();


        Mockito.verify(task, Mockito.times(1)).runTask();
    }

    @Test
    void runWithIOExceptions() throws InterruptedException, IOException, DataverseException {
        var config = new ResumeTasksConfig(3, Duration.of(3, ChronoUnit.SECONDS));
        var datasetResumeTask = new DatasetResumeTask(dataverseApiServiceMock, new DatasetResumeTaskPayload(), config);
        var task = Mockito.spy(datasetResumeTask);
        Mockito.doNothing().when(task).sleep(Mockito.any());
        Mockito.doThrow(IOException.class).when(task).runTask();

        task.run();

        Mockito.verify(task, Mockito.times(3)).runTask();
        Mockito.verify(task, Mockito.times(2)).sleep(Duration.of(3, ChronoUnit.SECONDS));
    }

    @Test
    void testThreadSleep() throws InterruptedException {
        var datasetResumeTask = new DatasetResumeTask(dataverseApiServiceMock, new DatasetResumeTaskPayload(), new ResumeTasksConfig(3, Duration.of(3, ChronoUnit.SECONDS)));
        var task = Mockito.spy(datasetResumeTask);
        var duration = Duration.of(1, ChronoUnit.MILLIS);
        assertDoesNotThrow(() -> task.sleep(duration));
    }

    @Test
    void runWithInterrupted() throws InterruptedException, IOException, DataverseException {
        var config = new ResumeTasksConfig(3, Duration.of(3, ChronoUnit.SECONDS));
        var datasetResumeTask = new DatasetResumeTask(dataverseApiServiceMock, new DatasetResumeTaskPayload(), config);
        var task = Mockito.spy(datasetResumeTask);

        Mockito.doThrow(IOException.class).when(task).runTask();
        Mockito.doThrow(InterruptedException.class).when(task).sleep(Mockito.any());

        task.run();

        // first the task will be run and throw an exception
        Mockito.verify(task, Mockito.times(1)).runTask();
        // then the pause between tries is triggered, which also throw exceptions
        Mockito.verify(task, Mockito.times(1)).sleep(Duration.of(3, ChronoUnit.SECONDS));
    }

    @Test
    void runTaskWithNoPositiveMatches() throws IOException, DataverseException {
        var dataverseApiService = Mockito.mock(DataverseApiService.class);
        var payload = new DatasetResumeTaskPayload();
        payload.setId("id");
        payload.setInvocationId("invocation_id");
        payload.setMatches(Map.of());
        var config = new ResumeTasksConfig(3, Duration.of(3, ChronoUnit.SECONDS));

        var task = new DatasetResumeTask(dataverseApiService, payload, config);

        task.runTask();

        Mockito.verify(dataverseApiService).completeWorkflow(Mockito.eq("invocation_id"), Mockito.anyString(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(dataverseApiService);
    }

    @Test
    void runTaskWithSomeMatches() throws IOException, DataverseException {
        var dataverseApiService = Mockito.mock(DataverseApiService.class);
        var file1 = new FileMeta();
        file1.setDataFile(new DataFile());
        file1.getDataFile().setFilename("filename1.txt");

        var file2 = new FileMeta();
        file2.setDataFile(new DataFile());
        file2.getDataFile().setFilename("filename2.txt");

        var payload = new DatasetResumeTaskPayload();
        payload.setId("id");
        payload.setInvocationId("invocation_id");
        payload.setMatches(Map.of(
            file1, List.of("virus1"),
            file2, List.of("virus2")
        ));

        var config = new ResumeTasksConfig(3, Duration.of(3, ChronoUnit.SECONDS));

        var task = new DatasetResumeTask(dataverseApiService, payload, config);

        task.runTask();

        Mockito.verify(dataverseApiService).failWorkflow(Mockito.eq("invocation_id"), Mockito.anyString(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(dataverseApiService);
    }

    @Test
    void payloadGeneration() {
        var task = new DatasetResumeTask(dataverseApiServiceMock, new DatasetResumeTaskPayload(), new ResumeTasksConfig(3, Duration.of(3, ChronoUnit.SECONDS)));

        var file1 = new FileMeta();
        file1.setDataFile(new DataFile());
        file1.getDataFile().setFilename("filename1.txt");

        var file2 = new FileMeta();
        file2.setDataFile(new DataFile());
        file2.getDataFile().setFilename("filename2.txt");

        var payload = List.of(
            Map.entry(file1, List.of("virus1 FOUND")),
            Map.entry(file2, List.of("virus2 FOUND"))
        );

        var result = task.generatePayload(payload);
        assertEquals("filename1.txt -> virus1 FOUND, filename2.txt -> virus2 FOUND", result);

    }
}