package ireader.data.plugins

import ireader.domain.plugins.composition.PipelineRepository
import ireader.domain.plugins.composition.PluginPipelineDefinition
import ireader.domain.plugins.composition.PipelineStepConfig
import ireader.domain.plugins.composition.PipelineDataType
import data.PluginPipelineQueries
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Implementation of PipelineRepository using SQLDelight.
 */
class PipelineRepositoryImpl(
    private val queries: PluginPipelineQueries
) : PipelineRepository {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }
    
    override suspend fun getAllPipelines(): List<PluginPipelineDefinition> {
        return queries.selectAll().executeAsList().map { it.toDomain() }
    }
    
    override suspend fun getPipeline(id: String): PluginPipelineDefinition? {
        return queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }
    
    override suspend fun savePipeline(definition: PluginPipelineDefinition) {
        queries.insert(
            id = definition.id,
            name = definition.name,
            description = definition.description,
            steps_json = json.encodeToString(definition.steps),
            input_type = definition.inputType.name,
            output_type = definition.outputType.name,
            created_at = definition.createdAt,
            updated_at = definition.updatedAt,
            is_public = definition.isPublic,
            author_id = definition.authorId,
            tags = definition.tags.joinToString(",")
        )
    }
    
    override suspend fun deletePipeline(id: String) {
        queries.delete(id)
    }
    
    override suspend fun getPublicPipelines(): List<PluginPipelineDefinition> {
        return queries.selectPublic().executeAsList().map { it.toDomain() }
    }
    
    override suspend fun getPipelinesByAuthor(authorId: String): List<PluginPipelineDefinition> {
        return queries.selectByAuthor(authorId).executeAsList().map { it.toDomain() }
    }

    private fun data.Plugin_pipeline.toDomain(): PluginPipelineDefinition {
        return PluginPipelineDefinition(
            id = id,
            name = name,
            description = description,
            steps = json.decodeFromString(steps_json),
            inputType = PipelineDataType.valueOf(input_type),
            outputType = PipelineDataType.valueOf(output_type),
            createdAt = created_at,
            updatedAt = updated_at,
            isPublic = is_public,
            authorId = author_id,
            tags = if (tags.isBlank()) emptyList() else tags.split(",")
        )
    }
}
