package com.edernilson.ctr.interfaces.incoming.errorhandling

import org.springframework.stereotype.Component
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import java.util.*
import javax.servlet.http.HttpServletRequest

@Component
class LocaleResolver: AcceptHeaderLocaleResolver() {

    private val DEFAULT_LOCALE = Locale("pt", "BR")
    private val ACCEPTED_LOCALES = listOf(DEFAULT_LOCALE, Locale("en"))

    override fun resolveLocale(request: HttpServletRequest): Locale {
        val acceptLanguageHeader = request.getHeader("Accept-Language")
        if (acceptLanguageHeader.isNullOrEmpty() || acceptLanguageHeader.trim() == "*") {
            return DEFAULT_LOCALE
        }

        val list = Locale.LanguageRange.parse(acceptLanguageHeader)
        return Locale.lookup(list, ACCEPTED_LOCALES) ?: DEFAULT_LOCALE
    }
}