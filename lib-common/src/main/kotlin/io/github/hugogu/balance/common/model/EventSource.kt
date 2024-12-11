package io.github.hugogu.balance.common.model

import com.fasterxml.jackson.annotation.JsonProperty

data class EventSource(
    val version: String = "",
    val connector: String = "",
    val name: String = "",
    @JsonProperty("ts_ms")
    val timestampMS: Long = 0L,
    @JsonProperty("ts_us")
    val timestampUS: Long = 0L,
    @JsonProperty("ts_ns")
    val timestampNS: Long = 0L,
    val snapshot: String = "false",
    val schema: String = "",
    @JsonProperty("db")
    val dbName: String = "",
    val table: String = "",
    val txId: Int = 0,
    val lsn: Long = 0L,
    val sequences: List<String> = emptyList(),
)
