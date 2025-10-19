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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.lib.dataverse.DataverseException;
import nl.knaw.dans.lib.dataverse.model.file.FileMeta;
import nl.knaw.dans.lib.dataverse.model.workflow.ResumeMessage;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

import java.io.IOException;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class DataverseApiServiceImpl implements DataverseApiService {
    private DataverseClient client;

    @Override
    public List<FileMeta> listFiles(String datasetId, String invocationId, String version) throws IOException, DataverseException {
        log.debug("Getting list of files for data set {}, invocation id {} and version :draft", datasetId, invocationId);
        var dataset = client.dataset(datasetId, invocationId);
        var files = dataset.getFiles(":draft");

        return files.getData();
    }

    @Override
    public <T> T getFile(int fileId, HttpClientResponseHandler<T> handler) throws IOException, DataverseException {
        log.debug("Getting file with id {}", fileId);
        return client.basicFileAccess(fileId).getFile(handler);
    }

    @Override
    public void completeWorkflow(String invocationId, String reason, String message) throws IOException, DataverseException {
        var resumeMessage = new ResumeMessage("Success", reason, message);
        log.info("Completing workflow with status Success, invocation id is {}", invocationId);
        this.client.workflows().resume(invocationId, resumeMessage);
    }

    @Override
    public void failWorkflow(String invocationId, String reason, String message) throws IOException, DataverseException {
        var resumeMessage = new ResumeMessage("Failure", reason, message);
        log.warn("Completing workflow with status Failure, reason is '{}', message is '{}', invocation id is {}", reason, message, invocationId);
        this.client.workflows().resume(invocationId, resumeMessage);
    }

    @Override
    public void checkConnection() throws IOException, DataverseException {
        this.client.checkConnection();
    }

}
