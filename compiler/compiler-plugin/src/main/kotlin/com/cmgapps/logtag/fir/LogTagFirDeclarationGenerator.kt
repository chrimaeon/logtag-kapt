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
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.NestedClassGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.createCompanionObject
import org.jetbrains.kotlin.fir.plugin.createDefaultPrivateConstructor
import org.jetbrains.kotlin.fir.plugin.createMemberProperty
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

internal class LogTagFirDeclarationGenerator(
    session: FirSession,
    private val messageCollector: MessageCollector,
) : FirDeclarationGenerationExtension(session) {
    private val predicate = LookupPredicate.create { annotated(LOG_TAG_ANNOTATION_FQ_NAME) }

    private val key: GeneratedDeclarationKey = LogTagPluginKey

    private val matchedClasses by lazy {
        session.predicateBasedProvider
            .getSymbolsByPredicate(predicate)
            .filterIsInstance<FirRegularClassSymbol>()
    }

    private val companionOwners: Map<ClassId, FirRegularClassSymbol> by lazy {
        matchedClasses.associateBy {
            it.classId.createNestedClassId(SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT)
        }
    }

    private val FirClassSymbol<*>.isOurs: Boolean
        get() = (origin as? FirDeclarationOrigin.Plugin)?.key == LogTagPluginKey

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(predicate)
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext,
    ): Set<Name> {
        if (classSymbol.classId !in companionOwners) return emptySet()
        return buildSet {
            add(LOG_TAG_PROPERTY_NAME)
            if (classSymbol.isOurs) add(SpecialNames.INIT)
        }
    }

    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirPropertySymbol> {
        val owner = context?.owner ?: return emptyList()

        messageCollector.report(
            CompilerMessageSeverity.INFO,
            "Declaring property $LOG_TAG_PROPERTY_NAME on ${owner.classId.asSingleFqName()}",
        )

        val property =
            when (callableId.callableName) {
                LOG_TAG_PROPERTY_NAME -> {
                    createMemberProperty(
                        owner = owner,
                        key = key,
                        name = LOG_TAG_PROPERTY_NAME,
                        returnType = session.builtinTypes.stringType.coneType,
                        isVal = true,
                        hasBackingField = false,
                    ) {
                        visibility = Visibilities.Private
                    }
                }

                else -> {
                    null
                }
            } ?: return emptyList()

        return listOf(property.symbol)
    }

    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext,
    ): FirClassLikeSymbol<*>? =
        when {
            name != SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT -> null
            owner !in matchedClasses -> null
            else -> createCompanionObject(owner, key).symbol
        }

    @OptIn(SymbolInternals::class)
    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext,
    ): Set<Name> {
        val regular = classSymbol as? FirRegularClassSymbol ?: return emptySet()
        return when {
            regular !in matchedClasses -> emptySet()
            regular.companionObjectSymbol != null -> emptySet()
            else -> setOf(SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT)
        }
    }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
        val owner = context.owner
        if (!owner.isOurs) return emptyList()
        return listOf(createDefaultPrivateConstructor(owner, key).symbol)
    }
}
