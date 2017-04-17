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

    Map<String, Map<String,String>> collectMetaData() {
        def containerMetaData = obtainContainerMetaData()
        def containerDetails = containerMetaData.collect { it['Id'] as String }.collect { String id ->
            obtainContainerDetails( id )
        }
        def containerToGeneralInformation = containerMetaData.collectEntries { it ->
            [(it['Id']): (it)]
        }
        containerDetails.inject( containerToGeneralInformation ) { accumulator, it ->
            def toAddTo = accumulator[it['Id']] as Map<String,String>
            it.collectEntries( toAddTo ) { key, value ->
                String transformedKey = "Detail${key}"
                [(transformedKey): value]
            }
            accumulator
        }
        containerToGeneralInformation
    }

    private List<Map<String,String>> obtainContainerMetaData()
    {
        def response = client.get( [path: '/containers/json' ] )
        if( !response.status['success'] )
        {
            throw new IllegalStateException( 'Unable to gather container meta-data' )
        }
        response.content as List<Map<String,String>>
    }

    private Map<String,String> obtainContainerDetails( String id )
    {
        def response = client.get( [path: "/containers/${id}/json"] )
        if( !response.status['success'] )
        {
            throw new IllegalStateException( 'Unable to gather container details' )
        }
        response.content as Map<String,String>
    }
}
