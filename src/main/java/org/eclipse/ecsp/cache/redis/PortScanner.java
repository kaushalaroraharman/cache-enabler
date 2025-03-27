/*
 * *******************************************************************************
 *
 *  Copyright (c) 2023-24 Harman International
 *
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *
 *  you may not use this file except in compliance with the License.
 *
 *  You may obtain a copy of the License at
 *
 *
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *       
 *
 *  Unless required by applicable law or agreed to in writing, software
 *
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *
 *  limitations under the License.
 *
 *
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  *******************************************************************************
 */

package org.eclipse.ecsp.cache.redis;

import org.eclipse.ecsp.cache.exception.IgniteCacheException;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;

/**
 * This class is used to scan the available port.
 */
public class PortScanner {
    
    /** The Constant LOGGER. */
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(PortScanner.class);
    
    /** The Constant MAX_PORT. */
    private static final int MAX_PORT =  65535;

    /**
     * This method is used to get the available port.
     *
     * @param port port
     * @return available port
     */    
    public int getAvailablePort(int port) {
        while (true) {
            if (port > MAX_PORT) {
                throw new IgniteCacheException("Max port limit has been reached while creating the socket");
            }
            try (Socket s = SSLSocketFactory.getDefault().createSocket("localhost", port)) {
                LOGGER.debug("Port {} is not available", port);
            } catch (IOException e) {
                return port;
            }
            port = port + 1;
        }
    }
}
