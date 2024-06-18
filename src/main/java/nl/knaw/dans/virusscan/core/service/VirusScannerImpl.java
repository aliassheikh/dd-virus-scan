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

import nl.knaw.dans.virusscan.config.VirusScannerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class VirusScannerImpl implements VirusScanner {

    private static final Logger log = LoggerFactory.getLogger(VirusScanner.class);

    private final VirusScannerConfig virusScannerConfig;

    private final ClamdService clamdService;

    public VirusScannerImpl(VirusScannerConfig virusScannerConfig, ClamdService clamdService) {
        this.virusScannerConfig = virusScannerConfig;
        this.clamdService = clamdService;
    }

    @Override
    public List<String> scanForVirus(InputStream inputStream) throws IOException {
        var result = clamdService.scanStream(inputStream);

        return result.stream()
            .map(line -> {
                // filter out trailing zero bytes
                var index = line.indexOf('\0');

                if (index > -1) {
                    return line.substring(0, index);
                }
                else {
                    return line;
                }

            })
            .map(String::strip).map(line -> {
                var positiveMatcher = virusScannerConfig.getResultPositivePattern().matcher(line);
                var negativeMatcher = virusScannerConfig.getResultNegativePattern().matcher(line);

                // the negative matcher is stricter than the positive matcher, so we need to make sure it does NOT match a negative result
                // before checking for a positive match
                if (!negativeMatcher.matches() && positiveMatcher.matches()) {
                    return positiveMatcher.group(1);
                }

                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
