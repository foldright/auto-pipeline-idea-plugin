package com.foldright.autopipeline.autopipelineideaplugin.action

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectTracker
import com.intellij.openapi.externalSystem.autoimport.ProjectNotificationAware
import com.intellij.openapi.externalSystem.autoimport.ProjectRefreshAction
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

object AutoPipelineCompileIntentionAction : BaseIntentionAction(), PriorityAction {

    init {
        text = "Compile project to generate Pipeline"
    }

    override fun getFamilyName(): String = "Auto-Pipeline"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = true

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        ApplicationManager.getApplication().invokeLater {
            val compilerManager = CompilerManager.getInstance(project)
            compilerManager.rebuild { abort, errors, _, _ ->
                if(!abort && errors == 0) {
                    refreshProject(project)
                }
            }
        }
    }

    override fun getPriority(): PriorityAction.Priority = PriorityAction.Priority.TOP

    private fun refreshProject(project: Project) {
        val projectNotificationAware = ProjectNotificationAware.getInstance(project)
        val systemIds = projectNotificationAware.getSystemIds()
        if (ExternalSystemUtil.confirmLoadingUntrustedProject(project, systemIds)) {
            val projectTracker = ExternalSystemProjectTracker.getInstance(project)
            projectTracker.markDirtyAllProjects()
            projectTracker.scheduleProjectRefresh()
        }
    }

}