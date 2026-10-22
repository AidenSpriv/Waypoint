package com.example.data.model

data class AiAnalysisResult(
    val suggestedCategory: BarrierCategory,
    val neutralSummary: String,
    val uncertaintyNotice: String,
    val suggestedPriority: ReviewPriority,
    val priorityRationale: String,
    val isDemoFallback: Boolean = false
)
