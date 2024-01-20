package com.github.senocak.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object AppConstants {

    val corePoolSize: Int = Runtime.getRuntime().availableProcessors()
    const val TOKEN_HEADER_NAME = "Authorization"
    const val TOKEN_PREFIX = "Bearer "
    const val ADMIN = "ADMIN"
    const val USER = "USER"
    const val securitySchemeName = "bearerAuth"

    const val EXPECTED_CONDITION_TIMEOUT: Long = 30L
    const val EXPECTED_CONDITION_POLLING_INTERVAL: Long = 1L
    const val DOCUMENT_READY_TIMEOUT: Long = 60L
    const val USER_WAIT_IN_MS: Long = 1500L
    const val WEBDRIVER_TIME_OUT_IN_SECONDS: Long = 3

    fun <R : Any> R.logger(): Lazy<Logger> = lazy {
        LoggerFactory.getLogger((if (javaClass.kotlin.isCompanion) javaClass.enclosingClass else javaClass).name)
    }
}
