package com.personalagent.core

/**
 * Memory stores are injected. The Android implementation will use Room and encrypted fields;
 * this interface deliberately has no filesystem or network capability.
 */
enum class MemoryKind { USER_PREFERENCE, EPISODIC_SUMMARY, TASK_FACT }

data class MemoryEntry(
    val id: String,
    val kind: MemoryKind,
    val content: String,
    val createdAtEpochMillis: Long,
    val sensitivity: Sensitivity,
    val sourceTaskId: TaskId?,
) {
    init {
        require(id.isNotBlank())
        require(content.isNotBlank() && content.length <= 2_000)
        require(createdAtEpochMillis >= 0)
    }
}

enum class Sensitivity { PUBLIC, PERSONAL, SENSITIVE }

interface AgentMemoryStore {
    suspend fun search(query: String, limit: Int = 5): List<MemoryEntry>
    suspend fun propose(entry: MemoryEntry): String
    suspend fun approve(proposalId: String): Boolean
    suspend fun delete(entryId: String): Boolean
    suspend fun clear(kind: MemoryKind? = null)
}

enum class SkillStepKind { LAUNCH_APP, OBSERVE, CLICK_NODE, SET_TEXT, WAIT_FOR_STATE }

data class SkillStep(
    val kind: SkillStepKind,
    val capability: CapabilityId,
    val parameters: Map<String, String>,
    val expected: Postcondition,
) {
    init {
        require(parameters.size <= 12)
        require(parameters.all { (key, value) ->
            key.matches(Regex("[a-z][a-zA-Z0-9_]{0,40}")) && value.length <= 500
        })
    }
}

data class SkillManifest(
    val id: String,
    val version: Int,
    val description: String,
    val requiredCapabilities: Set<CapabilityId>,
    val steps: List<SkillStep>,
    val approvedByUser: Boolean,
) {
    init {
        require(id.matches(Regex("[a-z0-9][a-z0-9._-]{2,63}")))
        require(version > 0)
        require(description.isNotBlank() && description.length <= 500)
        require(steps.size in 1..8)
    }
}

/** Skills are data, never Kotlin scripts, shell commands, reflection expressions or downloaded executables. */
class SkillValidator {
    fun validate(skill: SkillManifest, available: Set<CapabilityId>): List<String> = buildList {
        if (!skill.approvedByUser) add("Skill has not been approved by the user")
        val missing = skill.requiredCapabilities - available
        if (missing.isNotEmpty()) add("Unavailable capabilities: ${missing.joinToString { it.value }}")
        skill.steps.forEachIndexed { index, step ->
            if (step.capability !in skill.requiredCapabilities) {
                add("Step ${index + 1} uses an undeclared capability")
            }
            when (step.kind) {
                SkillStepKind.LAUNCH_APP -> {
                    if (step.parameters.keys != setOf("packageName")) add("Step ${index + 1}: invalid launch parameters")
                }
                SkillStepKind.CLICK_NODE -> {
                    if (step.parameters.keys != setOf("nodeId", "snapshotId")) add("Step ${index + 1}: invalid click parameters")
                }
                SkillStepKind.SET_TEXT -> {
                    if (step.parameters.keys != setOf("nodeId", "snapshotId", "text")) add("Step ${index + 1}: invalid text parameters")
                }
                SkillStepKind.OBSERVE -> if (step.parameters.isNotEmpty()) add("Step ${index + 1}: observe takes no parameters")
                SkillStepKind.WAIT_FOR_STATE -> {
                    if (step.parameters.keys != setOf("timeoutMillis") ||
                        (step.parameters["timeoutMillis"]?.toLongOrNull() ?: 0L) !in 1L..15_000L
                    ) add("Step ${index + 1}: wait must be bounded to 15 seconds")
                }
            }
        }
    }
}
