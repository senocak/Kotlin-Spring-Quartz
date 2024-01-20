package com.github.senocak.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.senocak.domain.Status
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalTime
import java.util.UUID

data class SchedulerRequest(
    @Schema(example = "lorem", description = "Name of the scheduler", required = true, name = "name", type = "String")
    val name: String,

    @Schema(example = "group1", description = "Group of the scheduler", required = true, name = "group", type = "String")
    val group: String,

    @Schema(example = "0/60 * * * * ?", description = "Cron Expression of the scheduler", required = true, name = "cron", type = "String")
    val cron: String,

    @Schema(example = "TarihteBugunJob", description = "Class name of the scheduler", required = true, name = "className", type = "String")
    val className: String,

    @Schema(example = "Ipsum", description = "Description of the scheduler", required = false, name = "description", type = "String")
    var description: String? = null,

    @Schema(example = "ACTIVE", description = "Status of the scheduler", required = false, name = "status", type = "Status")
    var status: Status = Status.UNKNOWN,
)

data class SchedulerResponse(
    @Schema(example = "b8c13284-b8bb-4bfc-ae58-b1281e8d4784", description = "Id of the scheduler", required = true, name = "id", type = "String")
    val id: UUID,

    @Schema(example = "lorem", description = "Name of the scheduler", required = true, name = "name", type = "String")
    val name: String,

    @Schema(example = "group1", description = "Group of the scheduler", required = true, name = "group", type = "String")
    val group: String,

    @Schema(example = "0/60 * * * * ?", description = "Cron Expression of the scheduler", required = true, name = "cron", type = "String")
    val cron: String,

    @Schema(example = "com.github.senocak.quartz.OperatingSystemJob", description = "Class name of of the scheduler", required = true, name = "className", type = "String")
    val className: String,

    @Schema(example = "Ipsum", description = "Description of the scheduler", required = false, name = "description", type = "String")
    var description: String? = null,

    @Schema(example = "ACTIVE", description = "Status of the scheduler", required = false, name = "status", type = "Status")
    var status: Status = Status.UNKNOWN,

    @JsonProperty("user")
    @Schema(required = true)
    var userResponse: UserResponse?,

    @ArraySchema(schema = Schema(description = "results", required = false, type = "String"))
    var results: List<String> = arrayListOf(),

    val scheduleTime: String,
    val nextFireTime: String
)

data class SampleJobRequest(
    val name: String,
    val group: String,
    val userId: UUID = UUID.randomUUID()
) {
    var startTime: LocalTime? = null
    var content: String? = null
    var description: String? = null
}
