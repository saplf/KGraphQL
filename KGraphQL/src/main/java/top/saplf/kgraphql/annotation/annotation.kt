package top.saplf.kgraphql.annotation

/**
 * @author saplf
 */

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY)
annotation class KGraph

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY)
annotation class KGraphQLName(val name: String)

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY)
annotation class KGraphQLArg

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY)
annotation class KGraphOperation