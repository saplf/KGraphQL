package top.saplf.kgraphql.dsl

/**
 * @author saplf
 */

class QueryNameFirst : Tag("first")

class QueryNameSecond : Tag("second")

class QueryName(tagArgs: TagArgs = null) : BlockTag("name", tagArgs) {
    fun first() {
        appendTag(QueryNameFirst())
    }

    fun second() {
        appendTag(QueryNameSecond())
    }
}

class Query(tagArgs: TagArgs = null) : BlockTag("query", tagArgs) {
    private val keyQueryNameAge = "age"
    private val keyQueryNameGender = "gender"

    fun name(age: Int? = null, gender: String? = null, execution: QueryName.() -> Unit) {
        appendTag(QueryName(mapOf(
                keyQueryNameAge to age,
                keyQueryNameGender to gender
        )), execution)
    }

    @Deprecated(message = "不能在此使用", replaceWith = ReplaceWith("Unit"), level = DeprecationLevel.ERROR)
    fun query(execution: Query.() -> Unit) = Unit
}

fun query(id: String = "", execution: Query.() -> Unit): String {
    val query = Query(mapOf("id" to id)).apply(execution)
    return query.generate()
}

fun demo() {
    val result = query(id = "32") {
        name(8) {
            second()
            first()
        }
        name {
            first()
        }

        name(age = 8, gender = "male") {  }
    }
    println(result)
}
