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
import nl.knaw.dans.lib.dataverse.model.file.FileMeta;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface DataverseApiService {

    List<FileMeta> listFiles(String datasetId, String invocationId, String version) throws IOException, DataverseException;

    <T> T getFile(int fileId, HttpClientResponseHandler<T> handler) throws IOException, DataverseException;

    void completeWorkflow(String invocationId, String reason, String message) throws IOException, DataverseException;

    void failWorkflow(String invocationId, String reason, String message) throws IOException, DataverseException;

    void checkConnection() throws IOException, DataverseException;
}
