package com.foldright.autopipeline.autopipelineideaplugin.activity

import com.foldright.autopipeline.autopipelineideaplugin.AutoPipelineBundle
import com.foldright.autopipeline.autopipelineideaplugin.util.AutoPipelineUtil
import com.intellij.compiler.CompilerConfiguration
import com.intellij.compiler.CompilerConfigurationImpl
import com.intellij.compiler.server.BuildManagerListener
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.notification.SingletonNotificationManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.awt.RelativePoint
import java.util.*
import java.util.concurrent.Callable

class AutoPipelineProjectValidatorActivity : StartupActivity.DumbAware {

    override fun runActivity(project: Project) {
        val connection = project.messageBus.connect()

        connection.subscribe(BuildManagerListener.TOPIC, AutoPipelineBuildManagerListener())

    }
}

class AutoPipelineBuildManagerListener : BuildManagerListener {

    private val notificationManager: SingletonNotificationManager =
        SingletonNotificationManager("AutoPipeline plugin", NotificationType.ERROR)

    override fun beforeBuildProcessStarted(project: Project, sessionId: UUID) {
        if (hasAnnotationProcessEnable(project)) {
            return
        }

        val isAutoPipelinePresent = ReadAction.nonBlocking(Callable {
            AutoPipelineUtil.isAutoPipelinePresent(project)
        }).executeSynchronously()

        if (!isAutoPipelinePresent) {
            return
        }

        suggestEnableAnnotations(project)
    }

    private fun hasAnnotationProcessEnable(project: Project): Boolean {
        val compilerConfiguration = getCompilerConfiguration(project)

        return compilerConfiguration.defaultProcessorProfile.isEnabled
                && compilerConfiguration.moduleProcessorProfiles.all { it.isEnabled }
    }

    private fun enableAnnotationProcessors(project: Project) {
        val compilerConfiguration = getCompilerConfiguration(project)
        compilerConfiguration.defaultProcessorProfile.isEnabled = true
        compilerConfiguration.moduleProcessorProfiles.forEach { it.isEnabled = true }

        val statusBar = WindowManager.getInstance().getStatusBar(project)
        JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder(
                AutoPipelineBundle.message("popup.content.java.annotation.processing.has.been.enabled"),
                MessageType.INFO,
                null
            )
            .setFadeoutTime(3000)
            .createBalloon()
            .show(RelativePoint.getNorthEastOf(statusBar.component), Balloon.Position.atRight)
    }

    private fun getCompilerConfiguration(project: Project): CompilerConfigurationImpl {
        return CompilerConfiguration.getInstance(project) as CompilerConfigurationImpl
    }

    private fun suggestEnableAnnotations(project: Project) {
        notificationManager.notify("",
            AutoPipelineBundle.message("config.warn.annotation-processing.disabled.title"),
            project) { notification ->
                notification.addAction(object : NotificationAction(AutoPipelineBundle.message("notification.enable.annotation.processing")) {
                    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                        enableAnnotationProcessors(project)
                        notification.expire()
                    }

                })

            }
    }

}