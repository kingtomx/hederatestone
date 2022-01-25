package org.mvp.hedera.repository


import com.mongodb.BasicDBObject
import com.mongodb.client.result.DeleteResult
import org.mvp.hedera.commons.CriteriaHelper
import org.mvp.hedera.dtos.AggregationDto
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.aggregation.*
import org.springframework.data.mongodb.core.findAndModify
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*


@Component
class ReactiveBusinessRepository {

    @Autowired
    lateinit var reactiveMongoOperations: ReactiveMongoOperations

    private val logger = mu.KotlinLogging.logger {}

    fun getReactiveMongo(): ReactiveMongoOperations {
        return this.reactiveMongoOperations
    }

    fun setReactiveMongo(reactiveMongoOperations: ReactiveMongoOperations) {
        this.reactiveMongoOperations = reactiveMongoOperations
    }


    fun create(collection: String, data: Map<String, Any>): Mono<Document> {
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'") // Quoted "Z" to indicate UTC, no timezone offset
        val nowAsISO = df.format(Date())
        val localDate : Date = df.parse(nowAsISO)
        return reactiveMongoOperations.save(Document(data).append("created", localDate), collection)
                .switchIfEmpty(Mono.empty())
    }

    fun createChange(collection: String, data: Map<String, Any>): Mono<Document> {
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'") // Quoted "Z" to indicate UTC, no timezone offset
        val nowAsISO = df.format(Date())
        val localDate : Date = df.parse(nowAsISO)
        return reactiveMongoOperations.save(Document(data).append("created", localDate), collection + "_changelog")
                .switchIfEmpty(Mono.empty())
    }


    fun save(collection: String, data: Map<String, Any>, filterFields: Map<String, Any>): Mono<Document> {
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'") // Quoted "Z" to indicate UTC, no timezone offset
        val nowAsISO = df.format(Date())
        val localDate : Date = df.parse(nowAsISO)
        val query = buildQuery(filterFields)
        val doc = Document(data)
        return findAndModify(collection, doc, query)
                .switchIfEmpty(reactiveMongoOperations.save(doc.append("created", localDate), collection))
    }

    fun update(collection: String, data: Map<String, Any>): Mono<Document> {
        return reactiveMongoOperations.save(Document(data).append("last_modified", Date()), collection)
                .switchIfEmpty(Mono.empty())

    }

    fun findAndModify(collection: String, data: Document, query: Query): Mono<Document> {
        val update = Update.fromDocument(data).currentDate("last_modified")
        return reactiveMongoOperations.findAndModify<Document>(
                query,
                update,
                FindAndModifyOptions().upsert(false).returnNew(true),
                collection
        ).switchIfEmpty(Mono.empty())
    }

    fun find(collection: String, query: Map<String, Any>): Mono<Document> {
        return reactiveMongoOperations.findOne(buildQuery(query), Document::class.java, collection)
                .switchIfEmpty(Mono.empty())
    }

    fun findAll(collection: String, query: Map<String, Any>): Flux<Document> {
        return reactiveMongoOperations.find(buildQuery(query), Document::class.java, collection)
                .switchIfEmpty(Mono.empty())
    }

    fun findAllBetweenDates(collection: String, criteriaDates: Criteria, requestParams: Map<String, Any>): Flux<Document> {

        val query: Query = buildQuery(requestParams)
        query.addCriteria(criteriaDates)

        return reactiveMongoOperations.find(query, Document::class.java, collection)
                .switchIfEmpty(Mono.empty())
    }

    fun findAllPaginated2(collection: String, query: Map<String, Any>, page: Pageable): Flux<Document> {
        var returnable: Flux<Document> = reactiveMongoOperations.tail(buildQuery(query).with(page), Document::class.java, collection)
        return returnable;
    }

    fun findAllPaginated(collection: String, query: Map<String, Any>, page: Pageable): Flux<Document> {
        return reactiveMongoOperations
                .find(buildQuery(query)
                    .with(page), Document::class.java, collection)
    }

