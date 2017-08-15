package top.saplf.kgraphql.dsl

/**
 * @author saplf
 */
class QueryNameFirst(
    tagArgs: TagArgs = null,
    identifier: String = "frag${System.nanoTime()}"
) : Tag(tagName = "First", tagArgs = tagArgs, identifier = identifier)

class QueryNameSecond(
    identifier: String = "frag${System.nanoTime()}"
) : Tag(tagName = "Second", identifier = identifier)

class QueryName(
    tagArgs: TagArgs = null,
    identifier: String = "frag${System.nanoTime()}",
    init: (QueryName.() -> Unit)? = null
) : BlockTag(tagName = "Name", tagArgs = tagArgs, identifier = identifier) {

  init {
    if (init != null) {
      init.invoke(this)
      treatAsFragment = true
    }
  }

  private val keyQueryNameFirstList = "list"

  fun first(list: List<*>? = null) {
    appendTag(QueryNameFirst(mapOf(keyQueryNameFirstList to list)), "first")
  }

  fun second() {
    appendTag(QueryNameSecond(), "second")
  }

  infix fun on(fragment: BlockTag) {
    appendFragment(fragment, false)
  }

  infix fun flat(fragment: BlockTag) {
    appendFragment(fragment, true)
  }
}

class Query(
    tagArgs: TagArgs = null,
    identifier: String = "frag${System.nanoTime()}"
) : BlockTag(tagName = "Query", tagArgs = tagArgs, identifier = identifier) {

  fun name(age: Int? = null, gender: String? = null, execution: QueryName.() -> Unit) {
    appendTag(tag = QueryName(mapOf(
        "age" to age,
        "gender" to gender
    )), fieldName = "name", fieldAlias = "nameAlias", execution = execution)
  }

  @Deprecated(message = "wrong scope", replaceWith = ReplaceWith("Unit"), level = DeprecationLevel.ERROR)
  fun query(execution: Query.() -> Unit) = Unit
}

class GithubQueryBlock(initBlock: (GithubQueryBlock.() -> Unit)?) {

  private var query: Query? = null
  var result: String? = null
  val fragment: BlockTag.() -> Unit

  init {
    fragment = {
      val queryDump = query ?: throw RuntimeException("GraphQL's fragment should define in lazy block")
      queryDump.defineFragment(this)
    }
    initBlock?.invoke(this)
  }

  fun query(id: String = "", execution: Query.() -> Unit) {
    if (null != query) {
      throw RuntimeException("You can't invoke query twice in one block.")
    }
    query = Query(mapOf("id" to id, "list" to listOf(1, 2, 3)))
    val queryDump = query ?: throw RuntimeException("Some thing must be wrong.")
    queryDump.apply(execution) // this must invoke after `query` is assigned as the `execution` will use `query`
    result = queryDump.generate()
  }
}
