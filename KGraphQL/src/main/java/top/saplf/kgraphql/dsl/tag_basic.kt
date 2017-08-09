package top.saplf.kgraphql.dsl

/**
 * @author saplf
 */

typealias TagArgs = Map<String, Any?>?

// 是否格式化输出
private var format = true

// tab 长度
private var tabSize = 2

/**
 * 标签超类
 * @param tagName 标签名
 * @param tagAlias 标签别名
 * @param tagArgs 标签参数名称及值
 */
abstract class Tag(val tagName: String,
                   val tagAlias: String? = null,
                   protected val tagArgs: TagArgs = null
) {
    open fun generate(level: Int): String {
        return if (format) generateFormat(level) else generateCompress(level)
    }

    protected open fun generateFormat(level: Int): String {
        val currentPrev = if (level == 0) "" else "\n"
        val currentTab = " ".repeat(tabSize * level)
        return "$currentPrev$currentTab$tagName${generateArgsString(true)}"
    }

    protected open fun generateCompress(level: Int): String {
        return "$tagName${generateArgsString(false)} "
    }

    /**
     * 生成参数输出
     */
    open protected fun generateArgsString(format: Boolean): String
            = StringBuilder().apply {
        val space = if (format) " " else ""
        tagArgs?.forEach { append(generateParamWith(it.key, it.value, space)) }
        if (length > 0) {
            delete(length - 1 - space.length, length)
            insert(0, '(')
            append(')')
        }
    }.toString()

    private fun generateParamWith(key: String, value: Any?, space: String): String
            = when (value) {
        null -> ""
        is CharSequence, is Char -> "$key:$space\"$value\",$space"
        is Byte, is Short, is Int, is Long,
        is Float, is Double, is Boolean, is Enum<*> -> "$key:$space$value,$space"
        is Iterable<*> -> "$key:$space[${value.map { generateListParamWith(it, space) }.joinToString(",$space")}],$space"
        else -> throw RuntimeException("Unexpected type ${value::class.java.name} for $key, only support value type & enum.")
    }

    private fun generateListParamWith(value: Any?, space: String): String
            = when (value) {
        null -> "null"
        is CharSequence, is Char -> "\"$value\""
        is Byte, is Short, is Int, is Long,
        is Float, is Double, is Boolean, is Enum<*> -> "$value"
        is Iterable<*> -> "[${value.map { generateListParamWith(it, space) }.joinToString(",$space")}]"
        else -> throw RuntimeException("Unexpected type ${value::class.java.name}, only support value type & enum.")
    }
}

/**
 *
 * @param pureTag `true` 表示没有标签体，`false` 表示有标签体
 */
abstract class BlockTag(
        tagName: String,
        tagArgs: TagArgs,
        protected val pureTag: Boolean = false
) : Tag(tagName = tagName, tagArgs = tagArgs) {
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
        return "$currentPrev$currentTab$tagName${generateArgsString(true)}$currentBody"
    }

    override fun generateCompress(level: Int): String {
        return "$tagName${generateArgsString(false)}{${generateBodyString(level)}}"
    }

    protected fun <T> appendTag(tag: T, execution: T.() -> Unit = {}) where T : Tag {
        childTags.add(tag.apply(execution))
    }

    /**
     * 生成子节点的输出
     */
    private fun generateBodyString(level: Int): String
            = StringBuilder().apply {
        childTags
                .filterIsInstance<Tag>()
                .forEach { append(it.generate(level + 1)) }
    }.toString()
}
