package com.github.senocak.service

import com.github.senocak.util.AppConstants.DOCUMENT_READY_TIMEOUT
import com.github.senocak.util.AppConstants.EXPECTED_CONDITION_POLLING_INTERVAL
import com.github.senocak.util.AppConstants.EXPECTED_CONDITION_TIMEOUT
import com.github.senocak.util.AppConstants.USER_WAIT_IN_MS
import com.github.senocak.util.AppConstants.WEBDRIVER_TIME_OUT_IN_SECONDS
import com.github.senocak.util.AppConstants.logger
import io.github.bonigarcia.wdm.WebDriverManager
import java.time.Duration
import org.openqa.selenium.By
import org.openqa.selenium.ElementClickInterceptedException
import org.openqa.selenium.ElementNotInteractableException
import org.openqa.selenium.InvalidArgumentException
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.Keys
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.NotFoundException
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.safari.SafariDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.FluentWait
import org.openqa.selenium.support.ui.Wait
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.Logger

class Page (
    environment: String? = "local",
    headlessProperty: Boolean? = true
) {
    private val log: Logger by logger()
    private val driver: WebDriver

    init {
        driver = when (environment) {
            "local" -> createLocalDriver(headlessProperty = headlessProperty)
            else -> "Remote driver is not implemented yet"
                .apply { log.error(this) }
                .run { throw RuntimeException(this) }
        }
    }

    /**
     * Create local driver instance
     */
    private fun createLocalDriver(driverType: String? = "CHROME", headlessProperty: Boolean? = true): WebDriver {
        val driver: WebDriver

        val args: MutableList<String> = ArrayList()
        args.add("--disable-gpu")
        args.add("--disable-extensions")
        args.add("--no-sandbox")
        if (headlessProperty == true) {
            args.add("--headless")
        }
        val arguments = args.toTypedArray()

        log.info("Browser: $driverType, arguments: ${arguments.contentToString()}")
        when (driverType) {
            "CHROME" -> {
                WebDriverManager.chromedriver().setup()
                val chromeOptions = ChromeOptions()
                chromeOptions.addArguments(*arguments)
                driver = ChromeDriver(chromeOptions)
            }
            "FIREFOX" -> {
                WebDriverManager.firefoxdriver().setup()
                val firefoxOptions = FirefoxOptions()
                firefoxOptions.addArguments(*arguments)
                driver = FirefoxDriver(firefoxOptions)
            }
            "EDGE" -> {
                WebDriverManager.edgedriver().setup()
                val edgeOptions = EdgeOptions()
                edgeOptions.addArguments(*arguments)
                driver = EdgeDriver(edgeOptions)
            }
            "SAFARI" -> {
                WebDriverManager.safaridriver().setup()
                driver = SafariDriver()
            }
            else -> "Local driver is not handled"
                .apply { log.error(this) }
                .run { throw RuntimeException(this) }
        }
        val time = 10L
        driver.manage().deleteAllCookies()
        driver.manage().window().maximize()
        val duration: Duration = Duration.ofSeconds(time)
        driver.manage().timeouts().implicitlyWait(duration)
        driver.manage().timeouts().pageLoadTimeout(duration)
        driver.manage().timeouts().scriptTimeout(duration)
        return this.driver
    }

    /**
     * Redirect to the given url
     * @param url the url to redirect to
     */
    fun getUrl(url: String): Page = this.also { driver.get(url) }

    /**
     * Send keys to the given element
     * @param p the element to send keys to
     * @param t the keys to send
     * @param <P> the type of element
     * @param <T> the type of keys
    */
    @Throws(InvalidArgumentException::class)
    fun <P, T> sendKeys(p: P, t: T): Page {
        sleepms(time = USER_WAIT_IN_MS)
        if (p !is WebElement && p !is By)
            "${p.toString()} parameter type not supported by this function"
                .apply { log.error(this) }
                .apply { throw InvalidArgumentException(this) }
        val wait: Wait<WebDriver> = FluentWait<WebDriver>(driver)
            .withTimeout(Duration.ofSeconds(EXPECTED_CONDITION_TIMEOUT))
            .pollingEvery(Duration.ofSeconds(EXPECTED_CONDITION_POLLING_INTERVAL))
            .ignoring(ElementNotInteractableException::class.java)
            .ignoring(NoSuchElementException::class.java)
            .ignoring(StaleElementReferenceException::class.java)
            .ignoring(ElementClickInterceptedException::class.java)
        wait.until<WebElement> { _: WebDriver ->
            val el: WebElement = if (p is By) {
                getElement(by = p as By)
            } else {
                p as WebElement
            }
            log.info("************Typing on ************** Element: $el, Text: $t")
            clearInput(el = el)
            when (t) {
                is String -> el.sendKeys((t as String))
                is Keys -> el.sendKeys((t as Keys))
            }
            sleepms(time = USER_WAIT_IN_MS)
            el
        }
        return this
    }

    /**
     * Click on the given element
     * @param p the element to click on
     * @param <T> the type of element
    */
    @Throws(NotFoundException::class, InvalidArgumentException::class)
    fun <T> click(p: T): Page {
        val wait: Wait<WebDriver> = FluentWait<WebDriver>(driver)
            .withTimeout(Duration.ofSeconds(EXPECTED_CONDITION_TIMEOUT))
            .pollingEvery(Duration.ofSeconds(EXPECTED_CONDITION_POLLING_INTERVAL))
            .ignoring(ElementNotInteractableException::class.java)
            .ignoring(NoSuchElementException::class.java)
            .ignoring(StaleElementReferenceException::class.java)
            .ignoring(TimeoutException::class.java)
            .ignoring(WebDriverException::class.java)
            .ignoring(ElementClickInterceptedException::class.java)
        wait.until<WebElement> { driver: WebDriver ->
            val el: WebElement?
            when (p) {
                is By -> el = getElement(p as By)
                is WebElement -> el = p
                is String -> {
                    log.info("p is $p")
                    el = (driver as JavascriptExecutor?)!!.executeScript("return $p") as WebElement?
                    if (el == null) {
                        log.error("Element not found named $p")
                        throw NotFoundException("$p is null")
                    }
                }
                else -> {
                    log.error("Element not found named $p")
                    throw InvalidArgumentException("$p parameter type not supported by this function")
                }
            }
            WebDriverWait(driver, Duration.ofSeconds(WEBDRIVER_TIME_OUT_IN_SECONDS))
                .until<WebElement>(ExpectedConditions.elementToBeClickable(el))
            log.info("************Tapping on ************** $el")
            el.click()
            el
        }
        sleepms(time = USER_WAIT_IN_MS)
        return this
    }

    /**
     * Clear the input of the given element
     * @param el element to clear the input of
     */
    fun clearInput(el: WebElement): Page {
        el.clear()
        el.sendKeys(Keys.CONTROL.toString() + "a", Keys.DELETE)
        return this
    }

    /**
     * Check if the given element is checked
     * @param by the element to check
     * @return true if the element is checked
     */
    fun isChecked(by: By): Boolean = getVisibleElement(by).isSelected

    /**
     * Right-click on the given element
     * @param by the element to right-click on
     */
    fun rightClickOn(by: By): Page {
        val wait: Wait<WebDriver> = FluentWait<WebDriver>(driver)
            .withTimeout(Duration.ofSeconds(EXPECTED_CONDITION_TIMEOUT))
            .pollingEvery(Duration.ofSeconds(EXPECTED_CONDITION_POLLING_INTERVAL))
            .ignoring(ElementNotInteractableException::class.java)
            .ignoring(NoSuchElementException::class.java)
            .ignoring(StaleElementReferenceException::class.java)
            .ignoring(ElementClickInterceptedException::class.java)
            .ignoring(TimeoutException::class.java)
            .ignoring(WebDriverException::class.java)
        val link: WebElement = wait.until<WebElement> { driver: WebDriver ->
            val el: WebElement = getElement(by)
            WebDriverWait(driver, Duration.ofSeconds(WEBDRIVER_TIME_OUT_IN_SECONDS))
                .until<WebElement>(ExpectedConditions.elementToBeClickable(el))
            el
        }
        val action = Actions(driver)
        action.contextClick(link).perform()
        sleepms(time = USER_WAIT_IN_MS)
        return this
    }

    /**
     * Wait for the given element to be visible
     */
    @Throws(NotFoundException::class)
    fun waitUntilDocumentReady(): Page {
        log.info("checking if document is in ready state")
        val js = driver as JavascriptExecutor?
        var i = 0
        do {
            try {
                if (js!!.executeScript("return document.readyState") == "complete") {
                    log.info("document state is ready")
                    return this
                }
                log.info("****************waiting for page to be in ready state***********: $i")
                sleepms(time = USER_WAIT_IN_MS)
                i++
            } catch (e: WebDriverException) {
                log.info("WebDriverException: " + e.message)
            }
        } while (i < DOCUMENT_READY_TIMEOUT)
        throw NotFoundException("Document was not ready in $DOCUMENT_READY_TIMEOUT seconds. Check if application is debuggable")
    }

    /**
     * Get the web element for the given locator
     * @param by the locator to find the element
     * @return the web element
     */
    fun getElement(by: By): WebElement {
        val wait: Wait<WebDriver> = FluentWait<WebDriver>(driver)
            .withTimeout(Duration.ofSeconds(EXPECTED_CONDITION_TIMEOUT))
            .pollingEvery(Duration.ofSeconds(EXPECTED_CONDITION_POLLING_INTERVAL))
            .ignoring(NoSuchElementException::class.java)
            .ignoring(ElementNotInteractableException::class.java)
            .ignoring(StaleElementReferenceException::class.java)
            .ignoring(WebDriverException::class.java)
            .withMessage("$by element could not be found!")
        return wait.until { driver: WebDriver -> driver.findElement(by) }
    }

    /**
     * Get the text value of element for the given locator
     * @param by the locator to find the element
     * @return the value
     */
    fun getText(by: By): String = getElement(by = by).text

    /**
     * Get the visible web element for the given locator
     * @param by the locator to find the element
     * @return the web element
     */
    fun getVisibleElement(by: By): WebElement {
        val wait: Wait<WebDriver> = FluentWait<WebDriver>(driver)
            .withTimeout(Duration.ofSeconds(EXPECTED_CONDITION_TIMEOUT))
            .pollingEvery(Duration.ofSeconds(EXPECTED_CONDITION_POLLING_INTERVAL))
            .ignoring(NoSuchElementException::class.java)
            .ignoring(ElementNotInteractableException::class.java)
            .ignoring(StaleElementReferenceException::class.java)
            .ignoring(WebDriverException::class.java)
            .withMessage("$by element could not be found!")
        return wait.until<WebElement> { driver: WebDriver ->
            WebDriverWait(driver, Duration.ofSeconds(WEBDRIVER_TIME_OUT_IN_SECONDS))
                .until<WebElement>(ExpectedConditions.visibilityOfElementLocated(by))
            getElement(by)
        }
    }

    /**
     * Get the elements for the given locator
     * @param by the locator to find the elements
     * @return the web elements
     */
    fun getElements(by: By): List<WebElement> {
        val wait: Wait<WebDriver> = FluentWait<WebDriver>(driver)
            .withTimeout(Duration.ofSeconds(EXPECTED_CONDITION_TIMEOUT))
            .pollingEvery(Duration.ofSeconds(EXPECTED_CONDITION_POLLING_INTERVAL))
            .ignoring(NoSuchElementException::class.java)
            .ignoring(StaleElementReferenceException::class.java)
            .ignoring(WebDriverException::class.java)
            .ignoring(TimeoutException::class.java)
            .withMessage("$by element could not be found!")
        return wait.until { driver: WebDriver -> driver.findElements(by) }
    }

    /**
     * Get the visible web elements for the given locator by text contains
     * @param text the text to find the elements
     * @return the web elements
     */
    fun getElementByContainsText(text: String): WebElement =
        getElement(by = By.xpath("//*[contains(text(),'$text')]"))

    /**
     * Get the last visible web element for the given locator
     * @param by the locator to find the element
     * @return the web element
     */
    fun getLastElement(by: By): WebElement {
        val currentPageTitle: String = try {
            driver.title
        } catch (e: Exception) {
            "???"
        }
        log.info("Locating elements $by on page titled $currentPageTitle")
        val els: List<WebElement> = getElements(by)
        log.info("List size is " + els.size)
        return els[els.size - 1]
    }

    /**
     * Swipe to default content
     */
    fun defaultContent(): WebDriver =
        driver.switchTo().defaultContent()
            .also { log.info("Switching to default content") }

    /**
     * Swipe to the given frame
     * @param frameName the frame name
     */
    fun switchToFrame(frameName: String): WebDriver =
        driver.switchTo().frame(frameName)
            .also { log.info("Switching to frame $frameName") }

    /**
     * Swipe to the given frame
     * @param by the locator to find the frame
     */
    fun switchToFrame(by: By): WebDriver =
        WebDriverWait(driver, Duration.ofSeconds(WEBDRIVER_TIME_OUT_IN_SECONDS))
            .until<WebDriver>(ExpectedConditions.frameToBeAvailableAndSwitchToIt(by))

    /**
     * Sleep for the given time
     * @param time the time to sleep
     */
    fun sleepms(time: Long) {
        try {
            log.info("Sleeping for $time ms")
            Thread.sleep(time)
        } catch (e: InterruptedException) {
            log.error("InterruptedException: ${e.message}")
        }
    }

    /**
     * Verify if the given element is present
     * @param t the web element to verify
     * @return true if the element is present
     */
    fun <T> isDisplayed(t: T): Boolean =
        try {
            if (t is WebElement) {
                log.debug("Element is WebElement. Checking if it is displayed")
                (t as WebElement).isDisplayed
            } else if (t is By) {
                log.debug("Element is By. Checking if it is displayed")
                getElement(by = t as By).isDisplayed
            }
            "Unknown type of element"
                .also { log.error(it) }
                .run { throw NoSuchElementException(this) }
        } catch (e: NoSuchElementException) {
            false
        }
}
