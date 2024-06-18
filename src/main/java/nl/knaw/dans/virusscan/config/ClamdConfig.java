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
package nl.knaw.dans.virusscan.config;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ClamdConfig {

    @NotNull
    @Valid
    private String host;

    @NotNull
    @Min(1)
    @Max(65535)
    private int port;

    // The chunksize determines how much data we send to ClamAV to check in total.
    // This should be less than the maximum configured filesize in ClamAV
    // If a file is bigger than the chunksize, it is split up and sent as multiple files
    // A sensible value would be around 10MB
    // If it is bigger than the ClamAV value `StreamMaxLength` it will cause errors
    @Min(1024 * 1024)
    private int chunksize;

    // Because files are split up, it might be split right in the middle of a possible match.
    // To prevent this, a part of the previous payload is sent along to make sure this cannot happen.
    // A sensible value would be around 1MB. This value should be a lot less than the chunksize value
    @Min(1024)
    private int overlapsize;

    // Indicates how much to send per packet. This is represented as a byte array. This value
    // should be a lot less than the overlap size. A sensible value would be around 8KB
    @Min(128)
    private int buffersize;

    public int getBuffersize() {
        return buffersize;
    }

    public void setBuffersize(int buffersize) {
        this.buffersize = buffersize;
    }

    public int getOverlapsize() {
        return overlapsize;
    }

    public void setOverlapsize(int overlapsize) {
        this.overlapsize = overlapsize;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getChunksize() {
        return chunksize;
    }

    public void setChunksize(int chunksize) {
        this.chunksize = chunksize;
    }
}
