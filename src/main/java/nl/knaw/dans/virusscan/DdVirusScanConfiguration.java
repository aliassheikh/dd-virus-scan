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

package nl.knaw.dans.virusscan;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.core.Configuration;
import nl.knaw.dans.virusscan.core.config.DataverseConfig;
import nl.knaw.dans.virusscan.core.config.VirusScannerConfig;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class DdVirusScanConfiguration extends Configuration {

    @Valid
    @NotNull
    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

    @Valid
    @NotNull
    private DataverseConfig dataverse;
    @Valid
    @NotNull
    private VirusScannerConfig virusscanner;

    public JerseyClientConfiguration getJerseyClient() {
        return jerseyClient;
    }

    public void setJerseyClient(JerseyClientConfiguration jerseyClient) {
        this.jerseyClient = jerseyClient;
    }

    public DataverseConfig getDataverse() {
        return dataverse;
    }

    public void setDataverse(DataverseConfig dataverse) {
        this.dataverse = dataverse;
    }

    public VirusScannerConfig getVirusscanner() {
        return virusscanner;
    }

    public void setVirusscanner(VirusScannerConfig virusscanner) {
        this.virusscanner = virusscanner;
    }

    @JsonProperty("jerseyClient")
    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClient;
    }

    @JsonProperty("jerseyClient")
    public void setJerseyClientConfiguration(JerseyClientConfiguration jerseyClient) {
        this.jerseyClient = jerseyClient;
    }

}
