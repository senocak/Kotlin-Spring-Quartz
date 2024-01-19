package com.github.senocak.util

import com.github.senocak.util.AppConstants.EXPECTED_CONDITION_TIMEOUT
import com.github.senocak.util.AppConstants.EXPECTED_CONDITION_POLLING_INTERVAL
import com.github.senocak.util.AppConstants.logger
import java.time.Duration
import java.util.regex.Pattern
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.FluentWait
import org.openqa.selenium.support.ui.Wait
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.Logger

class Assert {
    private val log: Logger by logger()

    /**
     * This method is used to assert the text to be present in the element.
     * @param errorMessage - Error message to be displayed if the element is not present in the DOM.
     * @param textToFind - Text to be present in the element.
     */
    fun userSeesTextInSource(driver: WebDriver, errorMessage: String, textToFind: String) {
        waitUntil(driver = driver, errorMessage = errorMessage) {
            d: WebDriver -> d.pageSource.contains(other = textToFind)
        }
    }

    /**
     * This method is used to wait until the element is present in the DOM.
     * @param ec - ExpectedCondition
     * @param errorMessage - Error message to be displayed if the element is not present in the DOM.
     */
    @Throws(Exception::class)
    private fun waitUntil(driver: WebDriver, errorMessage: String, ec: ExpectedCondition<Boolean>) {
        try {
            WebDriverWait(driver, Duration.ofSeconds(EXPECTED_CONDITION_TIMEOUT))
                .ignoring(ClassCastException::class.java)
                .pollingEvery(Duration.ofSeconds(EXPECTED_CONDITION_POLLING_INTERVAL))
                .ignoring(WebDriverException::class.java)
                .withMessage(errorMessage)
                .until<Boolean>(ec)
        } catch (e: Exception) {
            log.error("Error in waitUntil method: ${e.message}")
            throw e
        }
    }

    /**
     * This method is used to wait until the element is present in the DOM.
     * @param ec - ExpectedCondition
     * @param errorMessage - Error message to be displayed if the element is not present in the DOM.
     */
    @Throws(Exception::class)
    private fun waitWebElementUntil(driver: WebDriver, errorMessage: String, ec: ExpectedCondition<WebElement>) {
        try {
            WebDriverWait(driver, Duration.ofSeconds(EXPECTED_CONDITION_TIMEOUT))
                .ignoring(ClassCastException::class.java)
                .pollingEvery(Duration.ofSeconds(EXPECTED_CONDITION_POLLING_INTERVAL))
                .ignoring(WebDriverException::class.java)
                .withMessage(errorMessage)
                .until<WebElement>(ec)
        } catch (e: Exception) {
            log.error("Error in waitUntil method: ${e.message}")
            throw e
        }
    }

    /**
     * This method is used to assert the text to be present in the element.
     * @param errorMessage - Error message to be displayed if the element is not present in the DOM.
     * @param by - By locator
     * @param textExpected - Text to be present in the element.
     */
    fun assertTextToBePresentInElementLocated(driver: WebDriver, errorMessage: String, by: By, textExpected: String) =
        waitUntil(driver = driver, ec = ExpectedConditions.textToBePresentInElementLocated(by, textExpected), errorMessage = errorMessage)

    /**
     * This method is used to assert the text to be present in the element.
     * @param errorMessage - Error message to be displayed if the element is not present in the DOM.
     * @param webElement - WebElement
     * @param textExpected  - Text to be present in the element.
     */
    fun assertTextToBePresentInElement(driver: WebDriver, errorMessage: String, webElement: WebElement, textExpected: String) =
        waitUntil(driver = driver, ec = ExpectedConditions.textToBePresentInElement(webElement, textExpected), errorMessage = errorMessage)

    /**
     * This method is used to assert the text to be present in the element value.
     * @param errorMessage - Error message to be displayed if the element is not present in the DOM.
     * @param by - By locator
     * @param textExpected - Text to be present in the element value.
     */
    fun assertTextToBePresentInElementValue(driver: WebDriver, errorMessage: String, by: By, textExpected: String) =
        waitUntil(driver = driver, ec = ExpectedConditions.textToBePresentInElementValue(by, textExpected), errorMessage = errorMessage)

