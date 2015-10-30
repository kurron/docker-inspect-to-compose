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
package org.kurron.example.rest

import static org.kurron.example.rest.feedback.ExampleFeedbackContext.MISSING_CORRELATION_ID
import static org.kurron.example.rest.feedback.ExampleFeedbackContext.PRECONDITION_FAILED
import org.kurron.feedback.AbstractFeedbackAware
import org.kurron.feedback.exceptions.PreconditionFailedError
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import org.kurron.example.rest.inbound.CustomHttpHeaders

/**
 * Intercepts each REST request and extracts the X-Correlation-Id header, which is added to the MDC logging context. If no header is
 * found, an error is thrown.
 */
@Component
class CorrelationIdHandlerInterceptor extends AbstractFeedbackAware implements HandlerInterceptor {

    /**
     * Provides currently active property values.
     */
    private final ApplicationProperties configuration

    /**
     * Correlation id key into the mapped diagnostic context.
     */
    public static final String CORRELATION_ID = 'correlation-id'

    @Autowired
    CorrelationIdHandlerInterceptor( final ApplicationProperties aConfiguration ) {
        configuration = aConfiguration
    }

    @Override
    boolean preHandle( final HttpServletRequest request, final HttpServletResponse response, final Object handler ) throws Exception {
        def correlationId = request.getHeader( CustomHttpHeaders.X_CORRELATION_ID )
        if ( !correlationId ) {
            if ( configuration.requireCorrelationId ) {
                feedbackProvider.sendFeedback( PRECONDITION_FAILED, CustomHttpHeaders.X_CORRELATION_ID )
                throw new PreconditionFailedError( PRECONDITION_FAILED, CustomHttpHeaders.X_CORRELATION_ID )
            } else {
                correlationId = UUID.randomUUID().toString()
                feedbackProvider.sendFeedback( MISSING_CORRELATION_ID, correlationId )
            }
        }
        MDC.put( CORRELATION_ID, correlationId )
        true
    }

    @Override
    void postHandle( HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView ) throws Exception {
        MDC.remove( CORRELATION_ID )
    }

    @Override
    void afterCompletion( HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex ) throws Exception { }
}

