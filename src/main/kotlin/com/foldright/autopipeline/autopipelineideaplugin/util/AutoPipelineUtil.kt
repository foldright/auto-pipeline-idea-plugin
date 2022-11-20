package com.foldright.autopipeline.autopipelineideaplugin.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiJavaFileImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil

object AutoPipelineUtil {

    private const val AUTO_PIPELINE_ANNOTATION_NAME = "com.foldright.auto.pipeline.AutoPipeline"
    private const val AUTO_PIPELINE_ANNOTATION_PROCESSOR_NAME = "com.foldright.auto.pipeline.processor.AutoPipelineProcessor"

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
            packageName, interfaceName,
        )
    }

    fun hasAutoPipelineAnnotation(psiModifierListOwner: PsiModifierListOwner): Boolean =
        findAnnotation(psiModifierListOwner, AUTO_PIPELINE_ANNOTATION_NAME) != null


    private fun findAnnotation(psiModifierListOwner: PsiModifierListOwner, annotationFQN: String): PsiAnnotation? =
        psiModifierListOwner.getAnnotation(annotationFQN)

    fun isAutoPipelinePresent(project: Project): Boolean {
        if (project.isDefault || !project.isInitialized) {
            return false
        }

        ApplicationManager.getApplication().assertReadAccessAllowed()

        return CachedValuesManager.getManager(project).getCachedValue(project) {
            val javaPsiFacade = JavaPsiFacade.getInstance(project)
            val projectScope = GlobalSearchScope.projectScope(project)
            val projectRootManager = ProjectRootManager.getInstance(project)

            javaPsiFacade.findClass(AUTO_PIPELINE_ANNOTATION_NAME, projectScope)
                ?: return@getCachedValue CachedValueProvider.Result(false, projectRootManager)

            javaPsiFacade.findClass(AUTO_PIPELINE_ANNOTATION_PROCESSOR_NAME, projectScope)
                ?: return@getCachedValue CachedValueProvider.Result(false, projectRootManager)

            return@getCachedValue CachedValueProvider.Result(true, projectRootManager)
        }
    }
}

