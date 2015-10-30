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
package org.kurron.example.rest.exception

import static org.kurron.example.rest.feedback.ExampleFeedbackContext.GENERIC_ERROR
import org.kurron.feedback.FeedbackAware
import org.kurron.feedback.FeedbackProvider
import org.kurron.feedback.NullFeedbackProvider
import org.kurron.feedback.exceptions.AbstractError
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Global handling for REST exceptions.
 */
@ControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler implements FeedbackAware {

    /**
     * The provider to use.
     */
    @Delegate
    private FeedbackProvider theFeedbackProvider = new NullFeedbackProvider()

    @Override
    FeedbackProvider getFeedbackProvider() {
        theFeedbackProvider
    }

    @Override
    void setFeedbackProvider( final FeedbackProvider aProvider ) {
        theFeedbackProvider = aProvider
    }

    @Override
    protected ResponseEntity<String> handleExceptionInternal( Exception e,
                                                              Object body,
                                                              HttpHeaders headers,
                                                              HttpStatus status,
                                                              WebRequest request ) {
        sendFeedback( GENERIC_ERROR, e.message )
        new ResponseEntity<String>( '{}', status )
    }

    /**
     * Handles errors thrown by the application itself.
     * @param e the error.
     * @return the constructed response entity, containing details about the error.
     */
    @ExceptionHandler( AbstractError )
    static ResponseEntity<String> handleApplicationException( AbstractError e ) {
        new ResponseEntity<String>( '{}', e.httpStatus )
    }


}
