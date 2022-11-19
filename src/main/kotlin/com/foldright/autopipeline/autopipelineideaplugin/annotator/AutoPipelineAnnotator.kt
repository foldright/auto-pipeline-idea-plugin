package com.foldright.autopipeline.autopipelineideaplugin.annotator

import com.foldright.autopipeline.autopipelineideaplugin.util.AutoPipelineUtil
import com.foldright.autopipeline.autopipelineideaplugin.action.AutoPipelineCompileIntentionAction
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.*
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
            .newAnnotation(HighlightSeverity.ERROR, "Auto-Pipeline doesn't generate files, try to compile project to generate")
            .range(element)
            .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            .withFix(AutoPipelineCompileIntentionAction())
            .create()
    }
}