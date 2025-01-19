/*
 * I18nAssetPipelineGrailsPlugin.groovy
 *
 * Copyright (c) 2014-2016, Daniel Ellermann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package asset.pipeline.i18n

import grails.plugins.Plugin
import groovy.transform.CompileStatic

@CompileStatic
class AssetPipelineI18nGrailsPlugin extends Plugin {

    def grailsVersion = '2023.0.0 > *'
    def title = 'Asset Pipeline Plugin to use I18n in JavaScript.'
    def author = 'Michael Yan'
    def authorEmail = 'rain@rainboyan.com'
    def description = 'An asset-pipeline plugin for client-side i18n. It generates JavaScript files from i18n resources for use in client-side code.'
    def documentation = 'https://github.com/grace-plugins/grace-asset-pipeline'
    def license = 'APACHE'
    def organization = [
        name: 'Grace Plugins',
        url: 'https://github.com/grace-plugins'
    ]
    def issueManagement = [
        system: 'GITHUB',
        url: 'https://github.com/grace-plugins/grace-asset-pipeline/issues'
    ]
    def scm = [url: 'https://github.com/grace-plugins/grace-asset-pipeline']
    def watchedResources = ['file:./app/i18n/*.properties']

}
