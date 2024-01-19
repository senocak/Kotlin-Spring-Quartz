package com.github.senocak.quartz

import com.github.senocak.util.AppConstants.logger
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.SchedulerException
import org.quartz.SchedulerListener
import org.quartz.Trigger
import org.quartz.TriggerKey
import org.slf4j.Logger
import org.springframework.stereotype.Component

@Component
class SchedulersListener: SchedulerListener {
    private val log: Logger by logger()

    override fun jobScheduled(trigger: Trigger): Unit = log.info("jobScheduled: $trigger")
    override fun jobUnscheduled(triggerKey: TriggerKey): Unit = log.info("jobUnscheduled: $triggerKey")
    override fun triggerFinalized(trigger: Trigger): Unit = log.info("triggerFinalized: $trigger")
    override fun triggerPaused(triggerKey: TriggerKey) : Unit = log.info("triggerPaused: $triggerKey")
    override fun triggersPaused(triggerGroup: String) : Unit = log.info("triggersPaused: $triggerGroup")
    override fun triggerResumed(triggerKey: TriggerKey) : Unit = log.info("triggerResumed: $triggerKey")
    override fun triggersResumed(triggerGroup: String) : Unit = log.info("triggersResumed: $triggerGroup")
    override fun jobAdded(jobDetail: JobDetail) : Unit = log.info("jobAdded: $jobDetail")
    override fun jobDeleted(jobKey: JobKey) : Unit = log.info("jobDeleted: $jobKey")
    override fun jobPaused(jobKey: JobKey) : Unit = log.info("jobPaused: $jobKey")
    override fun jobsPaused(jobGroup: String) : Unit = log.info("jobsPaused: $jobGroup")
    override fun jobResumed(jobKey: JobKey) : Unit = log.info("jobResumed: $jobKey")
    override fun jobsResumed(jobGroup: String) : Unit = log.info("jobsResumed: $jobGroup")
    override fun schedulerError(msg: String, cause: SchedulerException) : Unit = log.info("schedulerError: msg: $msg, cause: $cause")
    override fun schedulerInStandbyMode() : Unit = log.info("schedulerInStandbyMode")
    override fun schedulerStarted() : Unit = log.info("schedulerStarted")
    override fun schedulerStarting() : Unit = log.info("schedulerStarting")
    override fun schedulerShutdown() : Unit = log.info("schedulerShutdown")
    override fun schedulerShuttingdown() : Unit = log.info("schedulerShuttingdown")
    override fun schedulingDataCleared() : Unit = log.info("schedulingDataCleared")
}