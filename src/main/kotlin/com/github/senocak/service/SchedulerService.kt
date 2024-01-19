package com.github.senocak.service

import com.github.senocak.domain.Scheduler
import com.github.senocak.domain.User
import com.github.senocak.exception.SchedulerNotFoundException
import com.github.senocak.repository.SchedulerRepository
import com.github.senocak.util.AppConstants.logger
import java.util.UUID
import org.slf4j.Logger
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class SchedulerService(
    private val schedulerRepository: SchedulerRepository,
){
    private val log: Logger by logger()

    fun count(): Long = schedulerRepository.count()
        .also { log.info("Total $it schedulers found.") }
    fun findAllPageable(page: Int, size: Int): Page<Scheduler> = schedulerRepository.findAll(PageRequest.of(page, size))
    fun findAll(): List<Scheduler> = schedulerRepository.findAll()
    fun findAllByUserId(user: User): List<Scheduler> = schedulerRepository.findAllByUser(user = user)
    fun save(scheduler: Scheduler): Scheduler = schedulerRepository.save(scheduler)
        .also { log.info("Scheduler: $it saved or updated") }
    fun deleteAll(): Unit = schedulerRepository.deleteAll()
    fun findById(schedulerId: UUID): Scheduler = schedulerRepository.findByIdOrNull(schedulerId)
        .also { log.info("Scheduler: $it found for $schedulerId.") }
        ?: throw SchedulerNotFoundException(element = schedulerId.toString())
            .also { log.error("SchedulerNotFoundException: $it") }
    fun delete(schedulerId: UUID): Unit = delete(scheduler = findById(schedulerId = schedulerId))
    fun delete(scheduler: Scheduler): Unit = schedulerRepository.delete(scheduler)
        .also { log.info("Scheduler: $it removed") }
}