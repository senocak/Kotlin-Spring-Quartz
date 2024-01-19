package com.github.senocak.config

import org.quartz.spi.TriggerFiredBundle
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.SpringBeanJobFactory

@Configuration
class AutowiringSpringBeanJobFactory(
    private val applicationContext: ApplicationContext
): SpringBeanJobFactory() {
    private val beanFactory: AutowireCapableBeanFactory = applicationContext.autowireCapableBeanFactory

    override fun createJobInstance(bundle: TriggerFiredBundle): Any {
        val job = super.createJobInstance(bundle)
        beanFactory.autowireBean(job)
        beanFactory.initializeBean(job, job.javaClass.name)
        return job
    }
}
