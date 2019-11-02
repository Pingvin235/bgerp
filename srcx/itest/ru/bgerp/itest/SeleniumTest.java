package ru.bgerp.itest;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

@Test (groups = "selenium", dependsOnGroups = "runServer")
public class SeleniumTest {
    private static final Logger log = Logger.getLogger(SeleniumTest.class);

    @Test
    public void init() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox");
        ChromeDriver driver = new ChromeDriver(options);

        driver.get("http://www.google.com");
        
        WebDriverWait wait = new WebDriverWait(driver, 15);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//body")));

        WebElement el = driver.findElement(By.xpath("//body"));
        log.info(el);
        driver.close();
    }
}
