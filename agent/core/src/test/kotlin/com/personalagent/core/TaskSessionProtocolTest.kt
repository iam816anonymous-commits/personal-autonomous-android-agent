package com.personalagent.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TaskSessionProtocolTest {
    private fun record(state: TaskSessionState = TaskSessionState.CREATED) = TaskSessionRecord(
        taskId = TaskId("task-session-1"),
        goal = "Open my notes app",
        inputModality = TaskInputModality.TEXT,
        state = state,
        createdAtEpochMillis = 100,
        updatedAtEpochMillis = 100,
    )

    @Test fun taskMustFollowLegalLifecycle() {
        val running = assertIs<TaskSessionDecision.Accept>(
            TaskSessionProtocol.transition(record(), TaskSessionState.RUNNING, 110),
        ).record
        assertEquals(TaskSessionState.RUNNING, running.state)
        assertIs<TaskSessionDecision.Reject>(
            TaskSessionProtocol.transition(running, TaskSessionState.CREATED, 120),
        )
    }

    @Test fun terminalStateRequiresReasonAndCannotBeReopened() {
        val running = record(TaskSessionState.RUNNING)
        assertIs<TaskSessionDecision.Reject>(
            TaskSessionProtocol.transition(running, TaskSessionState.COMPLETED, 120),
        )
        val completed = assertIs<TaskSessionDecision.Accept>(
            TaskSessionProtocol.transition(running, TaskSessionState.COMPLETED, 120, "Goal verified"),
        ).record
        assertIs<TaskSessionDecision.Reject>(
            TaskSessionProtocol.transition(completed, TaskSessionState.RUNNING, 130),
        )
    }

    @Test fun interruptedRunningTaskRequiresReconciliation() {
        val interrupted = assertIs<TaskSessionDecision.Accept>(
            TaskSessionProtocol.interrupted(record(TaskSessionState.RUNNING), 200),
        ).record
        assertEquals(TaskSessionState.RECOVERY_REQUIRED, interrupted.state)
        assertEquals("App process restarted; task requires state reconciliation", interrupted.terminalReason)
    }

    @Test fun approvalPromptInterruptedByProcessDeathRequiresRecovery() {
        val interrupted = assertIs<TaskSessionDecision.Accept>(
            TaskSessionProtocol.interrupted(record(TaskSessionState.AWAITING_APPROVAL), 200),
        ).record
        assertEquals(TaskSessionState.RECOVERY_REQUIRED, interrupted.state)
    }

    @Test fun recoveryNeverMovesClockBackwards() {
        val interrupted = assertIs<TaskSessionDecision.Accept>(
            TaskSessionProtocol.interrupted(record(TaskSessionState.RUNNING), 50),
        ).record
        assertEquals(100, interrupted.updatedAtEpochMillis)
    }
}
