package com.modutrap.models

import kotlinx.serialization.Serializable

/** A single Fast Flag key/value pair, always stored as a string value. */
@Serializable
data class FFlagEntry(val key: String, val value: String)
