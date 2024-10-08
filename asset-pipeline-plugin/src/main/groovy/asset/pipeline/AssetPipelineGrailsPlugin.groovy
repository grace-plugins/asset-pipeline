/*
 * Copyright 2014-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package asset.pipeline

import grails.plugins.Plugin

class AssetPipelineGrailsPlugin extends Plugin {

    def grailsVersion   = "2023.0.0 > *"
    def title           = "Asset Pipeline Plugin"
    def author          = "David Estes"
    def authorEmail     = "destes@bcap.com"
    def description     = 'Asset-Pipeline is a plugin used for managing and processing static assets in Grace applications.'
    def documentation   = "https://github.com/grace-plugins/grace-asset-pipeline"
    def license         = "APACHE"
    def organization    = [ name: "Grace Plugins", url: "https://github.com/grace-plugins" ]
    def issueManagement = [ system: "GITHUB", url: "https://github.com/grace-plugins/grace-asset-pipeline/issues" ]
    def scm             = [ url: "https://github.com/grace-plugins/grace-asset-pipeline" ]
    def pluginExcludes  = [
        "app/assets/**",
        "test/dummy/**"
    ]
    def developers      = [ [id: 'rainboyan'], [name: 'Michael Yan'], [email: 'rain@rainboyan.com'] ]
    def loadAfter = ['url-mappings']

}
