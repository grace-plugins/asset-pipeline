/*
 * Copyright 2024 the original author or authors.
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
package asset.pipeline;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import asset.pipeline.grails.AssetProcessorService;
import asset.pipeline.grails.AssetResourceLocator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import asset.pipeline.grails.LinkGenerator;
import asset.pipeline.grails.CachingLinkGenerator;
import org.springframework.core.annotation.Order;

import grails.config.Config;
import grails.config.Settings;
import grails.core.GrailsApplication;
import grails.util.BuildSettings;
import grails.util.Environment;
import grails.web.mapping.UrlMappingsHolder;
import org.grails.core.io.ResourceLocator;
import org.grails.web.mapping.DefaultLinkGenerator;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Asset Pipeline
 *
 * @author Michael Yan
 * @since 6.1
 */
@AutoConfiguration
@AutoConfigureOrder
public class AssetPipelineAutoConfiguration {

    @Bean
    @Order(-20)
    @ConditionalOnMissingBean(grails.web.mapping.LinkGenerator.class)
    public DefaultLinkGenerator assetLinkGenerator(ObjectProvider<GrailsApplication> grailsApplication,
            ObjectProvider<UrlMappingsHolder> grailsUrlMappingsHolder, ObjectProvider<AssetProcessorService> assetProcessorService) {
        Config config = grailsApplication.getObject().getConfig();
        boolean isReloadEnabled = Environment.isDevelopmentMode() || Environment.getCurrent().isReloadEnabled();
        boolean cacheUrls = config.getProperty(Settings.WEB_LINK_GENERATOR_USE_CACHE, Boolean.class, !isReloadEnabled);
        String serverURL = config.getProperty(Settings.SERVER_URL);

        if (cacheUrls) {
            CachingLinkGenerator cachingLinkGenerator = new CachingLinkGenerator(serverURL);
            cachingLinkGenerator.setGrailsApplication(grailsApplication.getObject());
            cachingLinkGenerator.setUrlMappingsHolder(grailsUrlMappingsHolder.getObject());
            cachingLinkGenerator.setAssetProcessorService(assetProcessorService.getObject());
            return cachingLinkGenerator;
        }
        else {
            LinkGenerator linkGenerator = new LinkGenerator(serverURL);
            linkGenerator.setGrailsApplication(grailsApplication.getObject());
            linkGenerator.setAssetProcessorService(assetProcessorService.getObject());
            linkGenerator.setUrlMappingsHolder(grailsUrlMappingsHolder.getObject());
            return linkGenerator;
        }
    }

    @Bean
    @Order
    @ConditionalOnMissingBean(ResourceLocator.class)
    public AssetResourceLocator assetResourceLocator() throws IOException {
        AssetResourceLocator assetResourceLocator = new AssetResourceLocator();
        assetResourceLocator.setSearchLocations(List.of(BuildSettings.BASE_DIR.getCanonicalPath()));
        return assetResourceLocator;
    }

    @Bean
    @ConditionalOnMissingBean
    public AssetProcessorService assetProcessorService(ObjectProvider<GrailsApplication> grailsApplication, grails.web.mapping.LinkGenerator grailsLinkGenerator) {
        AssetProcessorService assetProcessorService = new AssetProcessorService(grailsApplication.getObject(), grailsLinkGenerator);
        return assetProcessorService;
    }

    @Bean
    @SuppressWarnings("rawtypes")
    public FilterRegistrationBean<AssetPipelineFilter> assetPipelineFilter(ObjectProvider<GrailsApplication> grailsApplication) {
        Config config = grailsApplication.getObject().getConfig();
        Map assetsConfig = config.getProperty("grails.assets", Map.class, new HashMap());
        String mapping = assetsConfig.containsKey("mapping") ? assetsConfig.get("mapping").toString() : "assets";

        AssetPipelineFilter filter = new AssetPipelineFilter();

        FilterRegistrationBean<AssetPipelineFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setUrlPatterns(List.of(String.format("/%s/*", mapping)));
        registration.setOrder(0);

        return registration;
    }

}
