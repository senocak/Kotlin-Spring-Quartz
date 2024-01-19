package com.github.senocak.quartz

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.domain.Scheduler
import com.github.senocak.service.SchedulerService
import com.github.senocak.util.AppConstants.logger
import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory
import java.text.DecimalFormat
import java.util.Date
import java.util.UUID
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.PersistJobDataAfterExecution
import org.slf4j.Logger
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
class OperatingSystemJob(
    private val schedulerService: SchedulerService,
    private val objectMapper: ObjectMapper
): Job {
    private val log: Logger by logger()
    private val operatingSystemMXBean: OperatingSystemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)

    private val byte = 1L
    private val kb: Long = byte * 1000
    private val mb: Long = kb * 1000
    private val gb: Long = mb * 1000
    private val tb: Long = gb * 1000

    @Transactional
    override fun execute(context: JobExecutionContext) {
        val jobDataMap: JobDataMap = context.jobDetail.jobDataMap
        val schedulerId: UUID = UUID.fromString(jobDataMap["schedulerId"].toString())

        val runtime: Runtime = Runtime.getRuntime()
        val performance = Performance(
            timestamp = Date().time,
            committedVirtualMemorySize = operatingSystemMXBean.committedVirtualMemorySize,
            totalSwapSpaceSize = operatingSystemMXBean.totalSwapSpaceSize,
            freeSwapSpaceSize = operatingSystemMXBean.freeSwapSpaceSize,
            totalMemorySize = operatingSystemMXBean.totalMemorySize,
            freeMemorySize = operatingSystemMXBean.freeMemorySize,
            cpuLoad = operatingSystemMXBean.cpuLoad,
            processCpuLoad = operatingSystemMXBean.processCpuLoad,
            availableProcessors = runtime.availableProcessors(),
            totalMemory = toHumanReadableSIPrefixes(runtime.totalMemory()),
            maxMemory = toHumanReadableSIPrefixes(runtime.maxMemory()),
            freeMemory = toHumanReadableSIPrefixes(runtime.freeMemory())
        )
        log.info("""
            [${context.jobDetail.key} - ${context.jobDetail.description}]
            fireTime: ${context.fireTime}
            scheduledFireTime: ${context.scheduledFireTime}
            prevFireTime: ${context.previousFireTime}
            nextFireTime: ${context.nextFireTime}
            schedulerId: $schedulerId
            Performance: $performance
        """)

        val findById: Scheduler = schedulerService.findById(schedulerId = schedulerId)
        findById.result.add(element = objectMapper.writeValueAsString(performance))
        schedulerService.save(scheduler = findById)
        Thread.sleep(20 * 1_000)
    }

    private fun toHumanReadableSIPrefixes(size: Long): String =
        when {
            size >= tb -> formatSize(size = size, divider = tb, unitName = "TB")
            size >= gb -> formatSize(size = size, divider = gb, unitName = "GB")
            size >= mb -> formatSize(size = size, divider = mb, unitName = "MB")
            size >= kb -> formatSize(size = size, divider = kb, unitName = "KB")
            else -> formatSize(size = size, divider = byte, unitName = "Bytes")
        }

    private fun formatSize(size: Long, divider: Long, unitName: String): String =
        DecimalFormat("#.##").format(size.toDouble() / divider) + " " + unitName
}

data class Performance(
    var timestamp: Long = 0,
    var committedVirtualMemorySize: Long = 0,
    var totalSwapSpaceSize: Long = 0,
    var freeSwapSpaceSize: Long = 0,
    var totalMemorySize: Long = 0,
    var freeMemorySize: Long = 0,
    var cpuLoad: Double = 0.0,
    var processCpuLoad: Double = 0.0,
    var availableProcessors: Int = 0,
    var totalMemory: String = "",
    var maxMemory: String = "",
    var freeMemory: String = "",
)