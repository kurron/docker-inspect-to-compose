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

/**
 * Converts the raw map of Docker containers into a Docker Compose v3 compatible format.
 **/
class MetaDataTransformer {

    Map<String, Map<String,String>> convert( Map<String, Map<String,String>> metaData ) {

        def services = metaData.collectEntries { key, value ->
            [(value['DetailName'].substring( 1 ) ): 'foo']
        }
        def compose = ['version': '3', 'services': services ]
        compose
    }
}
