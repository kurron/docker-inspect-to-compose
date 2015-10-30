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

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Application specific properties. This can be injected into beans to share values.
 */
@ConfigurationProperties( value = 'example', ignoreUnknownFields = false )
class ApplicationProperties {

    /**
     * The exchange to publish to.
     */
    String exchange

    /**
     * The port the MongoDB service is listening on.
     */
    int mongodbServicePort

    /**
     * The port the Redis service is listening on.
     */
    int redisServicePort

    /**
     * The port the MySQL service is listening on.
     */
    int mySqlServicePort

    /**
     * The port the PostgreSQL service is listening on.
     */
    int postgreSqlServicePort

    /**
     * Flag controlling whether or not the correlation id is required.
     */
    boolean requireCorrelationId
}
