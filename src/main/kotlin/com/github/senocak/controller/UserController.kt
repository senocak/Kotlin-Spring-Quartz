package com.github.senocak.controller

import com.github.senocak.domain.Scheduler
import com.github.senocak.domain.User
import com.github.senocak.domain.dto.ExceptionDto
import com.github.senocak.domain.dto.JobResponse
import com.github.senocak.domain.dto.PaginationCriteria
import com.github.senocak.domain.dto.SchedulerRequest
import com.github.senocak.domain.dto.SchedulerResponse
import com.github.senocak.domain.dto.UpdateUserDto
import com.github.senocak.domain.dto.UserPaginationDTO
import com.github.senocak.domain.dto.UserResponse
import com.github.senocak.exception.ServerException
import com.github.senocak.quartz.OperatingSystemJob
import com.github.senocak.quartz.QuartzHandler
import com.github.senocak.quartz.TarihteBugunJob
import com.github.senocak.security.Authorize
import com.github.senocak.service.SchedulerService
import com.github.senocak.service.UserService
import com.github.senocak.service.WebSocketCacheService
import com.github.senocak.util.AppConstants.ADMIN
import com.github.senocak.util.AppConstants.USER
import com.github.senocak.util.AppConstants.securitySchemeName
import com.github.senocak.util.OmaErrorMessageType
import com.github.senocak.util.PageRequestBuilder
import com.github.senocak.util.convertEntityToDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.Pattern
import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@Authorize(roles = [ADMIN, USER])
@RequestMapping(UserController.URL)
@Tag(name = "User", description = "User Controller")
class UserController(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val webSocketCacheService: WebSocketCacheService,
    private val quartzHandler: QuartzHandler,
    private val schedulerService: SchedulerService
): BaseController() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Throws(ServerException::class)
    @Operation(
        summary = "All Users",
        tags = ["User"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = UserPaginationDTO::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN, USER])]
    )
    @GetMapping
    fun allUsers(
        @Parameter(name = "page", description = "Page number", example = "0") @RequestParam(defaultValue = "1", required = false) page: Int,
        @Parameter(name = "size", description = "Page size", example = "20") @RequestParam(defaultValue = "\${spring.data.web.pageable.default-page-size:10}", required = false) size: Int,
        @Parameter(name = "sortBy", description = "Sort by column", example = "id") @RequestParam(defaultValue = "id", required = false) sortBy: String,
        @Parameter(name = "sort", description = "Sort direction", schema = Schema(type = "string", allowableValues = ["asc", "desc"])) @RequestParam(defaultValue = "asc", required = false) @Pattern(regexp = "asc|desc") sort: String,
        @Parameter(name = "q", description = "Search keyword", example = "lorem") @RequestParam(required = false) q: String?
    ): UserPaginationDTO =
        arrayListOf("id", "name", "email")
            .run {
                if (this.none { it == sortBy }) {
                    "Invalid sort column"
                        .also { log.error(it) }
                        .run error@ { throw ServerException(omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                            variables = arrayOf(this@error), statusCode = HttpStatus.BAD_REQUEST) }
                }
                PaginationCriteria(page = page, size = size)
                    .also { it: PaginationCriteria ->
                        it.sortBy = sortBy
                        it.sort = sort
                        it.columns = this
                    }
                    .run paginationCriteria@ {
                        userService.findAllUsers(
                            specification = userService.createSpecificationForUser(q = q),
                            pageRequest = PageRequestBuilder.build(paginationCriteria = this@paginationCriteria)
                        )
                    }
                    .run messagePage@ {
                        UserPaginationDTO(
                            pageModel = this@messagePage,
                            items = this@messagePage.content.map { it: User -> it.convertEntityToDto() }.toList(),
                            sortBy = sortBy,
                            sort = sort
                        )
                    }
            }

    @Throws(ServerException::class)
    @Operation(
        summary = "Get me",
        tags = ["User"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = UserResponse::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN, USER])]
    )
    @GetMapping("/me")
    fun me(): UserResponse =
        userService.loggedInUser
            .run { this.convertEntityToDto(roles = true) }

    @PatchMapping("/me")
    @Operation(
        summary = "Update user",
        tags = ["User"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = UserResponse::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN, USER])]
    )
    @Throws(ServerException::class)
    fun patchMe(request: HttpServletRequest,
        @Parameter(description = "Request body to update", required = true) @Validated @RequestBody userDto: UpdateUserDto,
        resultOfValidation: BindingResult
    ): UserResponse {
        validate(resultOfValidation)
        val user: User = userService.loggedInUser
        val name: String? = userDto.name
        if (!name.isNullOrEmpty())
            user.name = name
        val password: String? = userDto.password
        val passwordConfirmation: String? = userDto.passwordConfirmation
        if (!password.isNullOrEmpty()) {
            if (passwordConfirmation.isNullOrEmpty()) {
                "Password confirmation not provided"
                    .apply { log.error(this) }
                    .run { throw ServerException(omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                        variables = arrayOf(this), statusCode = HttpStatus.BAD_REQUEST) }
            }
            if (passwordConfirmation != password) {
                "Password and confirmation not matched"
                    .apply { log.error(this) }
                    .run { throw ServerException(omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                        variables = arrayOf(this), statusCode = HttpStatus.BAD_REQUEST) }

            }
            user.password = passwordEncoder.encode(password)
        }
        return userService.save(user = user).convertEntityToDto(roles = true)
    }

    @GetMapping("/jobs")
    @Operation(
        summary = "Get all job of user",
        tags = ["User"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = SchedulerResponse::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN, USER])]
    )
    fun findAllJob(): List<SchedulerResponse> =
        schedulerService.findAllByUserId(user = userService.loggedInUser)
            .map { it.convertEntityToDto() }

    @PostMapping("/jobs")
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(
        summary = "Create job",
        tags = ["User"],
        responses = [
            ApiResponse(responseCode = "201", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = SchedulerResponse::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN, USER])]
    )
    fun createJob(
        @Parameter(description = "Request body to create a job", required = true) @Validated @RequestBody cronJobRequest: SchedulerRequest
    ): SchedulerResponse =
        Scheduler(status = cronJobRequest.status, name = cronJobRequest.name, description = cronJobRequest.description,
            groupName = cronJobRequest.group, user = userService.loggedInUser, cron = cronJobRequest.cron, className = cronJobRequest.className)
            .run { schedulerService.save(scheduler = this) }
            .apply { quartzHandler.addCronJob(job = TarihteBugunJob::class.java, request = cronJobRequest, params = mapOf("userid" to "${this.user.id}")) }
            .run { this.convertEntityToDto() }

    @GetMapping("/jobs/{id}")
    @Operation(
        summary = "Get the job",
        tags = ["User"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = SchedulerResponse::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN])]
    )
    fun getJob(
        @Parameter(description = "Id of the job", required = true) @PathVariable id: String
    ): SchedulerResponse =
        schedulerService.findById(schedulerId = UUID.fromString(id))
            .run {
                val jobResponse: JobResponse? = quartzHandler.findAllActivatedJob()
                    .find { it: JobResponse ->
                        it.jobName == this.name && it.groupName == this.groupName
                    }
                this.convertEntityToDto(scheduleTime = jobResponse?.scheduleTime)
            }

    @GetMapping("/jobs/actives")
    @Authorize(roles = [ADMIN])
    @Operation(
        summary = "All active jobs",
        tags = ["User"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = JobResponse::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN])]
    )
    fun findAllActivatedJob(): List<JobResponse> = quartzHandler.findAllActivatedJob()

    @GetMapping("/jobs/current")
    @Authorize(roles = [ADMIN])
    @Operation(
        summary = "All active jobs",
        tags = ["User"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = JobResponse::class)))),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = ExceptionDto::class))))
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN])]
    )
    fun getCurrentlyExecutingJobs(): List<JobResponse> = quartzHandler.findCurrentlyExecutingJobs()

