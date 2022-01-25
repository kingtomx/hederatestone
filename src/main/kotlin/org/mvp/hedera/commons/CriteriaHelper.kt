package org.mvp.hedera.commons

import org.springframework.data.mongodb.core.query.Criteria
import java.util.ArrayList




object CriteriaHelper {
    const val VALUE_DELIMITER = ","
    const val KEY_DELIMITER = ":"

    fun getCriteriaQuery(key: String, value: Any): Criteria{
        val field = getField(key)

        //between filter dates format must be yyyyMMdd
        if(key.contains("BETWEEN:") && value.toString().contains(VALUE_DELIMITER)){
            val values = value.toString().split(VALUE_DELIMITER)
            return Criteria.where(field).gte(LocalDateParser.basicIsoParse(values[0])).lte(
                LocalDateParser.basicIsoParse(
                    values[1]
                )
            )
        }

        if(key.contains("NOT:")){
            return Criteria.where(field).ne(value)
        }

        if(key.contains("ISNULL:")){
            return Criteria.where(field).exists(value.toString().toBoolean())
        }

        if(key.contains("LIKE:")){
            return Criteria.where(field).regex(value.toString())
        }

        if(key.contains("NOT_IN:")){
            return Criteria.where(field).nin(value.toString().split(VALUE_DELIMITER))
        }

        if(key.contains("IN:")){
            return Criteria.where(field).`in`(value.toString().split(VALUE_DELIMITER))
        }

        return Criteria.where(key).`is`(value)
    }

    fun getOrCriteriaQuery(key: String, listValues: Any): Criteria{

        var criteria: Array<Criteria?>
        val orCriterias: MutableList<Criteria> = ArrayList()
        var fields: List<String> = key.replace("OR:", "").split(",")
        var i = 0;

        for (field in fields) {
            val values: List<String> = listValues.toString().split(",")
            var value = values[i]

            var criteria2 = getCriteriaQuery(field,value);
            orCriterias.add(criteria2)
            i++
        }

        criteria = orCriterias.toTypedArray()

        return Criteria().orOperator(*criteria)
    }

    private fun getField(key: String): String{
        return if(key.contains(KEY_DELIMITER)) key.split(KEY_DELIMITER)[1] else key
    }
}