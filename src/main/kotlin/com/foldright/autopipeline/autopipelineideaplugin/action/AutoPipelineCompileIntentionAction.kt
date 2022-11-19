package com.foldright.autopipeline.autopipelineideaplugin.action

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class AutoPipelineCompileIntentionAction : BaseIntentionAction(), PriorityAction {

    init {
        text = "Compile project to generate pipeline"
    }

    override fun getFamilyName(): String = "Auto-Pipeline"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = true

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        ApplicationManager.getApplication().invokeLater {
            val compilerManager = CompilerManager.getInstance(project)
            compilerManager.rebuild { _, _, _, _ -> }
        }
    }

    override fun getPriority(): PriorityAction.Priority = PriorityAction.Priority.TOP


}