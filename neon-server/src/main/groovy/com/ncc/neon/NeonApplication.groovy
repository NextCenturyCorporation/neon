package com.ncc.neon

import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.wadl.WadlFeature

class NeonApplication extends ResourceConfig {
	NeonApplication() {
		packages("com.ncc.neon.services")
		register(WadlFeature) // Probably don't need this? Because it's not solving the wadl issue.
	}
}