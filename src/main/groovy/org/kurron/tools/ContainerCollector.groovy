/*
 * Copyright (c) 2017. Ronald D. Kurr kurr@jvmguy.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kurron.tools

import de.gesellix.docker.client.OkDockerClient
import groovy.util.logging.Slf4j

/**
 * This object will pull meta-data about all containers deployed to the specified Docker Engine.
 **/
@Slf4j
class ContainerCollector {

    /**
     * Knows how to interact with the Docker Engine.
     */
    private final client = new OkDockerClient()

    List<Map<String,String>> collectMetaData() {
        def result = client.get( [path: '/containers/json'] )
        def good = result.status['success']
        result.content as List<Map<String,String>>
    }
}
