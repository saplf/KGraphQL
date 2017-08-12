package top.saplf.kgraphql.dsl

/**
 * @author saplf
 */

fun demo() {
    val result = GithubQueryBlock {
        val fragmentA by lazy {
            QueryName {
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
                this flat fragmentA
                this on QueryName {
                    first()
                }
            }
            name(age = 8, gender = "male") {
                first()
            }
        }
    }

    println(result.result)
}
