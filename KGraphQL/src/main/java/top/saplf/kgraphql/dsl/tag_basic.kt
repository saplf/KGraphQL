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
 * @property tagName 标签名
 * @property tagArgs 标签参数名称及值
 * @property treatAsFragment 该 [Tag] 是否应该被视为 Fragment
 * @property identifier [Tag] 唯一标识
 */
abstract class Tag(val tagName: String,
                   protected val tagArgs: TagArgs = null,
                   var treatAsFragment: Boolean = false,
                   val identifier: String
) {

  /**
   * @param level 表示当前 [Tag] 是整个表达式的第几层，用于格式化输出时打印空格
   * @param fieldName 该 [Tag] 在父级 [BlockTag] 中的 field 名
   * @param fieldAlias 该 [Tag] 在父级 [BlockTag] 中的别名
   * @param defineFragment 表示是否是表示 Fragment 定义
   */
  open fun generateQuery(level: Int, fieldName: String, fieldAlias: String?, defineFragment: Boolean = false): String {
    return if (format) generateQueryFormat(level, fieldName, fieldAlias) else generateQueryCompress(level, fieldName, fieldAlias)
  }

  /**
   * 格式化输出的生成
   * @see generateQuery
   */
  protected open fun generateQueryFormat(level: Int, fieldName: String, fieldAlias: String?, defineFragment: Boolean = false): String {
    val currentPrev = if (level == 0) "" else "\n"
    val currentTab = " ".repeat(tabSize * level)
    val currentAlias = if (fieldAlias.isNullOrEmpty()) "" else "$fieldAlias: "
    return "$currentPrev$currentTab$currentAlias$fieldName${generateArgsString(true)}"
  }

  /**
   * 压缩输出
   * @see generateQuery
   */
  protected open fun generateQueryCompress(level: Int, fieldName: String, fieldAlias: String?, defineFragment: Boolean = false): String {
    val currentAlias = if (fieldAlias.isNullOrEmpty()) "" else "$fieldAlias:"
    return "$currentAlias$fieldName${generateArgsString(false)} "
  }

  /**
   * 生成参数输出
   * @param format 是否格式化输出
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
 * 有标签体的标签
 * @property pureTag `true` 表示没有标签体，`false` 表示有标签体
 * @see Tag
 */
abstract class BlockTag(
    tagName: String,
    tagArgs: TagArgs,
    protected val pureTag: Boolean = false,
    identifier: String
) : Tag(tagName = tagName, tagArgs = tagArgs, identifier = identifier) {

  /**
   * subField list
   */
  private val childTags: MutableList<Triple<String, String, Tag>> by lazy { mutableListOf<Triple<String, String, Tag>>() }

  /**
   * 定义的 fragments
   */
  private val definedFragments: MutableList<BlockTag> by lazy { mutableListOf<BlockTag>() }

  private var fragmentNeedFlat = true

  /**
   * @param defineFragment 是否是定义 fragment
   * @return 生成的查询字符串
   */
  fun generate(defineFragment: Boolean = false): String {
    return generateQuery(0, "query", null, defineFragment) + generateFragmentDefine()
  }

  fun generateFragmentDefine(): String {
    return StringBuilder().apply {
      definedFragments.forEach { append("\nfragment ${it.identifier} on ${it.generateQuery(0, it.tagName, null, true)}") }
    }.toString()
  }

  /**
   * 生成
   *
   * ```
   * query {
   *  ...
   * }
   * ```
   *
   * 块
   */
  override fun generateQuery(level: Int, fieldName: String, fieldAlias: String?, defineFragment: Boolean): String {
    return if (format) generateQueryFormat(level, fieldName, fieldAlias, defineFragment)
    else generateQueryCompress(level, fieldName, fieldAlias, defineFragment)
  }

  override fun generateQueryFormat(level: Int, fieldName: String, fieldAlias: String?, defineFragment: Boolean): String {
    val currentPrev = if (level == 0) "" else "\n"
    val currentTab = " ".repeat(tabSize * level)

    // treat as fragment to flat
    if (!defineFragment && treatAsFragment) {
      return if (fragmentNeedFlat) "$currentPrev$currentTab...$identifier"
      else "$currentPrev$currentTab...on $tagName {${generateBodyString(level)}\n$currentTab}"
    }

    val bodyString = generateBodyString(level)
    val currentBody = if (bodyString.isEmpty()) throw RuntimeException("Field with sub fields can't be empty body.") else " {$bodyString\n$currentTab}"
    val currentAlias = if (fieldAlias.isNullOrEmpty()) "" else "$fieldAlias: "
    return "$currentPrev$currentTab$currentAlias$fieldName${generateArgsString(true)}$currentBody"
  }

  override fun generateQueryCompress(level: Int, fieldName: String, fieldAlias: String?, defineFragment: Boolean): String {
    if (!defineFragment && treatAsFragment) {
      return if (fragmentNeedFlat) "...$identifier"
      else "...on $tagName{${generateBodyString(level)}}"
    }

    val bodyString = generateBodyString(level)
    val currentBody = if (bodyString.isEmpty()) throw RuntimeException("Field with sub fields can't be empty body.") else " {$bodyString}"
    val currentAlias = if (fieldAlias.isNullOrEmpty()) "" else "$fieldAlias:"
    return "$currentAlias$fieldName${generateArgsString(false)}$currentBody"
  }

  /**
   * 生成子节点的输出
   */
  private fun generateBodyString(level: Int): String
      = StringBuilder().apply {
    childTags
        .forEach { append(it.third.generateQuery(level + 1, it.first, it.second)) }
  }.toString()

  /**
   * 添加子节点
   * @param T 子节点的类型
   * @param tag 子节点
   * @param fieldName  该 [Tag] 在父级 [BlockTag] 中的 field 名
   * @param execution [tag] 需要进行的初始化操作
   */
  protected fun <T> appendTag(tag: T, fieldName: String, fieldAlias: String? = null, execution: T.() -> Unit = {}) where T : Tag {
    if (tag is BlockTag) {
      tag.fragmentNeedFlat = true
    }
    childTags.add(Triple(fieldName, fieldAlias ?: "", tag.apply(execution)))
  }

  /**
   * 添加插入的 fragment
   * @see appendTag
   */
  protected fun <T> appendFragment(fragment: T, needFlat: Boolean, execution: T.() -> Unit = {}) where T : BlockTag {
    fragment.fragmentNeedFlat = needFlat
    childTags.add(Triple("", "", fragment.apply(execution)))
  }

  /**
   * 定义 fragment
   */
  fun defineFragment(fragment: BlockTag) {
    definedFragments.add(fragment)
  }
}
