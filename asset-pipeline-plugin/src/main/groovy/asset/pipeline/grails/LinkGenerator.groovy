package asset.pipeline.grails

import groovy.transform.CompileStatic
import groovy.util.logging.Commons

import grails.core.GrailsApplication
import org.grails.web.mapping.DefaultLinkGenerator

@Commons
@CompileStatic
class LinkGenerator extends DefaultLinkGenerator {
	private GrailsApplication grailsApplication
	private AssetProcessorService assetProcessorService

	LinkGenerator(String serverUrl) {
		super(serverUrl)
	}

	void setGrailsApplication(GrailsApplication grailsApplication) {
		this.grailsApplication = grailsApplication
	}

	void setAssetProcessorService(AssetProcessorService assetProcessorService) {
		this.assetProcessorService = assetProcessorService
	}

	@Override
	String resource(final Map attrs) {
		asset(attrs) ?: super.resource(attrs)
	}

	/**
	 * Finds an Asset from the asset-pipeline based on the file attribute.
	 * @param attrs [file]
	 */
	String asset(final Map attrs) {
		assetProcessorService.asset(attrs, this)
	}

	@Override
	String makeServerURL() {
		assetProcessorService.makeServerURL(this)
	}
}
