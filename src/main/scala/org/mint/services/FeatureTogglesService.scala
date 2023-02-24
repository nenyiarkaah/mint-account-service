package org.mint.services

import org.mint.configs.FeatureToggles

class FeatureTogglesService(featureToggles: FeatureToggles) {
  val createSchemaIsEnabled = featureToggles.createSchema
}
