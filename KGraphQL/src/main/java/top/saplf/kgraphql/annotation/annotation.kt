package top.saplf.kgraphql.annotation

import java.lang.annotation.RetentionPolicy

/**
 * @author saplf
 */

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY)
annotation class KGraphQLName(val name: String)

annotation class KGraphQLArg

annotation class KGraphOperation