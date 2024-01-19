package com.github.senocak.quartz

import com.github.senocak.domain.Scheduler
import com.github.senocak.service.SchedulerService
import com.github.senocak.util.AppConstants.logger
import com.github.senocak.util.split
import java.text.SimpleDateFormat
import java.time.Month
import java.util.Date
import java.util.UUID
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
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
class TarihteBugunJob(
    private val schedulerService: SchedulerService
): Job {
    private val log: Logger by logger()

    private fun ayAdi(ay: Int): String =
        arrayOf("Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")[ay]

    @Transactional
    override fun execute(context: JobExecutionContext) {
        val jobDataMap: JobDataMap = context.jobDetail.jobDataMap
        val schedulerId: UUID = UUID.fromString(jobDataMap["schedulerId"].toString())
        val userId: String = jobDataMap["userId"].toString()

        val dataMap: HashMap<String, List<TarihteBugun?>> = hashMapOf()
        val date = Date()
        val day: String = SimpleDateFormat("dd").format(date)
        val month: Int = SimpleDateFormat("M").format(date).toInt()
        val doc: Document = Jsoup.connect("https://tr.wikipedia.org/wiki/${day}_${ayAdi(ay = month)}").get()
        val contentDiv: Elements = doc.select("div[class='mw-content-ltr mw-parser-output']>ul")
        if (contentDiv.size != 4) {
            log.error("Invalid size: ${contentDiv.size} of data found")
            return
        }
        contentDiv.forEachIndexed {
            index: Int, element: Element ->
            val key: String = when(index) {
                0 -> "olaylar"
                1 -> "dogumlar"
                2 -> "ölümler"
                3 -> "tatiller"
                else -> throw RuntimeException("else")
            }
            dataMap[key] = element.select("li")
                .map { it: Element ->
                    val split: Array<String> = it.text().split(delimiter = " - ") ?: arrayOf(it.text())
                    when(split.size) {
                        1 -> TarihteBugun(day = 1, month = Month.of(1), year = null, text = split[0])
                        2 -> TarihteBugun(day = 1, month = Month.of(1), year = split[0], text = split[1])
                        else -> log.info("TarihteBugun is set null").run { null }
                    }

                }
        }
        val findById: Scheduler = schedulerService.findById(schedulerId = schedulerId)
        findById.result.add(element = dataMap.toString())
        schedulerService.save(scheduler = findById)

        log.info("""
            [${context.jobDetail.key} - ${context.jobDetail.description}]
            fireTime: ${context.fireTime}
            scheduledFireTime: ${context.scheduledFireTime}
            prevFireTime: ${context.previousFireTime}
            nextFireTime: ${context.nextFireTime}
            schedulerId: $schedulerId
            userId: $userId
        """)
    }
}

data class TarihteBugun(val day: Int, val month: Month, val year: String?, val text: String)
