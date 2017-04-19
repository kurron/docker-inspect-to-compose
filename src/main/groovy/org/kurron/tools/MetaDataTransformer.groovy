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

        //TODO: test when keys are missing
        /*
        DetailName -> service

        DetailHostConfig.Memory -> deploy:resource:limits:memory
        DetailHostConfig.MemoryReservation -> deploy:resource:reservations:memory
        DetailConfig.Entrypoint -> entrypoint
        DetailConfig.Env -> environment
        DetailHostConfig.ExtraHosts -> extra_hosts
        DetailConfig.Healthcheck.Test -> healthcheck:test
        DetailConfig.Healthcheck.Interval -> healthcheck:invterval
        DetailConfig.Healthcheck.Timeout -> healthcheck:timeout
        DetailConfig.Healthcheck.Retries -> healthcheck:retries
        DetailConfig.Image -> image
        DetailConfig.Labels -> labels ?
        DetailHostConfig.LogConfig.Type -> logging:driver
        DetailHostConfig.LogConfig.Config.foo -> logging:driver:options:foo
        Ports.PublicPort,Ports.PrivatePort,Ports.Type -> ports
        Mounts.Source,Mounts.Destination,Mounts.Mode -> volume

        * memory can be defaulted
        * health check can be defaulted
        * labels can be defaulted
        * logging can be defaulted
        *
         */
        def services = metaData.collectEntries { key, value ->
            def command = value['DetailPath'] + value['DetailArgs'].collect { it }
            def deploy = ['mode'          : 'replicated',
                          'replicas'      : '2',
                          'update_config' : ['parallelism': '2', 'delay': '10s'],
                          'restart_policy': ['condition': 'on-failure', 'delay': '5s', 'max_attempts': '3', 'window': '120s'],
                          'resources'     : ['limits': ['memory': '128m'], 'reservations': ['memory': '128m']]]
            def entrypoint = value['DetailConfig']['Entrypoint']
            def environment = value['DetailConfig']['Env']
            def hosts = ['logfaces:192.168.254.123', 'logfaces-boston:192.168.100.124']
            def healthcheck = ['test': ["curl", "--fail", "http://localhost/"], 'interval': '10s', 'timeout': '10s', 'retries': '3']
            def labels = ['com.transparent.generated.by': 'inspect-to-compose', 'com.transparent.generated.on': Calendar.instance.time as String]
            def ports = value['Ports'].collect { map ->
                def privatePort = map['PrivatePort']
                def publicPort = map['PublicPort']
                def protocol = map['Type']
                "${publicPort}:${privatePort}/${protocol}" as String
            }
            def logging = ['driver': 'json-file', 'options': ['max-size': '10m']]
            def volumes = value['Mounts'].collect { map ->
                def source = map['Source']
                def destination = map['Destination']
                def mode = map['Mode'] ?: 'rw'
                "${source}:${destination}:${mode}" as String
            }
            def service = ['command': command,
                           'deploy'     : deploy,
                           'environment': environment,
                           'extra_hosts': hosts,
                           'healthcheck': healthcheck,
                           'image'      : value['DetailConfig']['Image'],
                           'labels'     : labels,
                           'logging'    : logging,
                           'ports'      : ports,
                           'volumes'    : volumes]
            if ( entrypoint ) { service['entrypoint'] = entrypoint }
            [(value['DetailName'].substring( 1 ) ): service]
        }
        def compose = ['version': '3', 'services': services ]
        compose
    }
}