    fun countToFlux(collection: String, query: Map<String, Any>): Mono<Long> {
        return reactiveMongoOperations.find(buildQuery(query), Document::class.java, collection).count()
    }

    fun findById(collection: String, id: String): Mono<Document> {
        return reactiveMongoOperations.findById(id, Document::class.java, collection)
                .switchIfEmpty(Mono.empty())
    }

    fun findAllByAggregationBetweenDates(collection: String, field: String, startDate: LocalDate, endDate: LocalDate): Flux<Document>{
        val betweenQuery: Aggregation = Aggregation.newAggregation(Aggregation.match(Criteria.where(field).gte(startDate).lte(endDate)))

        return reactiveMongoOperations.aggregate(betweenQuery, collection, Document::class.java)
    }

    fun findAllAggregated(aggregation: AggregationDto): Flux<BasicDBObject> {

        // MongoDb Aggregation example:
        // db.sivaUserInfo.aggregate([
        //    { $lookup: { from: "sivaUserRole", localField: "userId", foreignField: "userId", as: "userRole" } },
        //    { $unwind:"$userRole" },
        //    { $project:{"_id":true, "userId" : true, "phone" : true, "role" :"$userRole.role" } }

        val lookup: AggregationOperation = Aggregation.lookup(aggregation.lookup.from, aggregation.lookup.localField, aggregation.lookup.foreignField, aggregation.lookup.showAs)
        val unwind: AggregationOperation = Aggregation.unwind(aggregation.unwind)
        val projection: ProjectionOperation = Aggregation.project().andInclude(*aggregation.project.toTypedArray())
        val aggregationOp:Aggregation = Aggregation.newAggregation(lookup, unwind, projection)
        return reactiveMongoOperations.aggregate(aggregationOp, aggregation.collection, BasicDBObject::class.java)

    }

    fun findAllCustomAggregation(collection: String,
                                 matchCriteria: Criteria,
                                 groupFields:Array<String>,
                                 groupPushTarget: String,
                                 groupPushAs: String,
                                 projectFields:Array<String>
    ): Flux<BasicDBObject> {

        val match: MatchOperation = Aggregation.match(matchCriteria)

        val group: GroupOperation = Aggregation
            .group(Fields.fields(*groupFields))
            .push(groupPushTarget).`as`(groupPushAs)

        val customExpressionValueIn: AggregationExpression = AccumulatorOperators.Sum.sumOf(ArrayOperators.ArrayElemAt.arrayOf("\$"+groupPushAs).elementAt(0))
        val projection: ProjectionOperation = Aggregation
            .project(Fields.fields(*projectFields)
            ).and(customExpressionValueIn).`as`(groupPushAs)

        val aggregationOp:Aggregation = Aggregation.newAggregation(match, group, projection)

        return reactiveMongoOperations.aggregate(aggregationOp, collection, BasicDBObject::class.java)

    }

    fun findAllCustomAggregation(collection: String, aggregation: Aggregation): Flux<BasicDBObject> {
        return reactiveMongoOperations.aggregate(aggregation, collection, BasicDBObject::class.java)
    }


    fun deleteById(collection: String, query: Map<String, Any>) : Mono<DeleteResult> {
        return reactiveMongoOperations.remove(buildQuery(query), collection);
    }


    fun buildQuery(values: Map<String, Any>): Query {
        logger.info { "Building query: $values" }
        val query = Query()
        for ((key, value) in values) {
            if(Objects.isNull(value)){
                query.addCriteria(Criteria.where(key).`is`(value))
            } else if(key.contains("OR:")){
                query.addCriteria(CriteriaHelper.getOrCriteriaQuery(key,value))
            } else if(Objects.nonNull(value)){
                query.addCriteria(CriteriaHelper.getCriteriaQuery(key,value))
            }

        }
        return query
    }

}