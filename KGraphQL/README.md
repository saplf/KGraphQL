# Annotation

- `KGraphQLName("query_name")` 定义实体类时，指定类型系统 field 名

- `KGraphQLOperation("operation_name")` 定义查询语句的方法时，指定操作名称

- `KGraphQLArg("arg")` 定义操作方法时，指定服务端的参数名称


```kotlin
@KGraphQLQuery
interface GitHubQuery {
    fun search(): Observable<UserInfo>
}
```

```kotlin
// generated code
object GitHubQueryImpl {
    fun search()
}
```