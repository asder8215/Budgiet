package com.example.budgiet

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Currency

class PriceFieldTests {
    @Test
    fun validPriceFormatted() {
        val result = parsePrice("1", Currency.getInstance("USD")).getOkOrNull()
        assertEquals(result, "1.00")
    }

    @Test
    fun invalidCharInPrice() {
        val result = parsePrice("(100)", Currency.getInstance("USD")).getErrOrNull()!!.message
        assertEquals(result, "Invalid character '(' used")
    }

    @Test
    fun tooManyDecimalsPrice() {
        val result = parsePrice("1..0", Currency.getInstance("USD")).getErrOrNull()!!.message
        assertEquals(result, "Decimal '.' exists already")
    }

    @Test
    fun tooManyDecimalPlacesUSDPrice() {
        val result = parsePrice("1.000", Currency.getInstance("USD")).getErrOrNull()!!.message
        assertEquals(result, "USD uses up to 2 decimal places")
    }

    @Test
    fun tooManyDecimalPlacesBHDPrice() {
        val result = parsePrice("1.0000", Currency.getInstance("BHD")).getErrOrNull()!!.message
        assertEquals(result, "BHD uses up to 3 decimal places")
    }

    @Test
    fun leadingUnfractionalZeroPrice() {
        val result = parsePrice("0100", Currency.getInstance("USD")).getErrOrNull()!!.message
        assertEquals(result, "Leading un-fractional 0s are not allowed")
    }

    @Test
    fun invalidLocaleDecimalPrice() {
        // invalid for US based locales
        val result = parsePrice("1,00", Currency.getInstance("USD")).getErrOrNull()!!.message
        assertEquals(result, "Your locale uses '.' as decimal")
    }
}