package com.foldright.autopipeline.autopipelineideaplugin

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.PropertyKey

class AutoPipelineBundle : DynamicBundle(BUNDLE_PATH) {

    companion object {
        private const val BUNDLE_PATH = "messages.AutoPipeline"
        private val BUNDLE = AutoPipelineBundle()

        @Nls
        fun message(@NotNull @PropertyKey(resourceBundle = BUNDLE_PATH) key:String, @NotNull vararg params: String): String =
            BUNDLE.getMessage(key, params)

    }
}