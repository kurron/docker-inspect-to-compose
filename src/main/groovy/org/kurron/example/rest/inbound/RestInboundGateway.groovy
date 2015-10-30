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

import static java.nio.charset.StandardCharsets.UTF_8
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.web.bind.annotation.RequestMethod.POST
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.util.concurrent.ThreadLocalRandom
import org.kurron.example.rest.ApplicationProperties
import org.kurron.example.rest.feedback.ExampleFeedbackContext
import org.kurron.feedback.AbstractFeedbackAware
import org.kurron.stereotype.InboundRestGateway
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.MessageDeliveryMode
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.core.MessagePropertiesBuilder
import org.springframework.amqp.rabbit.core.RabbitOperations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.CounterService
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
     * Handles RabbitMQ interactions.
     **/
    private final RabbitOperations rabbitTemplate

    /**
     * Mapping of service names to their service endpoints
     **/
    private final Map<String,URI> serviceToUriMap = [default: UriComponentsBuilder.newInstance().scheme( 'http' ).host( 'google.com' ).path( '/' ).build().toUri()]

    @Autowired
    RestInboundGateway( final ApplicationProperties aConfiguration,
                        final CounterService aCounterService,
                        final RestOperations aTemplate,
                        final RabbitOperations aRabbitTemplate ) {
        configuration = aConfiguration
        counterService = aCounterService
        theTemplate = aTemplate
        rabbitTemplate = aRabbitTemplate
    }

    @RequestMapping( method = POST, consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE] )
    ResponseEntity<String> post( @RequestBody final String request, @RequestHeader( 'X-Correlation-Id' ) Optional<String> correlationID ) {
        counterService.increment( 'gateway.post' )

        def loggingID = correlationID.orElse( Integer.toHexString( ThreadLocalRandom.newInstance().nextInt( 0, Integer.MAX_VALUE ) ) )
        feedbackProvider.sendFeedback( ExampleFeedbackContext.PROCESSING_REQUEST, loggingID )

        // 01 - list all containers -- running and not
        // 02 - extract the container ids
        // 03 - for each container, inspect them

        def parsed = new JsonSlurper().parseText( request )
        def results = parsed.collect { Map<String,String> serviceActions ->
            def service = serviceActions.entrySet().first().key
            def action = serviceActions.entrySet().first().value
            def status = callService( service, action, loggingID )
            rabbitTemplate.send( newMessage( action, loggingID ) )
            [service: service, command: action, status: status]
        }
        def builder = new JsonBuilder( results )
        def resultingStatus = results.collect { entry -> entry['status'] } as List<HttpStatus>
        def downStreamStatus = resultingStatus.every { HttpStatus status -> status.is2xxSuccessful() } ? HttpStatus.OK : HttpStatus.BAD_GATEWAY
        new ResponseEntity<String>( builder.toPrettyString(), downStreamStatus )
    }

    HttpStatus callService( String service, String action, String correlationID ) {
        def builder = new JsonBuilder( ['command': action] )
        def command = builder.toPrettyString()

        def headers = new HttpHeaders()
        headers.setContentType( MediaType.APPLICATION_JSON )
        headers.set( 'X-Correlation-Id', correlationID )

        HttpStatus status
        try {
            HttpEntity<String> requestEntity = new HttpEntity<>( command, headers )
            ResponseEntity<Void> response = theTemplate.postForEntity( toEndPoint( service ), requestEntity, Void )
            status = response.statusCode
        }
        catch( Exception ignored ) {
            status = HttpStatus.BAD_GATEWAY
        }
        status
    }

    private URI toEndPoint( String service ) {
        serviceToUriMap[(service)] ?: serviceToUriMap['mongodb']
    }


    private static MessageProperties newProperties( String correlationID ) {
        MessagePropertiesBuilder.newInstance().setAppId( 'monitor-api-gateway' )
                .setContentType( 'text/plain' )
                .setMessageId( UUID.randomUUID().toString() )
                .setDeliveryMode( MessageDeliveryMode.NON_PERSISTENT )
                .setTimestamp( Calendar.instance.time )
                .setCorrelationId( correlationID.getBytes( UTF_8  ) )
                .build()
    }

    private static Message newMessage( String request, String correlationID ) {
        def properties = newProperties( correlationID )
        MessageBuilder.withBody( request.getBytes( UTF_8  ) )
                      .andProperties( properties )
                      .build()
    }
}
