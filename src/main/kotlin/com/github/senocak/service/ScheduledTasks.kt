package com.github.senocak.service

import com.github.senocak.domain.dto.WebsocketIdentifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.socket.PingMessage

@Component
@EnableScheduling
class ScheduledTasks(
    private val webSocketCacheService: WebSocketCacheService
){
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * this is scheduled to run every in 10_000 milliseconds period // every 10 seconds
     */
    @Scheduled(fixedRate = 10_000)
    fun pingWs() {
        val allWebSocketSession: Map<String, WebsocketIdentifier> = webSocketCacheService.allWebSocketSession
        if (allWebSocketSession.isNotEmpty())
            for (entry: Map.Entry<String, WebsocketIdentifier> in allWebSocketSession) {
                try {
                    entry.value.session.sendMessage(PingMessage())
                    log.info("Pinged user with key: ${entry.key}, and session: ${entry.value}")
                } catch (e: Exception) {
                    log.error("Exception occurred for sending ping message: ${e.message}")
                    webSocketCacheService.deleteSession(key = entry.key)
                }
            }
    }
}