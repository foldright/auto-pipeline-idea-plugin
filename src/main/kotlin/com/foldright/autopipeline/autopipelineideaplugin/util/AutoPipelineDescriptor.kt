package com.foldright.autopipeline.autopipelineideaplugin.util

data class AutoPipelineDescriptor(
    val packageName: String,
    val interfaceName: String,
) {
    val pipelineName = "${packageName}.pipeline.${interfaceName}Pipeline"
    val handlerName = "${packageName}.pipeline.${interfaceName}Handler"
    val handlerContextName = "${packageName}.pipeline.${interfaceName}HandlerContext"
    val abstractHandlerContextName = "${packageName}.pipeline.Abstract${interfaceName}HandlerContext"
    val defaultHandlerContextName = "${packageName}.pipeline.Default${interfaceName}HandlerContext"
}