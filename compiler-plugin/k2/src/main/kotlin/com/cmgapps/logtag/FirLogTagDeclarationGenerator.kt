/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag

import com.cmgapps.logtag.LogTagPluginNames.LOG_TAG_ANNOTATION_ID
import com.cmgapps.logtag.LogTagPluginNames.LOG_TAG_PROPERTY_NAME
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.getStringArgument
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.createMemberProperty
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

class FirLogTagDeclarationGenerator(
    session: FirSession,
    private val messageCollector: MessageCollector,
) : FirDeclarationGenerationExtension(session) {
    private val predicate = LookupPredicate.create { annotated(LOG_TAG_ANNOTATION_ID.asSingleFqName()) }

    private val key: GeneratedDeclarationKey
        get() = LogTagPluginKey

    private val matchedClasses by lazy {
        session.predicateBasedProvider
            .getSymbolsByPredicate(predicate)
            .filterIsInstance<FirRegularClassSymbol>()
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(predicate)
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext,
    ): Set<Name> = setOf(LOG_TAG_PROPERTY_NAME)

    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirPropertySymbol> {
        val owner = context?.owner ?: return emptyList()
        if (!checkLogTagClassSymbols(owner, session) { it in matchedClasses }) return emptyList()

        require(owner is FirRegularClassSymbol) { "Owner must be a class symbol" }

        val overriddenName =
            owner.annotations
                .find { it.fqName(session) == LOG_TAG_ANNOTATION_ID.asSingleFqName() }
                ?.getStringArgument(Name.identifier("value"), session)

        val className = overriddenName ?: owner.name.asString()

        messageCollector.report(
            CompilerMessageSeverity.INFO,
            "Generating property $LOG_TAG_PROPERTY_NAME for $callableId with value $className",
        )

        val property =
            when (callableId.callableName) {
                LOG_TAG_PROPERTY_NAME ->
                    createMemberProperty(
                        owner,
                        key,
                        LOG_TAG_PROPERTY_NAME,
                        session.builtinTypes.stringType.coneType,
                    ) {
                        visibility = Visibilities.Private
                    }.apply {
                        replaceAnnotations(owner.annotations)
                    }

                else -> null
            } ?: return emptyList()

        return listOf(property.symbol)
    }

    private inline fun checkLogTagClassSymbols(
        symbol: FirClassSymbol<*>,
        session: FirSession,
        predicate: (FirClassSymbol<*>) -> Boolean,
    ): Boolean {
        if (predicate(symbol)) return true
        return symbol.resolvedSuperTypeRefs.any { superTypeRef ->
            val superTypeSymbol =
                superTypeRef.coneType.toRegularClassSymbol(session)
                    ?: return@any false
            predicate(superTypeSymbol)
        }
    }
}
