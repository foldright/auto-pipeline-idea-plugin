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

        // currently only process import statement
        if(ref.parent is PsiImportStatement) {
            if (canNotHelp(ref)) return

            val componentQualifiedName = findComponentQualifiedName(ref.qualifiedName) ?: return

            val project = ref.project
            val projectScope = GlobalSearchScope.projectScope(project)
            val javaPsiFacade = JavaPsiFacade.getInstance(project)

            val componentPsiClass = javaPsiFacade.findClass(componentQualifiedName, projectScope) ?: return

            if( AutoPipelineUtil.hasAutoPipelineAnnotation(componentPsiClass) == null ) {
                return
            }

            registrar.register(AutoPipelineCompileIntentionAction())
        }


        if(ref.parent is PsiReferenceList || ref.parent is PsiTypeElement) {
            if (canNotHelp(ref)) return

            val componentShortName = findComponentShortName(ref.qualifiedName) ?: return

            val project = ref.project
            val projectScope = GlobalSearchScope.projectScope(project)

            val componentPsiClass =
                PsiShortNamesCache.getInstance(project).getClassesByName(componentShortName, projectScope) ?: return

            if( AutoPipelineUtil.hasAutoPipelineAnnotation(componentPsiClass[0]) == null ) {
                return
            }

            registrar.register(AutoPipelineCompileIntentionAction())
        }





    }

    private fun canNotHelp(ref: PsiJavaCodeReferenceElement): Boolean {
        val psiFile = ref.containingFile
        if (psiFile !is PsiJavaFile) {
            return true
        }

        if (psiFile.language !is JavaLanguage) {
            return true
        }

        val psiClasses = psiFile.classes

        if (psiClasses.size != 1) {
            return true
        }
        return false
    }

    override fun getReferenceClass(): Class<PsiJavaCodeReferenceElement> =
        PsiJavaCodeReferenceElement::class.java


    companion object {
        private val GENERATED_PIPELINE_HANDLER_OR_CONTEXT_NAME = """^(?<packageName>.+)\.pipeline.(?<componentName>.+)Handler(Context)?$""".toRegex()
        private val GENERATED_PIPELINE_HANDLER_OR_CONTEXT_SHORT_NAME = """^(?<componentName>.+)Handler(Context)?$""".toRegex()

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