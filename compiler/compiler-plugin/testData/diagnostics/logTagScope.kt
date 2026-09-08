// RUN_PIPELINE_TILL: FRONTEND

package foo.bar

import com.cmgapps.LogTag

@LogTag
fun tagged(): String = LOG_TAG

fun untagged(): String = <!LOG_TAG_OUTSIDE_ANNOTATED_SCOPE!>LOG_TAG<!>

@LogTag
class TaggedClass {
    fun member(): String = LOG_TAG

    fun nested(): String {
        fun local(): String = LOG_TAG
        return local()
    }
}

fun userDefined(): String {
    val LOG_TAG = "user-defined"
    return LOG_TAG
}
