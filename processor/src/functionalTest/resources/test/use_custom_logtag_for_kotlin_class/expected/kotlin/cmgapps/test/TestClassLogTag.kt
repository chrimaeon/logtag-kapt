@file:Suppress(
  "SpellCheckingInspection",
  "RedundantVisibilityModifier",
  "unused",
)

package cmgapps.test

import kotlin.String
import kotlin.Suppress

public inline val TestClass.LOG_TAG: String
  get() = "MyCustomTag"
