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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import groovy.util.logging.Slf4j
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner

/**
 * Initialization logic, like pre-warming a cache, goes here.  We'll be using it to interrogate the Docker daemon.
 **/
@Slf4j
class RunAtStartUp implements ApplicationRunner {

    private ContainerCollector containerCollector

    RunAtStartUp( final ContainerCollector aContainerCollector ) {
        containerCollector = aContainerCollector
    }

    @Override
    void run( final ApplicationArguments arguments ) {

        def rawArguments = arguments.sourceArgs
        Optional.ofNullable( arguments.containsOption( 'one' ) ).map( { value -> println value } )
        arguments.getOptionValues( 'one' )
        def justSwitches = arguments.nonOptionArgs
        def optionNames = arguments.optionNames
        log.debug( 'Command-line arguments are: ', arguments.sourceArgs.join( ',' ) )


        def metaData = containerCollector.collectMetaData()
        def outputFileName = Optional.ofNullable( arguments.getOptionValues( 'output' ) ).orElse( ['docker-compose.yml'] )
        def outputFile = new File( outputFileName.first() )
        outputFile.withWriter( 'UTF-8' ) { writer ->
            def mapper = new ObjectMapper( new YAMLFactory() )
            mapper.writeValue( writer, metaData )
        }
        'foo'
    }
}