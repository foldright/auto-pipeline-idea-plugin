package com.foldright.autopipeline.autopipelineideaplugin

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class CompileIntentionAction : BaseIntentionAction() {

    init {
        text = "Compile project to generate files"
    }

    override fun getFamilyName(): String = "Auto-Pipeline"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = true

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        ApplicationManager.getApplication().invokeLater(Runnable {

            val compilerManager = CompilerManager.getInstance(project)
            val compileScope = compilerManager.createProjectCompileScope(project)
            compilerManager.compile(
                compileScope
            ) { aborted, errors, warnings, compileContext ->
                println("----------------------------")
                println("$aborted  $errors  $warnings $compileContext")
            }
        })
    }


}