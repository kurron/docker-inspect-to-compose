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
import org.kurron.traits.GenerationAbility
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.AsyncRestOperations
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification

/**
 * Integration test for the RestInboundGateway object.
 **/
class RestInboundGatewayIntegrationTest extends Specification implements GenerationAbility {

    @Autowired
    AsyncRestOperations theTemplate

    @Value( '${local.server.port}' )
    int port

    def requestPayload = ['docker-uri': UriComponentsBuilder.newInstance().scheme( 'http' ).host( '192.168.255.18' ).port( 2375 ).path( '/' ).build().toUri().toString()]
    def retagPayload = ['docker-uri': UriComponentsBuilder.newInstance().scheme( 'http' ).host( 'localhost' ).port( 2375 ).path( '/' ).build().toUri().toString()]

    def 'exercise retag'() {

        given: 'a valid environment'
        assert theTemplate
        assert port

        def builder = new JsonBuilder( retagPayload )
        def payload = builder.toPrettyString()

        and: 'the POST request is made'
        def uri = UriComponentsBuilder.newInstance().scheme( 'http' ).host( 'localhost' ).port( port ).path( '/retag' ).build().toUri()
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
        def json = new JsonSlurper().parseText( response.body )
        def tagCommands = json['image-tags'].collect {
            def oldTag = it as String
            def newTag = oldTag.replace( 'registry.transparent.com', 'docker-registry-load-balancer-385982309.us-west-2.elb.amazonaws.com/docker' )
            "docker tag ${oldTag} ${newTag} "
        }
        def pushCommands = json['image-tags'].collect {
            def oldTag = it as String
            def newTag = oldTag.replace( 'registry.transparent.com', 'docker-registry-load-balancer-385982309.us-west-2.elb.amazonaws.com/docker' )
            "docker push ${newTag}"
        }
        def file = new File( 'retag-and-push.sh' )
        file.withWriter('UTF-8') { writer ->
            writer.write( '#!/bin/bash' )
            writer.write( System.getProperty( 'line.separator' ) )
            writer.write( System.getProperty( 'line.separator' ) )
            tagCommands.each { line ->
                writer.write( line )
                writer.write( System.getProperty( 'line.separator' ) )
            }
            pushCommands.each { line ->
                writer.write( line )
                writer.write( System.getProperty( 'line.separator' ) )
            }
        }
        true
    }

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
/*
            def yaml =
"""
${name}:
    container_name: ${name}
    image: ${it['image']}
    restart: always
    net: bridge
    log_driver: "syslog"
    log_opt:
        syslog-facility: daemon
        syslog-tag: "${name}"
"""
*/
            def yaml =
                    """
${name}:
    container_name: ${name}
    image: ${it['image']}
    restart: always
    net: bridge
    log_driver: "json-file"
"""
            def yamlBuilder = new StringBuilder( yaml )
            if ( it['port-mappings'] ) {
                def mappings = it['port-mappings'].collect { key, value ->
                    // not sure what this isn't working right so go old school
                    new StringBuilder().append( '- "' ).append( key ).append( ':' ).append( value ).append( '" ' ).toString()
                }
                yamlBuilder.append( '    ports: ' ).append( System.getProperty( 'line.separator' ) )
                mappings.each { mapping -> yamlBuilder.append( '        ' ).append( mapping ).append( System.getProperty( 'line.separator' ) ) }
            }

            if ( it['mount-points'] ) {
                def mounts = it['mount-points'].collect { key, value ->
                    // not sure what this isn't working right so go old school
                    new StringBuilder().append( '- ' ).append( key ).append( ':' ).append( value ).append( ' ' ).toString()
                }
                yamlBuilder.append( '    volumes: ' ).append( System.getProperty( 'line.separator' ) )
                mounts.each { mapping -> yamlBuilder.append( '        ' ).append( mapping ).append( System.getProperty( 'line.separator' ) ) }
            }

            if ( it['environment'] ) {
                yamlBuilder.append( '    environment: ' ).append( System.getProperty( 'line.separator' ) )
                it['environment'].each { mapping -> yamlBuilder.append( '        - ' ).append( mapping ).append( System.getProperty( 'line.separator' ) ) }
            }

            if ( it['hosts'] ) {
                yamlBuilder.append( '    extra_hosts: ' ).append( System.getProperty( 'line.separator' ) )
                it['hosts'].each { mapping -> yamlBuilder.append( '        - ' ).append( '"' ).append( mapping ).append( '"' ).append( System.getProperty( 'line.separator' ) ) }
            }

            yamlBuilder.append( System.getProperty( 'line.separator' ) ).toString()
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
