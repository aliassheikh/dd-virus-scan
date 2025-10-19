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

import nl.knaw.dans.lib.dataverse.DataverseException;
import nl.knaw.dans.virusscan.api.PrePublishWorkflowPayloadDto;
import nl.knaw.dans.virusscan.core.model.DatasetResumeTaskPayload;
import nl.knaw.dans.virusscan.core.task.DatasetScanTask;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.eq;

class DatasetScanTaskTest {
    @Test
    void runNoVirusFound() throws IOException, DataverseException {
        var dataverseApiService = Mockito.mock(DataverseApiService.class);
        var virusScanner = Mockito.mock(VirusScanner.class);
        var datasetResumeTaskFactory = Mockito.mock(DatasetResumeTaskFactory.class);
        var payload = new PrePublishWorkflowPayloadDto();
        payload.setInvocationId("invocation_id");
        payload.setGlobalId("some_global_id");
        payload.setMajorVersion("1");
        payload.setMinorVersion("2");

        var json = " {\"status\":\"OK\",\"data\":[{\"label\":\"CODING.pdf\",\"restricted\":false,\"version\":1,\"datasetVersionId\":2,\"dataFile\":{\"id\":4,\"persistentId\":\"\",\"pidURL\":\"\",\"filename\":\"CODING.pdf\",\"contentType\":\"application/pdf\",\"filesize\":1047589,\"storageIdentifier\":\"file://181a9ae3b38-97327cb37b42\",\"rootDataFileId\":-1,\"checksum\":{\"type\":\"SHA-1\",\"value\":\"9c1b89cf6be851b81727f22ac155cce215c6a342\"},\"creationDate\":\"2022-06-28\"}},{\"label\":\"CreateSpace.RPM.Feb.2016.ISBN.1517188466.pdf\",\"restricted\":false,\"version\":1,\"datasetVersionId\":2,\"dataFile\":{\"id\":3,\"persistentId\":\"\",\"pidURL\":\"\",\"filename\":\"CreateSpace.RPM.Feb.2016.ISBN.1517188466.pdf\",\"contentType\":\"application/pdf\",\"filesize\":2277705,\"storageIdentifier\":\"file://181a9ae3c58-f0959eb1e774\",\"rootDataFileId\":-1,\"checksum\":{\"type\":\"SHA-1\",\"value\":\"ccf4a0cc15e9ce309d8be4e9626bbc87effac405\"},\"creationDate\":\"2022-06-28\"}},{\"label\":\"Exploring Expect.pdf\",\"restricted\":false,\"version\":1,\"datasetVersionId\":2,\"dataFile\":{\"id\":6,\"persistentId\":\"\",\"pidURL\":\"\",\"filename\":\"Exploring Expect.pdf\",\"contentType\":\"application/pdf\",\"filesize\":25977953,\"storageIdentifier\":\"file://181a9ae3f7d-d0d6af49fb44\",\"rootDataFileId\":-1,\"checksum\":{\"type\":\"SHA-1\",\"value\":\"196007cdf6d4358cd3ffd0fde4c197ae6a27c323\"},\"creationDate\":\"2022-06-28\"}},{\"label\":\"formal-05-07-04.pdf\",\"restricted\":false,\"version\":1,\"datasetVersionId\":2,\"dataFile\":{\"id\":5,\"persistentId\":\"\",\"pidURL\":\"\",\"filename\":\"formal-05-07-04.pdf\",\"contentType\":\"application/pdf\",\"filesize\":5542288,\"storageIdentifier\":\"file://181a9ae4179-c9cb7e5450f9\",\"rootDataFileId\":-1,\"checksum\":{\"type\":\"SHA-1\",\"value\":\"e29cf3ae1fbef9b3031d8ca2044c2e8d70013814\"},\"creationDate\":\"2022-06-28\"}}]}";
        var result = new MockedDataverseResponse(json);

        Mockito.when(dataverseApiService.listFiles(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(result.getData());

        Mockito.when(dataverseApiService.getFile(Mockito.anyInt(), Mockito.any()))
            .thenReturn(new ArrayList<>());

        var task = new DatasetScanTask(dataverseApiService, virusScanner, payload, datasetResumeTaskFactory);
        task.run();

        var expectedPayload = new DatasetResumeTaskPayload();
        expectedPayload.setId("some_global_id");
        expectedPayload.setInvocationId("invocation_id");
        expectedPayload.setMatches(new HashMap<>());

        Mockito.verify(datasetResumeTaskFactory).completeWorkflow(eq(expectedPayload));
    }

    @Test
    void runFoundVirusInFirstFile() throws IOException, DataverseException {
        var dataverseApiService = Mockito.mock(DataverseApiService.class);
        var virusScanner = Mockito.mock(VirusScanner.class);
        var datasetResumeTaskFactory = Mockito.mock(DatasetResumeTaskFactory.class);
        var payload = new PrePublishWorkflowPayloadDto();
        payload.setInvocationId("invocation_id");
        payload.setGlobalId("some_global_id");
        payload.setMajorVersion("1");
        payload.setMinorVersion("2");

        var json = " {\"status\":\"OK\",\"data\":[{\"label\":\"CODING.pdf\",\"restricted\":false,\"version\":1,\"datasetVersionId\":2,\"dataFile\":{\"id\":4,\"persistentId\":\"\",\"pidURL\":\"\",\"filename\":\"CODING.pdf\",\"contentType\":\"application/pdf\",\"filesize\":1047589,\"storageIdentifier\":\"file://181a9ae3b38-97327cb37b42\",\"rootDataFileId\":-1,\"checksum\":{\"type\":\"SHA-1\",\"value\":\"9c1b89cf6be851b81727f22ac155cce215c6a342\"},\"creationDate\":\"2022-06-28\"}},{\"label\":\"CreateSpace.RPM.Feb.2016.ISBN.1517188466.pdf\",\"restricted\":false,\"version\":1,\"datasetVersionId\":2,\"dataFile\":{\"id\":3,\"persistentId\":\"\",\"pidURL\":\"\",\"filename\":\"CreateSpace.RPM.Feb.2016.ISBN.1517188466.pdf\",\"contentType\":\"application/pdf\",\"filesize\":2277705,\"storageIdentifier\":\"file://181a9ae3c58-f0959eb1e774\",\"rootDataFileId\":-1,\"checksum\":{\"type\":\"SHA-1\",\"value\":\"ccf4a0cc15e9ce309d8be4e9626bbc87effac405\"},\"creationDate\":\"2022-06-28\"}},{\"label\":\"Exploring Expect.pdf\",\"restricted\":false,\"version\":1,\"datasetVersionId\":2,\"dataFile\":{\"id\":6,\"persistentId\":\"\",\"pidURL\":\"\",\"filename\":\"Exploring Expect.pdf\",\"contentType\":\"application/pdf\",\"filesize\":25977953,\"storageIdentifier\":\"file://181a9ae3f7d-d0d6af49fb44\",\"rootDataFileId\":-1,\"checksum\":{\"type\":\"SHA-1\",\"value\":\"196007cdf6d4358cd3ffd0fde4c197ae6a27c323\"},\"creationDate\":\"2022-06-28\"}},{\"label\":\"formal-05-07-04.pdf\",\"restricted\":false,\"version\":1,\"datasetVersionId\":2,\"dataFile\":{\"id\":5,\"persistentId\":\"\",\"pidURL\":\"\",\"filename\":\"formal-05-07-04.pdf\",\"contentType\":\"application/pdf\",\"filesize\":5542288,\"storageIdentifier\":\"file://181a9ae4179-c9cb7e5450f9\",\"rootDataFileId\":-1,\"checksum\":{\"type\":\"SHA-1\",\"value\":\"e29cf3ae1fbef9b3031d8ca2044c2e8d70013814\"},\"creationDate\":\"2022-06-28\"}}]}";
        var result = new MockedDataverseResponse(json);
        var data = result.getData();

        Mockito.when(dataverseApiService.listFiles(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(data);

        Mockito.when(dataverseApiService.getFile(eq(4), Mockito.any()))
            .thenReturn(List.of("virus!"));

        Mockito.when(dataverseApiService.getFile(not(eq(4)), Mockito.any()))
            .thenReturn(new ArrayList<>());

        var task = new DatasetScanTask(dataverseApiService, virusScanner, payload, datasetResumeTaskFactory);
        task.run();

        var expectedPayload = new DatasetResumeTaskPayload();
        expectedPayload.setId("some_global_id");
        expectedPayload.setInvocationId("invocation_id");
        expectedPayload.setMatches(Map.of(data.get(0), List.of("virus!")));

        Mockito.verify(datasetResumeTaskFactory).completeWorkflow(eq(expectedPayload));

    }

}