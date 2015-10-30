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

package org.kurron.example.rest.health

import org.kurron.example.rest.ApplicationProperties
import org.kurron.feedback.AbstractFeedbackAware
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestOperations

/**
 * Base class for down stream service health checks.
 **/
abstract class AbstractHealthCheck extends AbstractFeedbackAware implements HealthIndicator {

    /**
     * Provides currently active property values.
     */
    protected final ApplicationProperties theConfiguration

    /**
     * Manages REST interactions.
     **/
    private final RestOperations theTemplate

    AbstractHealthCheck( final ApplicationProperties aConfiguration, final RestOperations aTemplate ) {
        theConfiguration = aConfiguration
        theTemplate = aTemplate
    }

    abstract URI healthURI()

    @Override
    Health health() {
        def status = callHealthEndpoint()
        status == HttpStatus.OK ? Health.up().withDetail( 'HTTP Status', status ).build() : Health.down().withDetail( 'HTTP Status', status ).build()
    }

    HttpStatus callHealthEndpoint() {
        HttpStatus status
        try {
            ResponseEntity<String> response = theTemplate.getForEntity( healthURI(), String )
            status = response.statusCode
        }
        catch( Exception ignored ) {
            status = HttpStatus.BAD_GATEWAY
        }
        status
    }
}
