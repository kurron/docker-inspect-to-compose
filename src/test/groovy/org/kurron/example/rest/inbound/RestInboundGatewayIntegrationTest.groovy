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

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.util.concurrent.Future
import org.kurron.example.rest.Application
import org.kurron.traits.GenerationAbility
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.AsyncRestOperations
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification

/**
 * Integration test for the RestInboundGateway object.
 **/
@ContextConfiguration( loader = SpringApplicationContextLoader, classes = [Application] )
@WebIntegrationTest( randomPort = true )
class RestInboundGatewayIntegrationTest extends Specification implements GenerationAbility {

    @Autowired
    AsyncRestOperations theTemplate

    @Value( '${local.server.port}' )
    int port

    def requestPayload = ['docker-uri': UriComponentsBuilder.newInstance().scheme( 'http' ).host( '192.168.255.18' ).port( 2375 ).path( '/' ).build().toUri().toString()]

    def 'exercise happy path'() {

        given: 'a valid environment'
        assert theTemplate
        assert port

        def builder = new JsonBuilder( requestPayload )
        def payload = builder.toPrettyString()

        and: 'the POST request is made'
        def uri = UriComponentsBuilder.newInstance().scheme( 'http' ).host( 'localhost' ).port( port ).path( '/' ).build().toUri()
        def headers = new HttpHeaders()
        headers.setContentType( MediaType.APPLICATION_JSON )
        headers.add( 'X-Correlation-Id', randomHexString() )
        HttpEntity<String> request = new HttpEntity<>( payload, headers )
        Future<ResponseEntity<String>> future = theTemplate.postForEntity( uri, request, String )

        when: 'the answer comes back'
        def response = future.get()

        then: 'the endpoint returns with 200'
        response.statusCode == HttpStatus.OK

        and: 'the expected fields are present'
        def json = new JsonSlurper().parseText( response.body ) as List
        def compose = json.collect {
            def name = it['name'].substring( 1 )
            def yaml =
"""
${name}:
    image: ${it['image']}
    restart: always
    net: bridged
    log_driver: "syslog"
    log_opt:
#       syslog-address: udp://localhost:1234
        syslog-facility: daemon
        syslog-tag: "${name}"
"""
            def yamlBuilder = new StringBuilder( yaml )
            if ( it['port-mappings'] ) {
                def mappings = it['port-mappings'].collect { key, value ->
                    // not sure what this isn't working right so go old school
                    new StringBuilder().append( '- "' ).append( key ).append( ':' ).append( value ).append( '" ' ).toString()
                }
                def ports = 'ports: '
                mappings.each { mapping -> ports += mapping } // we'll clean it up by hand during the review of the file
                yamlBuilder.append( ports ).append( System.getProperty( 'line.separator' ) )
            }

            if ( it['mount-points'] ) {
                def mounts = it['mount-points'].collect { key, value ->
                    // not sure what this isn't working right so go old school
                    new StringBuilder().append( '- ' ).append( key ).append( ':' ).append( value ).append( ' ' ).toString()
                }
                def volumes = 'volumes: '
                mounts.each { mapping -> volumes += mapping } // we'll clean it up by hand during the review of the file
                yamlBuilder.append( volumes ).append( System.getProperty( 'line.separator' ) )
            }

            yamlBuilder.toString()
        }


        def file = new File( 'docker-compose-example.yml' )
        file.withWriter('UTF-8') { writer ->
            compose.each { line ->
                writer.write( line )
            }
        }
        true
    }
}