    /**
     * This method is used to assert the attribute to be present in the element.
     * @param errorMessage - Error message to be displayed if the element is not present in the DOM.
     * @param by - By locator
     * @param attribute - Attribute to be present in the element.
     * @param value - Value of the attribute.
     */
    fun assertAttributeContains(driver: WebDriver, errorMessage: String, by: By, attribute: String, value: String) =
        waitUntil(driver = driver, ec = ExpectedConditions.attributeContains(by, attribute, value), errorMessage = errorMessage)

    /**
     * This method is used to assert the attributes to be present in the element.
     * @param errorMessage - Error message to be displayed if the element is not present in the DOM.
     * @param by - By locator
     * @param attribute - Attribute to be present in the element.
     * @param value - Value of the attribute.
     * @param timeout   - Timeout in seconds.
     */
    fun assertAttributeContains(driver: WebDriver, errorMessage: String, by: By, attribute: String, value: String, timeout: Long) =
        WebDriverWait(driver, Duration.ofSeconds(timeout))
            .ignoring(ClassCastException::class.java)
            .withMessage(errorMessage)
            .until<Boolean>(ExpectedConditions.attributeContains(by, attribute, value))

    /**
     * This method is used to assert the text to be matched in the element.
     * @param errorMessage - Error message to be displayed if the element is not present in the DOM.
     * @param by - By locator
     * @param pattern - Pattern to be matched in the element.
     */
    fun assertTextMatches(driver: WebDriver, errorMessage: String, by: By, pattern: Pattern) =
        waitUntil(driver = driver, ec = ExpectedConditions.textMatches(by, pattern), errorMessage = errorMessage)

    /**
     * This method is used to assert the js return value.
     * @param errorMessage - Error message to be displayed if the element is not present in the DOM.
     * @param javascript - Javascript to be executed.
     */
    fun assertJSReturnsTrue(driver: WebDriver, errorMessage: String, javascript: String) {
        val wait: Wait<WebDriver> = FluentWait<WebDriver>(driver)
            .withTimeout(Duration.ofSeconds(EXPECTED_CONDITION_TIMEOUT))
            .pollingEvery(Duration.ofSeconds(EXPECTED_CONDITION_POLLING_INTERVAL))
            .withMessage(errorMessage)
            .ignoring(NoSuchElementException::class.java)
        wait.until { _driver: WebDriver ->
            val js = (_driver as JavascriptExecutor)
            js.executeScript(javascript) as Boolean
        }
    }

    /**
     * This method is used to assert the js return value and text matches.
     * @param errorMessage - Error message to be displayed if the element is not present in the DOM.
     * @param javascript - Javascript to be executed.
     * @param textExpected - Text to be matched in the element.
     */
    fun assertJSReturnsValueAndTextMatches(driver: WebDriver, errorMessage: String, javascript: String, textExpected: String) : Boolean =
    (WebDriverWait(driver, Duration.ofSeconds(EXPECTED_CONDITION_TIMEOUT))
            .ignoring(ClassCastException::class.java)
            .withMessage(errorMessage)
            .until<Any>(ExpectedConditions.jsReturnsValue(javascript)) as String)
        .run { this === textExpected }

    /**
     * This method is used to assert the element to be visible.
     * @param errorMessage - Error message to be displayed if the element is not present in the DOM.
     * @param by - By locator
     */
    fun assertElementVisible(driver: WebDriver, errorMessage: String, by: By) =
        waitWebElementUntil(driver = driver, ec = ExpectedConditions.visibilityOfElementLocated(by), errorMessage = errorMessage)

    /**
     * This method is used to assert the element to be visible.
     * @param errorMessage - Error message to be displayed if the element is not present in the DOM.
     * @param el - WebElement
     */
    fun assertElementVisible(driver: WebDriver, errorMessage: String, el: WebElement) {
        waitWebElementUntil(driver = driver, ec = ExpectedConditions.visibilityOf(el), errorMessage = errorMessage)
    }

    /**
     * This method is used to assert the element to be visible.
     * @param errorMessage - Error message to be displayed if the element is not present in the DOM.
     * @param by - By locator
     */
    fun assertElementInvisible(driver: WebDriver, errorMessage: String, by: By) {
        waitUntil(driver = driver, ec = ExpectedConditions.invisibilityOfElementLocated(by), errorMessage = errorMessage)
    }
}
