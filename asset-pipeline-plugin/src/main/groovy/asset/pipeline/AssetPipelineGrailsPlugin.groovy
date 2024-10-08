/*
 * Copyright 2014-2024 the original author or authors.
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
package asset.pipeline

import asset.pipeline.fs.*

import grails.plugins.Plugin
import grails.util.BuildSettings
import org.grails.plugins.BinaryGrailsPlugin
import groovy.util.logging.Commons

@Commons
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

    void doWithApplicationContext() {
        //Register Plugin Paths
        def ctx = applicationContext
        def config = applicationContext.grailsApplication.config
        def assetsPath= config.getProperty('grails.assets.assetsPath', String, 'app/assets')
        AssetPipelineConfigHolder.registerResolver(new FileSystemAssetResolver('application', "${BuildSettings.BASE_DIR}/${assetsPath}"))

        try {
            ctx.pluginManager.getAllPlugins()?.each { plugin ->
                if(plugin instanceof BinaryGrailsPlugin) {
                    def projectDirectory = plugin.getProjectDirectory()
                    if(projectDirectory) {
                        String assetPath = new File(plugin.getProjectDirectory(), assetsPath).canonicalPath

                        AssetPipelineConfigHolder.registerResolver(new FileSystemAssetResolver(plugin.name,assetPath))
                    }
                }
            }
        } catch(ex) {
            log.warn("Error loading exploded plugins ${ex}",ex)
        }
        AssetPipelineConfigHolder.registerResolver(new ClasspathAssetResolver('classpath', 'META-INF/assets','META-INF/assets.list'))
        AssetPipelineConfigHolder.registerResolver(new ClasspathAssetResolver('classpath', 'META-INF/static'))
        AssetPipelineConfigHolder.registerResolver(new ClasspathAssetResolver('classpath', 'META-INF/resources'))
    }

    Closure doWithSpring() {{->
        def application = grailsApplication
        def config = application.config
        def assetsConfig = config.getProperty('grails.assets', Map, [:])

        def manifestProps = new Properties()
        def manifestFile

        try {
            manifestFile = applicationContext.getResource("assets/manifest.properties")
            if(!manifestFile.exists()) {
                manifestFile = applicationContext.getResource("classpath:assets/manifest.properties")
            }
        } catch(e) {
            if(application.warDeployed) {
                log.warn "Unable to find asset-pipeline manifest, etags will not be properly generated"
            }
        }

        def useManifest = assetsConfig.useManifest ?: true

        if(useManifest && manifestFile?.exists()) {
            try {
                manifestProps.load(manifestFile.inputStream)
                assetsConfig.manifest = manifestProps
                AssetPipelineConfigHolder.manifest = manifestProps
            } catch (ignored) {
                log.warn "Failed to load Manifest"
            }
        }

        if(assetsConfig instanceof org.grails.config.NavigableMap) {
            AssetPipelineConfigHolder.config = assetsConfig.toFlatConfig()
        } else {
            AssetPipelineConfigHolder.config = assetsConfig
        }

        if (BuildSettings.TARGET_DIR) {
            AssetPipelineConfigHolder.config.cacheLocation = new File((File) BuildSettings.TARGET_DIR, ".assetcache").canonicalPath
        }
    }}
}
