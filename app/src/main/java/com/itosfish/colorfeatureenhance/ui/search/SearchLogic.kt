package com.itosfish.colorfeatureenhance.ui.search

import android.content.Context
import com.itosfish.colorfeatureenhance.FeatureMode
import com.itosfish.colorfeatureenhance.data.model.AppFeatureMappings
import com.itosfish.colorfeatureenhance.data.model.FeatureGroup
import com.itosfish.colorfeatureenhance.data.model.OplusFeatureMappings

object SearchLogic {
    fun filterFeatureGroups(
        groups: List<FeatureGroup>,
        query: String,
        context: Context,
        currentMode: FeatureMode
    ): List<FeatureGroup> {
        if (query.isBlank()) return groups
        
        val normalizedQuery = query.trim().lowercase()
        
        return groups.filter { group ->
            group.features.any { feature ->
                val nameMatches = feature.name.lowercase().contains(normalizedQuery)
                
                val description = if (currentMode == FeatureMode.APP) {
                    AppFeatureMappings.getLocalizedDescription(context, feature.name)
                } else {
                    OplusFeatureMappings.getLocalizedDescription(context, feature.name)
                }
                
                val descriptionMatches = description.lowercase().contains(normalizedQuery)
                
                nameMatches || descriptionMatches
            }
        }
    }
}
