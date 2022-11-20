package com.foldright.autopipeline.autopipelineideaplugin.reference

import com.foldright.autopipeline.autopipelineideaplugin.action.AutoPipelineCompileIntentionAction
import com.foldright.autopipeline.autopipelineideaplugin.util.AutoPipelineUtil
import com.intellij.codeInsight.daemon.QuickFixActionRegistrar
import com.intellij.codeInsight.quickfix.UnresolvedReferenceQuickFixProvider
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache

class AutoPipelineUnresolvedReferenceQuickFixProvider :
    UnresolvedReferenceQuickFixProvider<PsiJavaCodeReferenceElement>() {

    override fun registerFixes(ref: PsiJavaCodeReferenceElement, registrar: QuickFixActionRegistrar) {
        val parent = ref.parent
        if (parent is PsiImportStatement || parent is PsiReferenceList || parent is PsiTypeElement) {
            if (!canFix(ref)) return
            val componentPsiClass = findComponentClass(ref)
            AutoPipelineUtil.hasAutoPipelineAnnotation(componentPsiClass)?.apply {
                registrar.register(AutoPipelineCompileIntentionAction)
            }
        }
    }

    override fun getReferenceClass(): Class<PsiJavaCodeReferenceElement> =
        PsiJavaCodeReferenceElement::class.java

    private fun findComponentClass(ref: PsiJavaCodeReferenceElement): PsiClass? {
        val project = ref.project
        val projectScope = GlobalSearchScope.projectScope(project)
        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        val psiShortNameCache = PsiShortNamesCache.getInstance(project)

        val componentPsiClass = when (ref.parent) {
            is PsiImportStatement ->  findComponentQualifiedName(ref.qualifiedName)?.let { componentQualifiedName ->
                javaPsiFacade.findClass(componentQualifiedName, projectScope)
            }

            is PsiReferenceList, is PsiTypeElement -> {
                val componentShortName = findComponentShortName(ref.qualifiedName) ?: return null
                val classes = psiShortNameCache.getClassesByName(componentShortName, projectScope)
                return if (classes.isNotEmpty()) classes[0] else null
            }

            else -> null
        }

        return componentPsiClass
    }

    private fun canFix(ref: PsiJavaCodeReferenceElement): Boolean {
        val psiFile = ref.containingFile
        if (psiFile !is PsiJavaFile) {
            return false
        }

        if (psiFile.language !is JavaLanguage) {
            return false
        }

        val psiClasses = psiFile.classes

        if (psiClasses.size != 1) {
            return false
        }
        return true
    }

    companion object {
        private val GENERATED_PIPELINE_HANDLER_OR_CONTEXT_NAME =
            """^(?<packageName>.+)\.pipeline.(?<componentName>.+)Handler(Context)?$""".toRegex()
        private val GENERATED_PIPELINE_HANDLER_OR_CONTEXT_SHORT_NAME =
            """^(?<componentName>.+)Handler(Context)?$""".toRegex()

        private fun findComponentQualifiedName(qualifiedName: String): String? =
            GENERATED_PIPELINE_HANDLER_OR_CONTEXT_NAME.matchEntire(qualifiedName)?.groups?.run {
                val packageName = get("packageName")?.value!!
                val componentName = get("componentName")?.value!!
                "${packageName}.${componentName}"
            }

        private fun findComponentShortName(shortName: String): String? =
            GENERATED_PIPELINE_HANDLER_OR_CONTEXT_SHORT_NAME.matchEntire(shortName)?.groups?.run {
                get("componentName")?.value!!
            }
    }

}