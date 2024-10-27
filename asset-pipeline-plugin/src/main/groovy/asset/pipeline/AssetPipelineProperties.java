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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the asset pipeline.
 *
 * @author Michael Yan
 * @since 6.1.0
 */
@ConfigurationProperties(prefix = "grails.assets")
public class AssetPipelineProperties {

    /**
     * Whether to use manifest, default is true.
     */
    private boolean useManifest = true;

    /**
     * The asset path to use.
     */
    private String assetsPath = "app/assets";

    /**
     * Whether to enable bundling in all other environments.
     */
    private boolean bundle = true;

    /**
     * The external storage path to use.
     */
    private String storagePath;

    /**
     * The asset path to use by the url mapping and the taglib.
     */
    private String mapping = "assets";

    /**
     * The custom CDN asset URL for serving these assets.
     */
    private String url;

    public boolean isUseManifest() {
        return this.useManifest;
    }

    public void setUseManifest(boolean useManifest) {
        this.useManifest = useManifest;
    }

    public String getAssetsPath() {
        return this.assetsPath;
    }

    public void setAssetsPath(String assetsPath) {
        this.assetsPath = assetsPath;
    }

    public boolean isBundle() {
        return this.bundle;
    }

    public void setBundle(boolean bundle) {
        this.bundle = bundle;
    }

    public String getStoragePath() {
        return this.storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getMapping() {
        return this.mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
