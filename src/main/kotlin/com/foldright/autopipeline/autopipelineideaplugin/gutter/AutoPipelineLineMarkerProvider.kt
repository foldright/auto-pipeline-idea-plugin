package com.foldright.autopipeline.autopipelineideaplugin.gutter

import com.foldright.autopipeline.autopipelineideaplugin.util.AutoPipelineUtil
import com.foldright.autopipeline.autopipelineideaplugin.icon.Icons
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope

class AutoPipelineLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {

        if(!AutoPipelineUtil.isAutoPipelineAnnotated(element)) {
            return
        }

        val autoPipelineDescriptor = AutoPipelineUtil.computeAutoPipelineDescriptor(element)
        val autoPipelineGenFilesName = listOf(
            autoPipelineDescriptor.pipelineName,
            autoPipelineDescriptor.handlerName,
            autoPipelineDescriptor.handlerContextName,
            autoPipelineDescriptor.abstractHandlerContextName,
            autoPipelineDescriptor.defaultHandlerContextName,
        )


        val project = element.project

        val projectScope = GlobalSearchScope.projectScope(project)
        val javaPsiFacade = JavaPsiFacade.getInstance(project)

        val psiFiles = autoPipelineGenFilesName.mapNotNull { javaPsiFacade.findClass(it, projectScope) }

        if (psiFiles.isEmpty()) {
            return
        }

        val builder = NavigationGutterIconBuilder.create(Icons.MARKER)
            .setTooltipText("Show class generated by <strong>@AutoPipeline</strong>")
            .setAlignment(GutterIconRenderer.Alignment.RIGHT)
            .setPopupTitle("Class generated by @AutoPipeline for ${autoPipelineDescriptor.interfaceName}")
            .setTargets(psiFiles)

        result.add(builder.createLineMarkerInfo(element))
    }
}