package com.github.senocak.quartz

import com.github.senocak.util.AppConstants.logger
import java.time.temporal.ChronoUnit
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobListener
import org.slf4j.Logger
import org.springframework.stereotype.Component

@Component
class JobsListener : JobListener {
    private val log: Logger by logger()

    override fun getName(): String = "JobsListener"
    override fun jobToBeExecuted(context: JobExecutionContext): Unit =
        log.info("[jobToBeExecuted] : ${context.jobDetail.key}")
    override fun jobExecutionVetoed(context: JobExecutionContext): Unit =
        log.info("[jobExecutionVetoed] : ${context.jobDetail.key}")
    override fun jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException?): Unit =
        log.info("[jobWasExecuted] : ${context.jobDetail.key}")
}

