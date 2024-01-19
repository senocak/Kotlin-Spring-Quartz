package com.github.senocak.quartz

import com.github.senocak.util.AppConstants.logger
import org.quartz.JobExecutionContext
import org.quartz.Trigger
import org.quartz.TriggerListener
import org.slf4j.Logger
import org.springframework.stereotype.Component

@Component
class TriggersListener: TriggerListener {
    private val log: Logger by logger()

    override fun getName(): String = "TriggersListener"

    override fun triggerFired(trigger: Trigger, context: JobExecutionContext) {
        log.info("[triggerFired] at ${trigger.startTime} :: jobKey : ${trigger.jobKey}")
    }

    override fun vetoJobExecution(trigger: Trigger, context: JobExecutionContext): Boolean =
        false
            .also { log.info("[vetoJobExecution]: $it") }

    override fun triggerMisfired(trigger: Trigger) {
        log.info("[triggerMisfired] at ${trigger.startTime} :: jobKey : ${trigger.jobKey}")
    }

    override fun triggerComplete(
        trigger: Trigger,
        context: JobExecutionContext,
        triggerInstructionCode: Trigger.CompletedExecutionInstruction
    ) {
        log.info("[triggerComplete] at ${trigger.startTime} :: jobKey : ${trigger.jobKey}")
    }
}
