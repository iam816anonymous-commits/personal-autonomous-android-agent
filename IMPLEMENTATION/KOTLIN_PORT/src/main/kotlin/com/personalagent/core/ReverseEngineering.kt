package com.personalagent.core

/**
 * REA (Reverse Engineering Agent) studies only app behavior exposed through permitted surfaces:
 * accessibility semantics, visible screen metadata and user-authorized interaction probes.
 * It does not imply APK decompilation, private API access, credential extraction or bypasses.
 */
data class AppIdentity(
    val packageName: String,
    val versionName: String?,
    val versionCode: Long?,
    val sdkInt: Int,
) {
    init {
        require(packageName.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+")))
        require(sdkInt >= 1)
    }
}

data class AppScreenFingerprint(
    val packageName: String,
    val normalizedVisibleLabels: List<String>,
    val viewIds: List<String>,
    val interactiveNodeCount: Int,
) {
    init {
        require(packageName.isNotBlank())
        require(normalizedVisibleLabels.size <= 80)
        require(viewIds.size <= 120)
        require(interactiveNodeCount in 0..250)
    }
}

data class InteractionProbe(
    val name: String,
    val action: AgentAction,
    val expected: Postcondition,
    val observedBefore: SnapshotId,
    val observedAfter: SnapshotId?,
    val verification: VerificationResult?,
) {
    init { require(name.isNotBlank() && name.length <= 120) }
}

data class AppBehaviorModel(
    val app: AppIdentity,
    val observedScreens: List<AppScreenFingerprint>,
    val transitions: List<Pair<Int, Int>>,
    val probes: List<InteractionProbe>,
    val limitations: List<String>,
) {
    init {
        require(observedScreens.size <= 100)
        require(transitions.size <= 250)
        require(probes.size <= 100)
        require(limitations.size <= 50)
        require(transitions.all { (from, to) -> from in observedScreens.indices && to in observedScreens.indices })
    }
}

data class SkillProposal(
    val manifest: SkillManifest,
    val evidenceProbeNames: Set<String>,
    val appPackage: String,
    val appVersionName: String?,
    val confidence: Double,
) {
    init {
        require(appPackage.isNotBlank())
        require(confidence in 0.0..1.0)
        require(evidenceProbeNames.size <= 100)
    }
}

sealed interface SkillCertification {
    data class Certified(val proposal: SkillProposal, val evidenceDigest: String) : SkillCertification
    data class Rejected(val reasons: List<String>) : SkillCertification
}

/** Certifies only skills backed by successful, repeatable, postcondition-verified probes. */
class AppSkillCertifier(private val validator: SkillValidator) {
    fun certify(
        proposal: SkillProposal,
        behavior: AppBehaviorModel,
        availableCapabilities: Set<CapabilityId>,
        minimumConfidence: Double = 0.85,
    ): SkillCertification {
        val failures = mutableListOf<String>()
        if (proposal.appPackage != behavior.app.packageName) failures += "Skill package does not match inspected app"
        if (proposal.appVersionName != behavior.app.versionName) failures += "App version changed; re-inspection is required"
        if (proposal.confidence < minimumConfidence) failures += "Confidence is below certification threshold"
        if (proposal.evidenceProbeNames.isEmpty()) failures += "No interaction probe evidence was supplied"

        val verifiedProbeNames = behavior.probes.filter {
            it.verification is VerificationResult.Verified &&
                it.observedAfter != null &&
                it.observedAfter != it.observedBefore
        }.map { it.name }.toSet()

        val missingEvidence = proposal.evidenceProbeNames - verifiedProbeNames
        if (missingEvidence.isNotEmpty()) failures += "Unverified probe evidence: ${missingEvidence.joinToString()}"

        val skillErrors = validator.validate(proposal.manifest, availableCapabilities)
        failures += skillErrors
        if (failures.isNotEmpty()) return SkillCertification.Rejected(failures.distinct())

        val canonical = listOf(
            behavior.app.packageName,
            behavior.app.versionName.orEmpty(),
            proposal.manifest.id,
            proposal.manifest.version.toString(),
            proposal.evidenceProbeNames.sorted().joinToString(","),
            verifiedProbeNames.sorted().joinToString(","),
        ).joinToString("|")
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return SkillCertification.Certified(proposal, digest)
    }
}
