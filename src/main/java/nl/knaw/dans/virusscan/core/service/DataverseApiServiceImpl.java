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

import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.lib.dataverse.DataverseClientConfig;
import nl.knaw.dans.lib.dataverse.DataverseException;
import nl.knaw.dans.lib.dataverse.model.file.FileMeta;
import nl.knaw.dans.lib.dataverse.model.workflow.ResumeMessage;
import nl.knaw.dans.virusscan.core.config.DataverseConfig;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

public class DataverseApiServiceImpl implements DataverseApiService {
    private static final Logger log = LoggerFactory.getLogger(DataverseApiServiceImpl.class);

    private final DataverseConfig dataverseConfig;
    private final Client httpClient;

    private DataverseClient client;

    public DataverseApiServiceImpl(DataverseConfig dataverseConfig, Client client) {
        this.dataverseConfig = dataverseConfig;
        this.httpClient = client;
    }

    DataverseClient getDataverseClient() {
        if (this.client == null) {
            var config = new DataverseClientConfig(URI.create(dataverseConfig.getBaseUrl()), dataverseConfig.getApiToken());
            this.client = new DataverseClient(config);
        }

        return client;
    }

    @Override
    public List<FileMeta> listFiles(String datasetId, String invocationId, String version) throws IOException, DataverseException {
        log.trace("Getting list of files for data set {}, invocation id {} and version :draft", datasetId, invocationId);
        var dataset = getDataverseClient().dataset(datasetId, invocationId);
        var files = dataset.getFiles(":draft");

        return files.getData();
    }

    @Override
    public <T> T getFile(int fileId, HttpClientResponseHandler<T> handler) throws IOException, DataverseException {
        log.trace("Getting file with id {}", fileId);
        return getDataverseClient().basicFileAccess(fileId).getFile(handler);
    }

    @Override
    public void completeWorkflow(String invocationId, String reason, String message) throws IOException, DataverseException {
        var resumeMessage = new ResumeMessage("Success", reason, message);
        log.trace("Completing workflow with status Success, invocation id is {}", invocationId);
        this.getDataverseClient().workflows().resume(invocationId, resumeMessage);
    }

    @Override
    public void failWorkflow(String invocationId, String reason, String message) throws IOException, DataverseException {
        var resumeMessage = new ResumeMessage("Failure", reason, message);
        log.trace("Completing workflow with status Failure, reasin is '{}', message is '{}', invocation id is {}", reason, message, invocationId);
        this.getDataverseClient().workflows().resume(invocationId, resumeMessage);
    }

    @Override
    public void checkConnection() throws IOException, DataverseException {
        this.getDataverseClient().checkConnection();
    }

}
