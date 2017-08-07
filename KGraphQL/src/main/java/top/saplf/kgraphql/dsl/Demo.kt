package top.saplf.kgraphql.dsl

/**
 * @author saplf
 */

typealias TagArgs = Map<String, Any?>

private var format = true
private var tabSize = 2

abstract class Tag(val tagName: String, val pureTag: Boolean = true) {
    open fun generate(level: Int): String {
        return if (format) generateFormat(level) else generateCompress(level)
    }

    protected open fun generateFormat(level: Int): String {
        val currentPrev = if (level == 0) "" else "\n"
        val currentTab = " ".repeat(tabSize * level)
        return "$currentPrev$currentTab$tagName"
    }

    protected open fun generateCompress(level: Int): String {
        return "$tagName "
    }
}

abstract class BlockTag(
        tagName: String,
        private val tagArgs: TagArgs
) : Tag(tagName, false) {
    private val childTags: MutableList<Any> by lazy { mutableListOf<Any>() }

    fun generate(): String {
        return generate(0)
    }

    override fun generate(level: Int): String {
        return if (format) generateFormat(level) else generateCompress(level)
    }

    override fun generateFormat(level: Int): String {
        val currentPrev = if (level == 0) "" else "\n"
        val currentTab = " ".repeat(tabSize * level)

        val bodyString = generateBodyString(level)
        val currentBody = if (bodyString.isEmpty()) "{}" else " {$bodyString\n$currentTab}"
        return "$currentPrev$currentTab$tagName${generateFormatArgsString()}$currentBody"
    }

    override fun generateCompress(level: Int): String {
        return "$tagName${generateCompressArgsString()}{${generateBodyString(level)}}"
    }

    protected fun <T> appendTag(tag: T, execution: T.() -> Unit = {}) where T : Tag {
        childTags.add(tag.apply(execution))
    }

    private fun generateBodyString(level: Int): String
            = StringBuilder().apply {
        childTags
                .filterIsInstance<Tag>()
                .forEach { append(it.generate(level + 1)) }
    }.toString()

    private fun generateCompressArgsString(): String
            = StringBuilder().apply {
        tagArgs.forEach { if (it.value != null) { append("${it.key}:${it.value},") } }
        if (length > 0) {
            deleteCharAt(length - 1)
            insert(0, '(')
            append(')')
        }
    }.toString()

    private fun generateFormatArgsString(): String
            = StringBuilder().apply {
        tagArgs.forEach { if (it.value != null) { append("${it.key}: ${it.value}, ") } }
        if (length > 0) {
            delete(length - 2, length)
            insert(0, '(')
            append(')')
        }
    }.toString()
}

class QueryNameFirst : Tag("first")

class QueryNameSecond : Tag("second")

class QueryName(tagArgs: TagArgs = emptyMap()) : BlockTag("name", tagArgs) {
    fun first() {
        appendTag(QueryNameFirst())
    }

    fun second() {
        appendTag(QueryNameSecond())
    }
}

class Query(tagArgs: TagArgs = emptyMap()) : BlockTag("query", tagArgs) {
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
