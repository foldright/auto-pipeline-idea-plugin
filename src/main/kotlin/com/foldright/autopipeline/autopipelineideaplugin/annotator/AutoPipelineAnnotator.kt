package com.foldright.autopipeline.autopipelineideaplugin.annotator

import com.foldright.autopipeline.autopipelineideaplugin.action.AutoPipelineCompileIntentionAction
import com.foldright.autopipeline.autopipelineideaplugin.util.AutoPipelineUtil
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope

class AutoPipelineAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {

        if (!AutoPipelineUtil.isAutoPipelineAnnotated(element)) {
            return
        }

        val autoPipelineDescriptor = AutoPipelineUtil.computeAutoPipelineDescriptor(element)

        val project = element.project
        val projectScope = GlobalSearchScope.projectScope(project)
        val javaPsiFacade = JavaPsiFacade.getInstance(project)

        val pipelineClass = javaPsiFacade.findClass(autoPipelineDescriptor.pipelineName, projectScope)
        if (pipelineClass != null) {
            return
        }

        holder
            .newAnnotation(
                HighlightSeverity.WEAK_WARNING,
                "AutoPipeline doesn't generate files, try to compile project to generate pipeline classes"
            )
            .range(element)
            .highlightType(ProblemHighlightType.WARNING)
            .withFix(AutoPipelineCompileIntentionAction)
            .create()
    }
}