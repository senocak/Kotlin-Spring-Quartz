package com.github.senocak.quartz

import com.github.senocak.domain.dto.JobResponse
import com.github.senocak.domain.dto.SampleJobRequest
import com.github.senocak.domain.dto.SchedulerRequest
import com.github.senocak.util.AppConstants.logger
import java.text.ParseException
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.impl.matchers.GroupMatcher
import org.slf4j.Logger
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.CronTriggerFactoryBean
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean

@Configuration
class QuartzHandler(
    private val scheduler: Scheduler
){
    private val log: Logger by logger()

    val currentlyExecutingJobs: MutableList<JobExecutionContext>
        get() = scheduler.currentlyExecutingJobs

    @Throws(SchedulerException::class)
    fun <T: Job> addJob(job: Class<T>, request: SampleJobRequest, params: Map<String, Any>) {
        val jobDetail: JobDetail = buildJobDetail(job = job, jobName = request.name, group = request.group, jobDescription = request.description, params = params)
        val trigger: Trigger? = buildTrigger(name = request.name, group = request.group, startTime = request.startTime!!)
        if (trigger == null) {
            log.warn("Trigger is null for addJob. Returning...")
            return
        }
        registerJobInScheduler(jobDetail = jobDetail, trigger = trigger)
    }

    @Throws(SchedulerException::class)
    fun <T: Job> addCronJob(job: Class<T>, request: SchedulerRequest, params: Map<String, Any>) {
        val jobDetail: JobDetail = buildJobDetail(job = job, jobName = request.name, group = request.group, jobDescription = request.description, params = params)
        val trigger: Trigger? = buildCronTrigger(cronExp = request.cron)
        if (trigger == null) {
            log.warn("Trigger is null for addCronJob. Returning...")
            return
        }
        registerJobInScheduler(jobDetail = jobDetail, trigger = trigger)
    }

    @Throws(SchedulerException::class)
    fun findAllActivatedJob(): List<JobResponse> {
        val result: MutableList<JobResponse> = arrayListOf()
        try {
            for (groupName: String in scheduler.jobGroupNames) {
                for (jobKey: JobKey in scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    val trigger: List<Trigger> = scheduler.getTriggersOfJob(jobKey) as List<Trigger>
                    result.add(element = JobResponse(
                        schedulerId = scheduler.getJobDetail(jobKey).jobDataMap["schedulerId"].toString(),
                        jobName = jobKey.name,
                        groupName = jobKey.group,
                        scheduleTime = trigger[0].startTime.toString(),
                        nextFireTime = trigger[0].nextFireTime.toString()
                    ))
                }
            }
        } catch (e: SchedulerException) {
            log.error("SchedulerException : ${e.message}")
        }
        return result
    }

    @Throws(SchedulerException::class)
    fun findCurrentlyExecutingJobs(): List<JobResponse> {
        val result: MutableList<JobResponse> = arrayListOf()
        try {
            for (jobExecutionContext: JobExecutionContext in scheduler.currentlyExecutingJobs) {
                val key: JobKey = jobExecutionContext.jobDetail.key
                result.add(element = JobResponse(
                    schedulerId = jobExecutionContext.mergedJobDataMap["schedulerId"].toString(),
                    jobName = key.name,
                    groupName = key.group,
                    scheduleTime = jobExecutionContext.trigger.startTime.toString(),
                    nextFireTime = jobExecutionContext.trigger.startTime.toString()
                ))
            }
        } catch (e: SchedulerException) {
            log.error("SchedulerException : ${e.message}")
        }
        return result
    }

    @Throws(SchedulerException::class)
    private fun registerJobInScheduler(jobDetail: JobDetail, trigger: Trigger) {
        if (scheduler.checkExists(jobDetail.key)) {
            scheduler.deleteJob(jobDetail.key)
            scheduler.scheduleJob(jobDetail, trigger)
            return
        }
        scheduler.scheduleJob(jobDetail, trigger)
    }

    fun <T: Job> buildJobDetail(job: Class<T>, jobName: String, group: String, jobDescription: String?, params: Map<String, Any>): JobDetail {
        val jobDataMap = JobDataMap()

        if (params.isNotEmpty()) {
            jobDataMap.putAll(params)
        }
        return JobBuilder.newJob(job)
            .withIdentity(jobName, group)
            .withDescription(jobDescription)
            .usingJobData(jobDataMap)
            .build()
    }

    private fun buildCronTrigger(cronExp: String): Trigger? {
        /*
        val triggr: CronTrigger = TriggerBuilder
            .newTrigger()
            .withIdentity("RUN_QUARTZ", "JOB_GROUP")
            .startNow()
            .withSchedule(CronScheduleBuilder.cronSchedule(cronExp))
            .build()
        */
        val cronTriggerFactory = CronTriggerFactoryBean()
        cronTriggerFactory.setCronExpression(cronExp)
        cronTriggerFactory.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW)
        try {
            cronTriggerFactory.afterPropertiesSet()
        } catch (e: ParseException) {
            log.error("ParseException : ${e.message}")
        }
        return cronTriggerFactory.getObject()
    }

    private fun buildTrigger(name: String, group: String, startTime: LocalTime): Trigger? =
        SimpleTriggerFactoryBean()
            .also { it: SimpleTriggerFactoryBean ->
                it.setName(name)
                it.setGroup(group)
                it.setStartTime(localTimeToDate(startTime))
                it.setRepeatCount(0)
                it.setRepeatInterval(0)
                it.afterPropertiesSet()
            }
            .run {
                this.getObject()
            }

    private fun localTimeToDate(startTime: LocalTime): Date =
        startTime
            .atDate(LocalDate.of(LocalDate.now().year, LocalDate.now().month, LocalDate.now().dayOfMonth))
            .atZone(ZoneId.systemDefault()).toInstant()
            .run { Date.from(this) }
}
