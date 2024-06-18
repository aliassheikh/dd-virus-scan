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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VirusScannerImplTest {

    List<String> doScan(Pattern negativePattern, Pattern positivePattern, String output) throws IOException {
        var config = new VirusScannerConfig();
        config.setResultNegativePattern(negativePattern);
        config.setResultPositivePattern(positivePattern);

        var clamd = Mockito.mock(ClamdService.class);
        Mockito.when(clamd.scanStream(Mockito.any())).thenReturn(Set.of(output));

        var scanner = new VirusScannerImpl(config, clamd);
        var inputStream = new ByteArrayInputStream("hello there".getBytes());

        return scanner.scanForVirus(inputStream);
    }

    @Test
    void scanForVirusEverythingOk() throws IOException {
        var negativePattern = Pattern.compile("^stream: OK$");
        var positivePattern = Pattern.compile("^stream: (.*)$");

        var result = doScan(negativePattern, positivePattern, "stream: OK\0");
        assertEquals(0, result.size());
    }

    @Test
    void scanForVirusVirusFound() throws IOException {
        var negativePattern = Pattern.compile("^stream: OK$");
        var positivePattern = Pattern.compile("^stream: (.*)$");

        var result = doScan(negativePattern, positivePattern, "stream: something.evil.found\0");
        assertEquals(1, result.size());
    }
    @Test
    void scanForVirusPropagatesException() throws IOException {
        var negativePattern = Pattern.compile("^stream: OK$");
        var positivePattern = Pattern.compile("^stream: (.*)$");

        var config = new VirusScannerConfig();
        config.setResultNegativePattern(negativePattern);
        config.setResultPositivePattern(positivePattern);

        var clamd = Mockito.mock(ClamdService.class);
        Mockito.when(clamd.scanStream(Mockito.any())).thenThrow(IOException.class);

        var scanner = new VirusScannerImpl(config, clamd);
        var inputStream = new ByteArrayInputStream("hello there".getBytes());

        assertThrows(IOException.class, () -> scanner.scanForVirus(inputStream));

    }
}