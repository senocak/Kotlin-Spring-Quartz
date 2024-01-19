package com.github.senocak.config

import javax.sql.DataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class QuartzConfig(
    private val dataSource: DataSource,
    private val platformTransactionManager: PlatformTransactionManager,
    private val autowiringSpringBeanJobFactory: AutowiringSpringBeanJobFactory
){

    @Bean
    fun schedulerFactoryBean(): SchedulerFactoryBean =
        SchedulerFactoryBean()
            .apply {
                this.setConfigLocation(ClassPathResource("quartz.properties"));
                this.setDataSource(dataSource)
                this.setTransactionManager(platformTransactionManager)
                this.setJobFactory(autowiringSpringBeanJobFactory)
                this.setOverwriteExistingJobs(true)
                this.isAutoStartup = true
            }
}

