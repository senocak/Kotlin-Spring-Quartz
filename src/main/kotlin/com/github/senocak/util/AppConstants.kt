package com.github.senocak.util

import com.github.senocak.domain.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object AppConstants {

    val corePoolSize: Int = Runtime.getRuntime().availableProcessors()
    const val DEFAULT_PAGE_NUMBER = "0"
    const val DEFAULT_PAGE_SIZE = "10"
    const val MAIL_REGEX = "^\\S+@\\S+\\.\\S+$"
    const val TOKEN_HEADER_NAME = "Authorization"
    const val TOKEN_PREFIX = "Bearer "
    const val ADMIN = "ADMIN"
    const val USER = "USER"
    const val securitySchemeName = "bearerAuth"

    fun <R : Any> R.logger(): Lazy<Logger> = lazy {
        LoggerFactory.getLogger((if (javaClass.kotlin.isCompanion) javaClass.enclosingClass else javaClass).name)
    }

    fun randomUser(userRange: ArrayList<User>): Pair<User, User> {
        val pair: Pair<User, User> = Pair(first = userRange.random(), second = userRange.random())
        if (pair.first == pair.second)
            return randomUser(userRange = userRange)
        return pair
    }

    const val EXPECTED_CONDITION_TIMEOUT: Long = 30L
    const val EXPECTED_CONDITION_POLLING_INTERVAL: Long = 1L
    const val DOCUMENT_READY_TIMEOUT: Long = 60L
    const val USER_WAIT_IN_MS: Long = 1500L
    const val WEBDRIVER_TIME_OUT_IN_SECONDS: Long = 3
}
