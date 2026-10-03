/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag.fir

import com.cmgapps.logtag.LOG_TAG_ANNOTATION_FQ_NAME
import com.cmgapps.logtag.LOG_TAG_PROPERTY_NAME
import com.cmgapps.logtag.LogTagPluginKey
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.createTopLevelProperty
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName

internal class LogTagFirDeclarationGenerator(
    session: FirSession,
) : FirDeclarationGenerationExtension(session) {
    private val predicate = LookupPredicate.create { annotated(LOG_TAG_ANNOTATION_FQ_NAME) }

    private val key: GeneratedDeclarationKey = LogTagPluginKey

    private val matchedClasses by lazy {
        session.predicateBasedProvider
            .getSymbolsByPredicate(predicate)
            .filterIsInstance<FirRegularClassSymbol>()
    }

    private val resolverPackages: Set<FqName> by lazy {
        buildSet {
            session.predicateBasedProvider
                .getSymbolsByPredicate(predicate)
                .filterIsInstance<FirNamedFunctionSymbol>()
                .mapTo(this) { it.callableId.packageName }
            matchedClasses.mapTo(this) { it.classId.packageFqName }
        }
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(predicate)
    }

    @OptIn(ExperimentalTopLevelDeclarationsGenerationApi::class)
    override fun getTopLevelCallableIds(): Set<CallableId> = resolverPackages.mapTo(linkedSetOf()) { CallableId(it, LOG_TAG_PROPERTY_NAME) }

    @OptIn(ExperimentalTopLevelDeclarationsGenerationApi::class)
    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirPropertySymbol> {
        if (context == null && callableId.classId == null) {
            if (callableId.callableName != LOG_TAG_PROPERTY_NAME || callableId.packageName !in resolverPackages) {
                return emptyList()
            }
            return listOf(
                createTopLevelProperty(
                    key = key,
                    callableId = callableId,
                    returnType = session.builtinTypes.stringType.coneType,
                    isVal = true,
                    hasBackingField = false,
                    containingFileName = "LogTagResolver",
                ).symbol,
            )
        }

        return emptyList()
    }
}
