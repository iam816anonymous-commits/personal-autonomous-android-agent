package com.personalagent.core

/**
 * A tool created by the agent is a validated declarative composition of existing native actions.
 * It is not generated Kotlin, Java, shell, DEX, JNI or downloaded executable code.
 */
enum class ToolParameterType { PACKAGE_NAME, NODE_ID, TEXT, BOUNDED_DURATION }

data class ToolParameter(
    val name: String,
    val type: ToolParameterType,
    val required: Boolean = true,
    val maxLength: Int = 500,
) {
    init {
        require(name.matches(Regex("[a-z][a-zA-Z0-9_]{0,40}")))
        require(maxLength in 1..4_000)
    }
}

data class ToolStepTemplate(
    val actionKind: KClassName,
    val capability: CapabilityId,
    val parameterBindings: Map<String, String>,
    val risk: RiskLevel,
) {
    init {
        require(parameterBindings.size <= 12)
        require(parameterBindings.all { (key, value) ->
            key.matches(Regex("[a-z][a-zA-Z0-9_]{0,40}") && value.matches(Regex("\\$[a-z][a-zA-Z0-9_]{0,40}"))
        })
    }
}

data class GeneratedToolDefinition(
    val id: String,
    val version: Int,
    val description: String,
    val parameters: List<ToolParameter>,
    val steps: List<ToolStepTemplate>,
    val requiredCapabilities: Set<CapabilityId>,
    val maximumRisk: RiskLevel,
    val createdFromSkillId: String?,
) {
    init {
        require(id.matches(Regex("[a-z0-9][a-z0-9._-]{2,63}")))
        require(version > 0)
        require(description.isNotBlank() && description.length <= 500)
        require(parameters.size <= 16)
        require(steps.size in 1..8)
        require(parameters.map { it.name }.distinct().size == parameters.size)
    }
}

data class ToolValidationContext(
    val approvedCapabilities: Set<CapabilityId>,
    val registeredActionKinds: Set<KClassName>,
    val maximumAllowedRisk: RiskLevel,
    val userApproved: Boolean,
)

sealed interface ToolFactoryResult {
    data class Created(val definition: GeneratedToolDefinition, val schemaDigest: String) : ToolFactoryResult
    data class Rejected(val reasons: List<String>) : ToolFactoryResult
}

class SelfToolFactory {
    fun create(definition: GeneratedToolDefinition, context: ToolValidationContext): ToolFactoryResult {
        val errors = mutableListOf<String>()
        if (!context.userApproved) errors += "Tool creation requires explicit user approval"
        val parameterNames = definition.parameters.map { it.name }.toSet()
        val missingCapabilities = definition.requiredCapabilities - context.approvedCapabilities
        if (missingCapabilities.isNotEmpty()) errors += "Capabilities not approved: ${missingCapabilities.joinToString { it.value }}"
        if (definition.maximumRisk.severity > context.maximumAllowedRisk.severity) errors += "Tool exceeds allowed risk ceiling"

        definition.steps.forEachIndexed { index, step ->
            if (step.actionKind !in context.registeredActionKinds) errors += "Step ${index + 1} uses an unregistered action"
            if (step.capability !in definition.requiredCapabilities) errors += "Step ${index + 1} uses an undeclared capability"
            if (step.risk.severity > definition.maximumRisk.severity) errors += "Step ${index + 1} exceeds the tool risk ceiling"
            step.parameterBindings.values.forEach { binding ->
                val parameter = binding.removePrefix("$")
                if (parameter !in parameterNames) errors += "Step ${index + 1} references unknown parameter '$parameter'"
            }
        }

        if (errors.isNotEmpty()) return ToolFactoryResult.Rejected(errors.distinct())
        val canonical = buildString {
            append(definition.id).append('|').append(definition.version).append('|')
            append(definition.description).append('|').append(definition.maximumRisk.name)
            definition.parameters.sortedBy { it.name }.forEach { append('|').append(it.name).append(':').append(it.type.name).append(':').append(it.required) }
            definition.steps.forEach { step ->
                append('|').append(step.actionKind.value).append(':').append(step.capability.value).append(':').append(step.risk.name)
                step.parameterBindings.toSortedMap().forEach { (key, value) -> append(':').append(key).append('=').append(value) }
            }
        }
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return ToolFactoryResult.Created(definition, digest)
    }
}
