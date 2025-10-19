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

import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Environment;
import nl.knaw.dans.lib.util.DataverseHealthCheck;
import nl.knaw.dans.virusscan.config.DdVirusScanConfig;
import nl.knaw.dans.virusscan.core.service.ClamdServiceImpl;
import nl.knaw.dans.virusscan.core.service.DatasetResumeTaskFactoryImpl;
import nl.knaw.dans.virusscan.core.service.DatasetScanTaskFactoryImpl;
import nl.knaw.dans.virusscan.core.service.DataverseApiServiceImpl;
import nl.knaw.dans.virusscan.core.service.VirusScannerImpl;
import nl.knaw.dans.virusscan.health.ClamdHealthCheck;
import nl.knaw.dans.virusscan.resource.InvokeResourceImpl;
import nl.knaw.dans.virusscan.resource.RollbackResourceImpl;

public class DdVirusScanApplication extends Application<DdVirusScanConfig> {

    public static void main(final String[] args) throws Exception {
        new DdVirusScanApplication().run(args);
    }

    @Override
    public String getName() {
        return "DD Virus Scan";
    }

    @Override
    public void run(final DdVirusScanConfig configuration, final Environment environment) {
        var scanDatasetTaskQueue = configuration.getVirusscanner().getScanDatasetTaskQueue().build(environment);
        var resumeDatasetTaskQueue = configuration.getVirusscanner().getResumeDatasetTaskQueue().build(environment);
        var clamdService = new ClamdServiceImpl(configuration.getVirusscanner().getClamd());
        var dataverseClient = configuration.getDataverse().build(environment, "dd-virus-scan/dataverse");
        var dataverseApiService = new DataverseApiServiceImpl(dataverseClient);
        var virusScanner = new VirusScannerImpl(configuration.getVirusscanner(), clamdService);

        var datasetResumeTaskFactory = new DatasetResumeTaskFactoryImpl(dataverseApiService, resumeDatasetTaskQueue, configuration.getVirusscanner().getResumeTasks());
        var datasetScanTaskFactory = new DatasetScanTaskFactoryImpl(dataverseApiService, virusScanner, scanDatasetTaskQueue, datasetResumeTaskFactory);

        environment.jersey().register(new InvokeResourceImpl(datasetScanTaskFactory));
        environment.jersey().register(new RollbackResourceImpl());

        environment.healthChecks().register("Clamd", new ClamdHealthCheck(clamdService));
        environment.healthChecks().register("Dataverse", new DataverseHealthCheck(dataverseClient));
    }
}
