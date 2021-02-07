/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmgapps.kotlin

import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.invoke
import arrow.meta.quotes.Transform
import arrow.meta.quotes.dotQualifiedExpression

val Meta.surroundWithBuildConfig: CliPlugin
    get() = "Surround Log call with BuildConfig" {
        meta(
            dotQualifiedExpression(
                this,
                {
                    messageCollector?.log(this.receiverExpression::class.java.canonicalName)
                    true
                }
            ) { expression ->
                messageCollector?.log(expression.toString())
                Transform.replace(
                    replacing = expression,
                    newDeclaration = identity()
                )
            }
        )
    }

fun org.jetbrains.kotlin.cli.common.messages.MessageCollector.log(msg: String) =
    report(org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.INFO, msg)
