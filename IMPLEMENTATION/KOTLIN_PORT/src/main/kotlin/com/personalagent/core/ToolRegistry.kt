package com.personalagent.core

/**
 * Explicit registration is intentional. Avoid global/import-time self-registration because
 * availability must be scoped to the active device profile and user-enabled services.
 */
data class ToolDescriptor(
    val id: CapabilityId,
    val description: String,
    val riskCeiling: RiskLevel,
    val supportedActions: Set<KClassName>,
) {
    init {
        require(description.isNotBlank() && description.length <= 500)
        require(supportedActions.isNotEmpty())
    }
}

/** Stable action identifiers exposed to model/tool schemas; not reflection class names. */
@JvmInline
value class KClassName(val value: String) {
    init { require(value.matches(Regex("[a-z][a-z0-9_.-]{1,80}"))) }
}

data class CapabilityStatus(
    val id: CapabilityId,
    val hardwarePresent: Boolean?,
    val apiAvailable: Boolean,
    val permissionGranted: Boolean,
    val runtimeEnabled: Boolean,
    val verifiedAtElapsedMillis: Long?,
    val unavailableReason: String? = null,
) {
    val usable: Boolean
        get() = apiAvailable && permissionGranted && runtimeEnabled &&
            (hardwarePresent != false) && unavailableReason == null
}

class ToolRegistry private constructor(
    private val tools: Map<CapabilityId, ToolDescriptor>,
) {
    fun descriptor(id: CapabilityId): ToolDescriptor? = tools[id]

    fun availableTools(statuses: Map<CapabilityId, CapabilityStatus>): List<ToolDescriptor> =
        tools.values.filter { descriptor ->
            statuses[descriptor.id]?.let { it.id == descriptor.id && it.usable } == true
        }.sortedBy { it.id.value }

    fun requireAvailable(id: CapabilityId, statuses: Map<CapabilityId, CapabilityStatus>): ToolDescriptor {
        val descriptor = tools[id] ?: error("Unknown capability: ${id.value}")
        val status = statuses[id] ?: error("Capability has not been discovered: ${id.value}")
        require(status.id == id) { "Capability status identity mismatch" }
        require(status.usable) { status.unavailableReason ?: "Capability is not currently usable" }
        return descriptor
    }

    class Builder {
        private val entries = linkedMapOf<CapabilityId, ToolDescriptor>()
        private var built = false

        fun register(descriptor: ToolDescriptor): Builder = apply {
            check(!built) { "Registry is already frozen" }
            require(entries.putIfAbsent(descriptor.id, descriptor) == null) {
                "Duplicate capability registration: ${descriptor.id.value}"
            }
        }

        fun build(): ToolRegistry {
            check(!built) { "Registry builder may only be consumed once" }
            built = true
            return ToolRegistry(entries.toMap())
        }
    }
}
