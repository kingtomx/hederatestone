package org.mvp.hedera.dtos

class AggregationDto(val collection: String, val lookup: Lookup, val unwind: String, val project: List<String>) {
    init {}
}

class Lookup(val from: String, val localField: String, val foreignField: String, val showAs: String) {
    init {}
}