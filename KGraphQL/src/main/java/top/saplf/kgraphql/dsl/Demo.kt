package top.saplf.kgraphql.dsl

/**
 * @author saplf
 */

class QueryNameFirst(
        tagArgs: TagArgs = null,
        identifier: String = "frag${System.nanoTime()}"
) : Tag(tagName = "first", tagArgs = tagArgs, identifier = identifier)

class QueryNameSecond(
        identifier: String = "frag${System.nanoTime()}"
) : Tag(tagName = "second", identifier = identifier)

class QueryName(
        tagArgs: TagArgs = null,
        identifier: String = "frag${System.nanoTime()}",
        init: (QueryName.() -> Unit)? = null
) : BlockTag(tagName = "name", tagArgs = tagArgs, identifier = identifier) {

    init {
        if (init != null) {
            init.invoke(this)
            treatAsFragment = true
        }
        init?.invoke(this)
    }

    private val keyQueryNameFirstList = "list"

    fun first(list: List<*>? = null) {
        appendTag(QueryNameFirst(mapOf(keyQueryNameFirstList to list)))
    }

    fun second() {
        appendTag(QueryNameSecond())
    }

    infix fun on(fragment: BlockTag) {
//        insertFragment(fragment.identifier ?: "${BlockTag::javaClass.name}${fragment.hashCode()}", fragment)
        appendTag(fragment)
    }

    @Deprecated(message = "不能在此使用", replaceWith = ReplaceWith("Unit"), level = DeprecationLevel.ERROR)
    fun name(age: Int? = null, gender: String? = null, execution: QueryName.() -> Unit) = Unit

    fun fragment() {

    }
}

class Query(
        tagArgs: TagArgs = null,
        identifier: String = "frag${System.nanoTime()}"
) : BlockTag(tagName = "query", tagArgs = tagArgs, identifier = identifier) {
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

class GithubQueryBlock(initBlock: (GithubQueryBlock.() -> Unit)?) {

    private var query: Query? = null
    var result: String? = null
    val fragment: BlockTag.() -> Unit

    init {
        fragment = {
            if (query == null) {
                throw RuntimeException("GraphQL's fragment should define in lazy block")
            }
            query?.defineFragment(this)
        }
        initBlock?.invoke(this)
    }

    fun query(id: String = "", execution: Query.() -> Unit) {
        if (null != query) {
            throw RuntimeException("You can't invoke query twice in one block.")
        }
        query = Query(mapOf("id" to id, "list" to listOf(1, 2, 3)))
        if (query == null) {
            throw RuntimeException("Some thing must be wrong.")
        }
        query?.apply(execution) // this must invoke after `query` is assigned as the `execution` will use `query`
        result = query?.generate()
    }
}

fun demo() {
    val result = GithubQueryBlock {
        val fragmentA by lazy {
            QueryName {
                first()
                second()
            }.apply(fragment)
        }

        query(id = "32") {
            name(age = 8) {
                second()
                first(list = listOf("ljc", 123))
            }
            name {
                first()
                this on fragmentA
                this on QueryName {

                }
            }
            name(age = 8, gender = "male") {
                first()
            }
        }
    }

    println(result.result)
}
