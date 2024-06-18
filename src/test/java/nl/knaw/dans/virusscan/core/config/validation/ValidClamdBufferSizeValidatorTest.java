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
package nl.knaw.dans.virusscan.core.config.validation;

import nl.knaw.dans.virusscan.config.ClamdConfig;
import nl.knaw.dans.virusscan.config.validation.ValidClamdBufferSizeValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidClamdBufferSizeValidatorTest {

    @Test
    void isValid() {
        // chunk size should be at least twice as big as overlap size
        // overlap size should be at least twice as big as buffer size
        var config = new ClamdConfig();
        config.setChunksize(1024*1024);
        config.setOverlapsize(2048);
        config.setBuffersize(512);

        var result = new ValidClamdBufferSizeValidator().isValid(config, null);

        assertTrue(result);
    }

    @Test
    void bufferSizeIsTooLarge() {
        // chunk size should be at least twice as big as overlap size
        // overlap size should be at least twice as big as buffer size
        var config = new ClamdConfig();
        config.setChunksize(1024*1024);
        config.setOverlapsize(2048);
        config.setBuffersize(1500);

        var result = new ValidClamdBufferSizeValidator().isValid(config, null);

        assertFalse(result);
    }

    @Test
    void overlapSizeIsTooLarge() {
        // chunk size should be at least twice as big as overlap size
        // overlap size should be at least twice as big as buffer size
        var config = new ClamdConfig();
        config.setChunksize(1024*1024);
        config.setOverlapsize(1024*1023);
        config.setBuffersize(1500);

        var result = new ValidClamdBufferSizeValidator().isValid(config, null);

        assertFalse(result);
    }

    @Test
    void overlapSizeIsBiggerThanChunksize() {
        // chunk size should be at least twice as big as overlap size
        // overlap size should be at least twice as big as buffer size
        var config = new ClamdConfig();
        config.setChunksize(1024*1024);
        config.setOverlapsize(1024*2048);
        config.setBuffersize(1500);

        var result = new ValidClamdBufferSizeValidator().isValid(config, null);

        assertFalse(result);
    }
}