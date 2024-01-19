package com.github.senocak

import com.github.senocak.domain.Role
import com.github.senocak.domain.Status
import com.github.senocak.domain.User
import com.github.senocak.domain.dto.SchedulerRequest
import com.github.senocak.quartz.JobsListener
import com.github.senocak.quartz.OperatingSystemJob
import com.github.senocak.quartz.QuartzHandler
import com.github.senocak.quartz.TarihteBugunJob
import com.github.senocak.quartz.SchedulersListener
import com.github.senocak.quartz.TriggersListener
import com.github.senocak.service.RoleService
import com.github.senocak.service.SchedulerService
import com.github.senocak.service.UserService
import com.github.senocak.util.AppConstants.logger
import com.github.senocak.util.RoleName
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import com.github.senocak.domain.Scheduler as SchedulerEntity

@SpringBootApplication
class MainApplication(
    private val roleService: RoleService,
    private val userService: UserService,
    private val schedulerService: SchedulerService,
    private val passwordEncoder: PasswordEncoder,
    private val scheduler: Scheduler,
    private val quartzHandler: QuartzHandler,
    private val jobsListener: JobsListener,
    private val triggersListener: TriggersListener,
    private val schedulersListener: SchedulersListener
){
    private val log: Logger by logger()

    @Value("\${spring.jpa.hibernate.ddl-auto}")
    lateinit var ddl: String

    @Transactional
    @EventListener(value = [ApplicationReadyEvent::class])
    fun init(event: ApplicationReadyEvent): Unit =
        log.debug("[ApplicationReadyEvent]: ${event.timeTaken.toSeconds()}")
            .also {
                try {
                    scheduler.clear()
                    scheduler.listenerManager.addJobListener(jobsListener)
                    scheduler.listenerManager.addTriggerListener(triggersListener)
                    scheduler.listenerManager.addSchedulerListener(schedulersListener)
                } catch (e: SchedulerException) {
                    "SchedulerException: ${e.message}"
                        .apply { log.error(this) }
                        .run { throw RuntimeException(this) }
                }

                if (ddl == "create" || ddl == "create-drop") {
                    roleService.deleteAll()
                    userService.deleteAll()
                    val userRole: Role = roleService.save(role = Role(name = RoleName.ROLE_USER))
                    val adminRole: Role = roleService.save(role = Role(name = RoleName.ROLE_ADMIN))
                    val user1: User = userService.save(user = User(name = "Anıl Şenocak", email = "anil@senocak.com", password = passwordEncoder.encode("asenocak"), roles = arrayListOf(userRole, adminRole)))
                    val user2: User = userService.save(user = User(name = "Canela Skin", email = "canela@skin.com", password = passwordEncoder.encode("asenocak"), roles = arrayListOf(userRole)))
                    val scheduler1: SchedulerEntity = schedulerService.save(scheduler = SchedulerEntity(name = "Every 3 minutes starting at :00 minute after the hour", groupName = "cron", className = "com.github.senocak.quartz.OperatingSystemJob", cron = "0 0/3 * * * ?", status = Status.ENABLED, user = user1))
                    log.debug("[ApplicationReadyEvent]: db migrated.")
                }

                schedulerService.findAll()
                    .forEach { se: SchedulerEntity ->
                        if (se.status == Status.ENABLED)
                            SchedulerRequest(name = se.name, group = se.groupName, cron = se.cron, description = "[Description] ${se.description}", className =  se.className)
                                .apply {
                                    val className: Class<*> = Class.forName(se.className)
                                    val clazz: Class<out Job> = when (className) {
                                        TarihteBugunJob::class.java -> TarihteBugunJob::class.java
                                        OperatingSystemJob::class.java -> OperatingSystemJob::class.java
                                        else -> throw RuntimeException("invalid job class")
                                    }
                                    quartzHandler.addCronJob(
                                        job = clazz,
                                        request = this,
                                        params = mapOf("userId" to "${se.user.id}", "schedulerId" to "${se.id}")
                                    )
                                }
                    }
            }
}

fun main(args: Array<String>) {
    runApplication<MainApplication>(*args)
}
