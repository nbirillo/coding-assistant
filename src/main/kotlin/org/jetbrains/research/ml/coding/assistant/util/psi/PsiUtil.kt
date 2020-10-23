/*
 * Copyright (c) 2020.  Anastasiia Birillo
 */

package org.jetbrains.research.ml.coding.assistant.util.psi

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement

val PsiElement.label: String
    get() = ApplicationManager.getApplication().runReadAction<String> {
        if (this.children.isEmpty()) this.text else ""
    }
