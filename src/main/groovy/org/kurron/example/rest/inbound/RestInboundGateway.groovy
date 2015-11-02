/*
 * Copyright (c) 2015. Ronald D. Kurr kurr@jvmguy.com
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
package org.kurron.example.rest.inbound

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.web.bind.annotation.RequestMethod.POST
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import java.util.concurrent.ThreadLocalRandom
import org.kurron.example.rest.ApplicationProperties
import org.kurron.example.rest.feedback.ExampleFeedbackContext
import org.kurron.feedback.AbstractFeedbackAware
import org.kurron.stereotype.InboundRestGateway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.CounterService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder

/**
 * Handles inbound REST requests.
 */
@InboundRestGateway
@RequestMapping( value = '/' )
class RestInboundGateway extends AbstractFeedbackAware {

    /**
     * Provides currently active property values.
     */
    private final ApplicationProperties configuration

    /**
     * Used to track counts.
     */
    private final CounterService counterService

    /**
     * Manages REST interactions.
     **/
    private final RestOperations theTemplate

    /**
     * Mapping of service names to their service endpoints
     **/
    private final Map<String,URI> serviceToUriMap = [default: UriComponentsBuilder.newInstance().scheme( 'http' ).host( 'google.com' ).path( '/' ).build().toUri()]

    @Autowired
    RestInboundGateway( final ApplicationProperties aConfiguration,
                        final CounterService aCounterService,
                        final RestOperations aTemplate ) {
        configuration = aConfiguration
        counterService = aCounterService
        theTemplate = aTemplate
    }

    @RequestMapping( method = POST, consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE] )
    ResponseEntity<String> post( @RequestBody final String request, @RequestHeader( 'X-Correlation-Id' ) Optional<String> correlationID ) {
        counterService.increment( 'gateway.post' )

        def loggingID = correlationID.orElse( Integer.toHexString( ThreadLocalRandom.newInstance().nextInt( 0, Integer.MAX_VALUE ) ) )
        feedbackProvider.sendFeedback( ExampleFeedbackContext.PROCESSING_REQUEST, loggingID )

        def parsed = new JsonSlurper().parseText( request )
        def dockerURI = parsed['docker-uri'] as String
        def containers = obtainContainerIDs( dockerURI )
        def containerInfo = containers.collect {
            inspectContainer( dockerURI, it )
        }

        def builder = new JsonBuilder( containerInfo )
        new ResponseEntity<String>( builder.toPrettyString(), HttpStatus.OK )
    }

    private List<String> obtainContainerIDs( String dockerURI ) {

        def serviceURI = UriComponentsBuilder.fromHttpUrl( dockerURI ).path( '/containers/json' ).query( 'all=1' ) .build().toUri()
        ResponseEntity<String> response = theTemplate.getForEntity( serviceURI, String )
        def parsed = new JsonSlurper().parseText( response.body )
        parsed.collect { it['Id'] as String }
    }

    @CompileDynamic
    private Map<String,String> inspectContainer( String dockerURI, String containerID ) {

        def serviceURI = UriComponentsBuilder.fromHttpUrl( dockerURI ).path( '/containers/{containerID}/json' ).query( 'all=1' ) .buildAndExpand( containerID ).toUri()
        ResponseEntity<String> response = theTemplate.getForEntity( serviceURI, String )
        def parsed = new JsonSlurper().parseText( response.body )
        Map<String,String> interesting = [:]
        interesting['name'] = parsed['Name'] as String
        interesting['image'] = parsed['Config']['Image'] as String
        interesting['networking'] = parsed['HostConfig']['NetworkMode'] as String
        def portKeys = parsed['Config']['ExposedPorts'].collect { key, value -> key }
        interesting['port-mappings'] = portKeys.collectEntries {
            def port = parsed['HostConfig']['PortBindings'][it] ? parsed['HostConfig']['PortBindings'][it]['HostPort'].first() : '-'
            // creates a mapping between from the container to the host side
            [(it): port]
        }
        interesting['mount-points'] = parsed['Mounts'].collectEntries {
            // creates a mapping from container side to host side
            [(it['Destination']): it['Source']]
        }
        interesting['environment'] = parsed['Config']['Env']

        interesting
    }
}
