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
package org.kurron.example.rest.feedback

import static org.kurron.feedback.Audience.DEVELOPMENT
import static org.kurron.feedback.Audience.QA
import static org.kurron.feedback.FeedbackLevel.ERROR
import static org.kurron.feedback.FeedbackLevel.INFO
import org.kurron.feedback.Audience
import org.kurron.feedback.FeedbackContext
import org.kurron.feedback.FeedbackLevel

/**
 * Message codes specific to this application.
 */
@SuppressWarnings( ['LineLength', 'SerializableClassMustDefineSerialVersionUID'] )
enum ExampleFeedbackContext implements FeedbackContext {

    GENERIC_ERROR( 2000, 'The following error has occurred and was caught by the global error handler: {}', ERROR, QA ),
    REDIS_STORE_INFO( 2001, 'Storing a {} byte payload with a content type of {} in Redis for {} seconds with a key of {}', INFO, DEVELOPMENT ),
    REDIS_RETRIEVE_INFO( 2002, 'Retrieving payload from Redis with a key of {}', INFO, DEVELOPMENT ),
    REDIS_RESOURCE_NOT_FOUND( 2003, 'The resource with an id of {} was not found in the system', ERROR, QA ),
    PRECONDITION_FAILED( 2004, 'The required {} header was not found on an inbound REST request', ERROR, QA ),
    CONTENT_LENGTH_REQUIRED( 2005, 'The Content-Length was not set and is required', ERROR, QA ),
    PAYLOAD_TOO_LARGE( 2006, 'The payload size of {} Bytes exceeds the maximum permitted size of {} Megabytes', ERROR, QA ),
    MISSING_CORRELATION_ID( 2007, 'A correlation id was missing from a request, and an auto-generated id of {} will be used instead', FeedbackLevel.WARN, QA ),
    PROCESSING_REQUEST( 2008, 'Processing request {}', FeedbackLevel.INFO, DEVELOPMENT )

    /**
     * Unique context code for this instance.
     */
    private final int theCode

    /**
     * Message format string for this instance.
     */
    private final String theFormatString

    /**
     * Feedback level for this instance.
     */
    private final FeedbackLevel theFeedbackLevel

    /**
     * The audience for this instance.
     */
    private final Audience theAudience

    ExampleFeedbackContext( int aCode, String aFormatString, FeedbackLevel aFeedbackLevel, Audience anAudience ) {
        theCode = aCode
        theFormatString = aFormatString
        theFeedbackLevel = aFeedbackLevel
        theAudience = anAudience
    }

    @Override
    int getCode() {
        theCode
    }

    @Override
    String getFormatString() {
        theFormatString
    }

    @Override
    FeedbackLevel getFeedbackLevel() {
        theFeedbackLevel
    }

    @Override
    Audience getAudience() {
        theAudience
    }
}