/*
@PostMapping("/jobs/{group}/{name}/{seconds}")
@ResponseStatus(code = HttpStatus.CREATED)
fun createJob(
    @PathVariable group: String,
    @PathVariable name: String,
    @PathVariable seconds: Int
): SampleJobRequest =
    SampleJobRequest(name = name, group = group, userId = UUID.randomUUID())
        .also { it: SampleJobRequest ->
            val now: LocalTime = LocalTime.now()
            it.content = "[Send Message] at $now"
            it.startTime = now.plusSeconds(seconds.toLong())
        }
        .apply {
            val params: MutableMap<String, Any> = HashMap()
            params["content"] = this.content!!
            params["userId"] = this.userId
            quartzHandler.addJob(job = SampleJob::class.java, request = this, params = params)
        }

@PostMapping("/jobs/{group}/{name}/{seconds}")
@ResponseStatus(code = HttpStatus.CREATED)
fun createJob(
    @PathVariable group: String,
    @PathVariable name: String,
    @PathVariable seconds: Int
): SampleJobRequest =
    SampleJobRequest(name = name, group = group, userId = UUID.randomUUID())
        .also { it: SampleJobRequest ->
            val now: LocalTime = LocalTime.now()
            it.content = "[Send Message] at $now"
            it.startTime = now.plusSeconds(seconds.toLong())
        }
        .apply {
            quartzHandler.addJob(SampleJob::class.java, this)
        }
*/

    // TODO: fire event in ws
    companion object {
        const val URL = "/api/v1/user"
    }
}
