package com.foldright.autopipeline.autopipelineideaplugin.util

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiJavaFileImpl
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil

object AutoPipelineUtil {

    private const val AUTO_PIPELINE_ANNOTATION_PACKAGE = "com.foldright.auto.pipeline"
    private const val AUTO_PIPELINE_ANNOTATION_NAME = "com.foldright.auto.pipeline.AutoPipeline"
    private const val AUTO_PIPELINE_ANNOTATION_SHORT_NAME = "AutoPipeline"

    fun isAutoPipelineAnnotated(element: PsiElement): Boolean {
        val psiFile = element.containingFile
        if (psiFile !is PsiJavaFile) {
            return false
        }
        if (psiFile.language !is JavaLanguage) {
            return false
        }

        if (!psiFile.name.endsWith(".java")) {
            return false
        }

        if (element !is PsiIdentifier ||
            element.parent !is PsiJavaCodeReferenceElement ||
            element.parent.parent !is PsiAnnotation
        ) {
            return false
        }

        val psiJavaCodeReferenceElement = element.parent as PsiJavaCodeReferenceElement

        return isAutoPipelineAnnotation(psiJavaCodeReferenceElement.qualifiedName)
    }

    fun isAutoPipelineAnnotation(annotationFQN: String): Boolean =
        annotationFQN == AUTO_PIPELINE_ANNOTATION_NAME

    fun computeAutoPipelineDescriptor(element: PsiElement): AutoPipelineDescriptor {

        val psiAnnotation = PsiTreeUtil.getParentOfType(element, PsiAnnotation::class.java)
        val psiModifierList = PsiTreeUtil.getParentOfType(psiAnnotation, PsiModifierList::class.java)
        val psiClass = PsiTreeUtil.getParentOfType(psiModifierList, PsiClass::class.java)
        val psiFile = PsiTreeUtil.getParentOfType(psiClass, PsiJavaFileImpl::class.java)

        val interfaceName = psiClass!!.name!!
        val packageName = psiFile!!.packageName

        return AutoPipelineDescriptor(
            packageName, interfaceName,
        )
    }

    fun hasAutoPipelineAnnotation(psiModifierListOwner: PsiModifierListOwner): Boolean =
        findAnnotation(psiModifierListOwner, AUTO_PIPELINE_ANNOTATION_NAME) != null
                || findAnnotation(psiModifierListOwner, AUTO_PIPELINE_ANNOTATION_SHORT_NAME) != null


    fun isAutoPipelinePresent(project: Project): Boolean {
        if (project.isDefault || !project.isInitialized) {
            return false
        }

        ApplicationManager.getApplication().assertReadAccessAllowed()
        return CachedValuesManager.getManager(project).getCachedValue(project) {
            val psiPackage = JavaPsiFacade.getInstance(project).findPackage(AUTO_PIPELINE_ANNOTATION_PACKAGE)
            return@getCachedValue CachedValueProvider.Result(psiPackage, ProjectRootManager.getInstance(project))
        } != null
    }

    private fun findAnnotation(psiModifierListOwner: PsiModifierListOwner, annotationFQN: String): PsiAnnotation? =
        psiModifierListOwner.getAnnotation(annotationFQN)

}

