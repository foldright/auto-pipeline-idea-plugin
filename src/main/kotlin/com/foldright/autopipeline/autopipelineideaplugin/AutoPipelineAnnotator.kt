package com.foldright.autopipeline.autopipelineideaplugin

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiJavaFileImpl
import com.intellij.psi.search.GlobalSearchScope

class AutoPipelineAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is PsiIdentifier ||
            element.parent !is PsiJavaCodeReferenceElement ||
            element.parent.parent !is PsiAnnotation
        ) {
            return
        }

        val psiJavaCodeReferenceElement = element.parent as PsiJavaCodeReferenceElement

        if (psiJavaCodeReferenceElement.qualifiedName != "com.foldright.auto.pipeline.AutoPipeline") {
            return
        }

        val psiAnnotation = psiJavaCodeReferenceElement.parent as PsiAnnotation
        val psiModifierList = psiAnnotation.parent as PsiModifierList
        val psiClass = psiModifierList.parent as PsiClass
        val psiFile = psiClass.parent as PsiJavaFileImpl

        val interfaceName = psiClass.name
        val project = psiClass.project
        val packageName = psiFile.packageName

        val pipelineName = "${packageName}.pipeline.${interfaceName}Pipeline"

        val projectScope = GlobalSearchScope.projectScope(project)
        val javaPsiFacade = JavaPsiFacade.getInstance(project)

        val pipelineClass = javaPsiFacade.findClass(pipelineName, projectScope)

        if (pipelineClass != null) {
            return
        }

        holder
            .newAnnotation(HighlightSeverity.ERROR, "Auto-Pipeline not generate files")
            .range(element)
            .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            .withFix(CompileIntentionAction())
            .create()
    }
}