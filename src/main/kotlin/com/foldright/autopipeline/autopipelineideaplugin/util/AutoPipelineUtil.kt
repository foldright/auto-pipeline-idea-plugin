package com.foldright.autopipeline.autopipelineideaplugin.util

import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiJavaFileImpl
import com.intellij.psi.util.PsiTreeUtil

object AutoPipelineUtil {

    private const val AUTO_PIPELINE_ANNOTATION_NAME = "com.foldright.auto.pipeline.AutoPipeline"

    fun isAutoPipelineAnnotated(element: PsiElement): Boolean {
        if (element !is PsiIdentifier ||
            element.parent !is PsiJavaCodeReferenceElement ||
            element.parent.parent !is PsiAnnotation
        ) {
            return false
        }

        val psiJavaCodeReferenceElement = element.parent as PsiJavaCodeReferenceElement

        if (psiJavaCodeReferenceElement.qualifiedName != AUTO_PIPELINE_ANNOTATION_NAME) {
            return false
        }

        return true
    }

    fun computeAutoPipelineDescriptor(element: PsiElement): AutoPipelineDescriptor {

        val psiAnnotation = PsiTreeUtil.getParentOfType(element, PsiAnnotation::class.java)
        val psiModifierList = PsiTreeUtil.getParentOfType(psiAnnotation, PsiModifierList::class.java)
        val psiClass = PsiTreeUtil.getParentOfType(psiModifierList, PsiClass::class.java)
        val psiFile = PsiTreeUtil.getParentOfType(psiClass, PsiJavaFileImpl::class.java)

        val interfaceName = psiClass!!.name!!
        val packageName = psiFile!!.packageName

        return AutoPipelineDescriptor(
            interfaceName, packageName
        )
    }

    fun hasAutoPipelineAnnotation(psiModifierListOwner: PsiModifierListOwner?): PsiAnnotation? =
        psiModifierListOwner?.run {
            findAnnotation(this, AUTO_PIPELINE_ANNOTATION_NAME)
        }


    private fun findAnnotation(psiModifierListOwner: PsiModifierListOwner, annotationFQN: String): PsiAnnotation? =
        psiModifierListOwner.getAnnotation(annotationFQN)
}

