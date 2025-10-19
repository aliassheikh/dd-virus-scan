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

import nl.knaw.dans.virusscan.config.ClamdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class ClamdServiceImpl implements ClamdService {
    private static final Logger log = LoggerFactory.getLogger(ClamdServiceImpl.class);
    private final ClamdConfig clamdConfig;

    public ClamdServiceImpl(ClamdConfig clamdConfig) {
        this.clamdConfig = clamdConfig;
    }

    @Override
    public Set<String> scanStream(InputStream inputStream) throws IOException {
        return processStreamInBatches(inputStream);
    }

    @Override
    public String ping() throws IOException {
        try (var socket = getConnection()) {
            socket.getOutputStream().write("PING".getBytes(StandardCharsets.UTF_8));
            return new String(socket.getInputStream().readAllBytes());
        }
    }

    void writeChunk(OutputStream outputStream, byte[] buffer, int bufferSize) throws IOException {
        // create a 4 byte sequence indicating the size of the payload (in network byte order)
        var header = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(bufferSize).array();

        //log.debug("Writing buffer size header (value is {}) to output stream", bufferSize);
        // write the size of the payload
        outputStream.write(header);
        outputStream.write(buffer, 0, bufferSize);
        outputStream.flush();
    }

    Socket getConnection() throws IOException {
        return new Socket(clamdConfig.getHost(), clamdConfig.getPort());
    }

    Set<String> processStreamInBatches(InputStream inputStream) throws IOException {

        var overlapBuffer = new byte[0];
        var bytesWritten = 0;
        var buffer = new byte[clamdConfig.getBuffersize()];
        var bytesRead = inputStream.read(buffer);
        var results = new HashSet<String>();

        while (bytesRead >= 0) {
            try (var socket = getConnection();
                var outputStream = new BufferedOutputStream(socket.getOutputStream());
                var socketInputStream = socket.getInputStream()) {

                log.debug("Writing zINSTREAM header for batch");
                outputStream.write("zINSTREAM\0".getBytes(StandardCharsets.UTF_8));
                outputStream.flush();

                if (overlapBuffer.length > 0) {
                    writeChunk(outputStream, overlapBuffer, overlapBuffer.length);
                    bytesWritten += overlapBuffer.length;
                }

                var overlapBufferOutputStream = new ByteArrayOutputStream();

                var bufferThreshold = clamdConfig.getChunksize() - clamdConfig.getOverlapsize();
                var restartThreshold = clamdConfig.getChunksize();

                log.debug("Buffer threshold is {}, restart threshold is {}", bufferThreshold, restartThreshold);

                while (bytesRead >= 0) {
                    writeChunk(outputStream, buffer, bytesRead);

                    // check if the virus scan daemon already has something to say
                    if (socketInputStream.available() > 0) {
                        throw new IOException("Error reply from server: " + new String(socketInputStream.readAllBytes()));
                    }

                    bytesWritten += bytesRead;

                    if (bytesWritten >= bufferThreshold) {
                        var start = Math.max(0, bufferThreshold - bytesWritten + bytesRead);

                        for (var i = start; i < bytesRead; ++i) {
                            overlapBufferOutputStream.write(buffer[i]);
                        }
                        // do math to get the correct amount of bytes to copy
                        // start is max(0, threshold - bytesWritten + bytesRead)
                        // say the threshold is 100
                        // we have written 110 bytes in total now
                        // and the last batch we wrote 40 bytes
                        // we get 100 - 110 + 40 = 30
                    }

                    bytesRead = inputStream.read(buffer);

                    if (bytesWritten >= restartThreshold) {
                        break;
                    }
                }

                log.debug("Total payload written (size was {}), sending final bytes", bytesWritten);
                // last payload should be 0 length to indicate we are done
                outputStream.write(new byte[] { 0, 0, 0, 0 });
                outputStream.flush();

                overlapBuffer = overlapBufferOutputStream.toByteArray();
                bytesWritten = 0;

                var result = new String(socketInputStream.readAllBytes());
                log.debug("Completed batch; intermediate result for virus scan: '{}'; overlap buffer size is {}", result, overlapBuffer.length);
                results.add(result);
            }
        }

        return results;
    }
}
