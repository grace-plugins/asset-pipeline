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

import asset.pipeline.AssetPipelineFilter
import asset.pipeline.grails.AssetProcessorService
import asset.pipeline.grails.LinkGenerator
import asset.pipeline.grails.CachingLinkGenerator
import asset.pipeline.grails.AssetResourceLocator
import asset.pipeline.grails.fs.*
import asset.pipeline.fs.*
import asset.pipeline.*
import grails.util.BuildSettings
import org.grails.plugins.BinaryGrailsPlugin
import groovy.util.logging.Commons
import org.springframework.util.ClassUtils

@Commons
class AssetPipelineGrailsPlugin extends grails.plugins.Plugin {
    def grailsVersion   = "2022.0.0 > *"
    def title           = "Asset Pipeline Plugin"
    def author          = "David Estes"
    def authorEmail     = "destes@bcap.com"
    def description     = 'The Asset-Pipeline is a plugin used for managing and processing static assets in Grace applications. Asset-Pipeline functions include processing and minification of both CSS and JavaScript files.'
    def documentation   = "https://github.com/grace-plugins/grace-asset-pipeline"
    def license         = "APACHE"
    def organization    = [ name: "Grace Plugins", url: "https://github.com/grace-plugins" ]
    def issueManagement = [ system: "GITHUB", url: "https://github.com/grace-plugins/grace-asset-pipeline/issues" ]
    def scm             = [ url: "https://github.com/grace-plugins/grace-asset-pipeline" ]
    def pluginExcludes  = [
        "grails-app/assets/**",
        "test/dummy/**"
    ]
    def developers      = [ [id: 'rainboyan'], [name: 'Michael Yan'], [email: 'rain@rainboyan.com'] ]
    def loadAfter = ['url-mappings']

    void doWithApplicationContext() {
        //Register Plugin Paths
        def ctx = applicationContext
        AssetPipelineConfigHolder.registerResolver(new FileSystemAssetResolver('application',"${BuildSettings.BASE_DIR}/grails-app/assets"))
        AssetPipelineConfigHolder.registerResolver(new FileSystemAssetResolver('applicationNew',"${BuildSettings.BASE_DIR}/app/assets"))

        try {
            ctx.pluginManager.getAllPlugins()?.each { plugin ->
                if(plugin instanceof BinaryGrailsPlugin) {
                    def projectDirectory = plugin.getProjectDirectory()
                    if(projectDirectory) {
                        String assetPath = new File(plugin.getProjectDirectory(),"grails-app/assets").canonicalPath
                        String assetPathNew = new File(plugin.getProjectDirectory(),"app/assets").canonicalPath

                        AssetPipelineConfigHolder.registerResolver(new FileSystemAssetResolver(plugin.name,assetPath))
                        AssetPipelineConfigHolder.registerResolver(new FileSystemAssetResolver(plugin.name + 'Assets',assetPathNew))
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
            } catch(e) {
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
        // Register Link Generator
        String serverURL = config?.getProperty('grails.serverURL', String, null)
        boolean cacheUrls = config?.getProperty('grails.web.linkGenerator.useCache', Boolean, true)

        assetProcessorService(AssetProcessorService)
        grailsLinkGenerator(cacheUrls ? CachingLinkGenerator : LinkGenerator, serverURL) { bean ->
            bean.autowire = true
        }

        assetResourceLocator(AssetResourceLocator) { bean ->
            bean.parent = "abstractGrailsResourceLocator"
        }

        def mapping = assetsConfig.containsKey('mapping') ? assetsConfig.mapping?.toString() : 'assets'

        ClassLoader classLoader = application.classLoader
        Class registrationBean = ClassUtils.isPresent("org.springframework.boot.web.servlet.FilterRegistrationBean", classLoader ) ?
                                    ClassUtils.forName("org.springframework.boot.web.servlet.FilterRegistrationBean", classLoader) :
                                    ClassUtils.forName("org.springframework.boot.context.embedded.FilterRegistrationBean", classLoader)
        assetPipelineFilter(registrationBean) {
            order = 0
            filter = new asset.pipeline.AssetPipelineFilter()
            if(!mapping) {
                urlPatterns = ["/*".toString()]
            } else {
                urlPatterns = ["/${mapping}/*".toString()]
            }
            
        }
    }}
}
