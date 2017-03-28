/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tests;

import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author martin
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testCars {

    private static final int WAIT_MAX = 4;
    static WebDriver driver;

    @BeforeClass
    public static void setup() {
        // local webdriver files
        // Mozilla gecko & Google Chrome
        System.setProperty("webdriver.gecko.driver", System.getProperty("user.dir") + "/geckodriver");
        System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "/chromedriver");

        //Reset Database
        com.jayway.restassured.RestAssured.given().get("http://localhost:3000/reset");

        driver = new ChromeDriver();
        driver.get("http://localhost:3000");
    }

    @AfterClass
    public static void tearDown() {
        driver.quit();
        //Reset Database 
        com.jayway.restassured.RestAssured.given().get("http://localhost:3000/reset");

    }

    @Test
    //Verify that page is loaded and all expected data are visible
    public void t1_verifyPageLoadExpectedData() throws Exception {
        (new WebDriverWait(driver, WAIT_MAX)).until((ExpectedCondition<Boolean>) (WebDriver d) -> {
            WebElement e = d.findElement(By.tagName("tbody"));
            List<WebElement> rows = e.findElements(By.tagName("tr"));
            Assert.assertThat(rows.size(), is(5));
            return true;
        });
    }

    @Test
    //Verify the filter functionality 
    public void t2_verifyFilterFunctionalityInput2002() throws Exception {
        //No need to WAIT, since we are running test in a fixed order, we know the DOM is ready (because of the wait in test1)
        WebElement filterbox = driver.findElement(By.id("filter"));

        filterbox.sendKeys("2002");
        WebElement e = driver.findElement(By.tagName("tbody"));
        List<WebElement> rows = e.findElements(By.tagName("tr"));

        // 2 fords should now be seen
        Assert.assertThat(rows.size(), is(2));

    }

    @Test
    //Verify data reset after clearing filter textbox
    public void t3_verifyFilterReset() throws Exception {
        WebElement filterbox = driver.findElement(By.id("filter"));

        // .clear() didn't work. So doubleclick + backspace.
        filterbox.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);

        WebElement e = driver.findElement(By.tagName("tbody"));
        List<WebElement> rows = e.findElements(By.tagName("tr"));

        // 2 fords should now be seen
        Assert.assertThat(rows.size(), is(5));
    }

    @Test
    //Verify Sort Function
    public void t4_verifySortFunction() throws Exception {
        WebElement yearSortButton = driver.findElement(By.id("h_year"));
        Assert.assertNotNull(yearSortButton);
        yearSortButton.click();

        WebElement e = driver.findElement(By.tagName("tbody"));
        List<WebElement> rows = e.findElements(By.tagName("tr"));
        //first year
        String prevYear = driver.findElement(By.xpath("//tbody/tr[1]/td[2]")).getText();

        // Verify that the entire list is sorted by year - oldest first.
        // i went rogue here, instead of verifying top to be ID 940 and bottom 938
        // it's better verification of year-sort to actually look at the years.
        for (int i = 2; i <= rows.size(); i++) {
            Assert.assertTrue(Integer.parseInt(prevYear) < Integer.parseInt(driver.findElement(By.xpath("//tbody/tr[" + i + "]/td[2]")).getText()));
        }
    }

    //Press the edit button for the car with the id 938. Change the Description to "Cool car", and save changes.
    @Test
    //Verify that the row for car with id 938 now contains "Cool car" in the Description column
    public void t5_verifyEditingCar() throws Exception {
        // reset for good measure
        com.jayway.restassured.RestAssured.given().get("http://localhost:3000/reset");
        driver.findElement(By.xpath("//tbody/tr[td//text()[contains(., '938')]]/td[8]/a[. = 'Edit']")).click();

        WebElement descriptionTextbox = driver.findElement(By.id("description"));

        descriptionTextbox.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        descriptionTextbox.sendKeys("Cool car");
        driver.findElement(By.id("save")).click();

        WebElement td = driver.findElement(By.xpath("//tbody/tr[td//text()[contains(., '938')]]/td[6]"));
        Assert.assertThat(td.getText(), is("Cool car"));
    }

    @Test
    //Verify that we have an error message with the text “All fields are required” and we still only have five rows in the all cars table.
    public void t6_verifyErrorEmptyFieldsNewCar() throws Exception {
        driver.findElement(By.id("new")).click();
        driver.findElement(By.id("save")).click();
        WebElement errorText = driver.findElement(By.id("submiterr"));
        Assert.assertThat(errorText.getText(), is("All fields are required"));
        WebElement e = driver.findElement(By.tagName("tbody"));
        Assert.assertThat(e.findElements(By.tagName("tr")).size(), is(5));
    }

    @Test
    public void t7_verifyAddCar() {
        driver.findElement(By.id("new")).click();
        driver.findElement(By.id("year")).sendKeys("2008");
        driver.findElement(By.id("registered")).sendKeys("2002-5-5");
        driver.findElement(By.id("make")).sendKeys("Kia");
        driver.findElement(By.id("model")).sendKeys("Rio");
        driver.findElement(By.id("description")).sendKeys("As new");
        driver.findElement(By.id("price")).sendKeys("31000");
        driver.findElement(By.id("save")).click();

        WebElement e = driver.findElement(By.tagName("tbody"));
        List<WebElement> rows = e.findElements(By.tagName("tr"));

        String nYear = driver.findElement(By.xpath("//tbody/tr[" + rows.size() + "]/td[2]")).getText();
        String nRegistered = driver.findElement(By.xpath("//tbody/tr[" + rows.size() + "]/td[3]")).getText();
        String nMake = driver.findElement(By.xpath("//tbody/tr[" + rows.size() + "]/td[4]")).getText();
        String nModel = driver.findElement(By.xpath("//tbody/tr[" + rows.size() + "]/td[5]")).getText();
        String nDescription = driver.findElement(By.xpath("//tbody/tr[" + rows.size() + "]/td[6]")).getText();
        String nPrice = driver.findElement(By.xpath("//tbody/tr[" + rows.size() + "]/td[7]")).getText();

        Assert.assertThat(nYear, is("2008"));
        Assert.assertThat(nRegistered, is("5/5/2002"));
        Assert.assertThat(nMake, is("Kia"));
        Assert.assertThat(nModel, is("Rio"));
        Assert.assertThat(nDescription, is("As new"));
        Assert.assertThat(nPrice, is("31.000,00 kr."));

    }
}
