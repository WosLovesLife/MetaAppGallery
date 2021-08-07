package com.example.metaappgallery.pojo

data class PagingResponse<T>(
    val adstar: Int,
    val boxresult: Any?,
    val ceg: String,
    val cn: Int,
    val cuben: Int,
    val end: Boolean,
    val gn: Int,
    val kn: Int,
    val lastindex: Int,
    val list: List<T>,
    val manun: Int,
    val pc: Int,
    val pornn: Int,
    val prevsn: Int,
    val ps: Int,
    val ran: Int,
    val ras: Int,
    val sid: String,
    val total: Int,
    val wordguess: Any?
)