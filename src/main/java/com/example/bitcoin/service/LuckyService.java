package com.example.bitcoin.service;

import com.example.bitcoin.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class LuckyService {
    @PostConstruct
    public void setup() {
        System.setProperty("webdriver.chrome.driver", "C:/chromedriver-win64/chromedriver.exe");
    }

    public String getLucky(User user) {
        log.info("1: " + user.getGender());
        log.info("2: " + user.getSolarLunar());
        log.info("3: " + user.getBirthTime());
        log.info("4: " + user.getBirthDate());
        log.info("5: " + user.getBirthDate().getYear());
        log.info("6: " + user.getBirthDate().getMonthValue());
        log.info("5: " + user.getBirthDate().getDayOfMonth());

        WebDriver driver = new ChromeDriver();
        try {
            driver.get("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=0&ie=utf8&query=%EC%98%A4%EB%8A%98%EC%9D%98%EC%9A%B4%EC%84%B8");

            // 페이지가 로드될 시간을 기다립니다.
            Thread.sleep(2000); // 2초 대기

            // 성별 선택
            WebElement genderSelectButton = driver.findElement(By.xpath("//a[contains(@class, 'btn_select') and span[contains(text(), '남성')]]"));
            genderSelectButton.click(); // 성별 선택하는 드롭다운 버튼 클릭
            Thread.sleep(500); // 드롭다운 메뉴가 열릴 시간을 기다립니다.

            WebElement genderOption = driver.findElement(By.xpath("//a[@href='#' and contains(text(), '" + user.getGender() + "')]"));
            genderOption.click(); // 성별 옵션 클릭
            log.info("성별 '" + user.getGender() + "' 선택 완료");

            // 양력/음력 선택
            WebElement calendarSelectButton = driver.findElement(By.xpath("//a[contains(@class, 'btn_select') and span[contains(text(), '양력')]]"));
            calendarSelectButton.click(); // 양력/음력 선택하는 드롭다운 버튼 클릭
            Thread.sleep(500); // 드롭다운 메뉴가 열릴 시간을 기다립니다.

            WebElement solarLunarOption = driver.findElement(By.xpath("//a[@href='#' and contains(text(), '" + user.getSolarLunar() + "')]"));
            solarLunarOption.click(); // 양력/음력 옵션 클릭
            log.info(user.getSolarLunar() + " 선택 완료");

            // 태어난 시간 선택
            WebElement timeSelectButton = driver.findElement(By.xpath("//a[contains(@class, 'btn_select') and span[contains(text(), '모름')]]"));
            timeSelectButton.click(); // 태어난 시간 선택하는 드롭다운 버튼 클릭
            Thread.sleep(500); // 드롭다운 메뉴가 열릴 시간을 기다립니다.

            WebElement timeOption = driver.findElement(By.xpath("//a[@href='#' and contains(text(), '" + user.getBirthTime() + "')]"));
            timeOption.click(); // 태어난 시간 옵션 클릭
            log.info("태어난 시간 '" + user.getBirthTime() + "' 선택 완료");

            // 생년월일 선택
            WebElement birthDateButton = driver.findElement(By.xpath("//a[@class='select_pop _trigger']"));
            birthDateButton.click(); // 생년월일 선택하는 버튼 클릭
            Thread.sleep(500); // 드롭다운 메뉴가 열릴 시간을 기다립니다.

            // 연도 선택
            WebElement yearOption = driver.findElement(By.xpath("//*[@id='fortune_birthCondition']/div[1]/div[2]/div/div[1]/div/div[1]/div/div/div/ul/li/a[contains(text(), '" + user.getBirthDate().getYear() + "')]"));
            yearOption.click();

            // 월 선택
            WebElement monthOption = driver.findElement(By.xpath("//*[@id=\"fortune_birthCondition\"]/div[1]/div[2]/div/div[1]/div/div[2]/div/div/div/ul/li/a[contains(text(), '" + user.getBirthDate().getMonthValue() + "')]"));
            monthOption.click();

            // 일 선택
            WebElement dayOption = driver.findElement(By.xpath("//*[@id=\"fortune_birthCondition\"]/div[1]/div[2]/div/div[1]/div/div[3]/div/div/div/ul/li/a[contains(text(), '" + user.getBirthDate().getDayOfMonth() + "')]"));
            dayOption.click();

            // 운세 확인 버튼 클릭
            WebElement submitButton = driver.findElement(By.cssSelector("button.img_btn._resultBtn"));
            submitButton.click();
            log.info("운세 확인 버튼 클릭 완료");

            // 운세 결과 가져오기
            Thread.sleep(2000); // 2초 대기
            WebElement resultElement = driver.findElement(By.cssSelector("dd"));
            String resultText = resultElement.getText();
            log.info("운세 결과 전체 가져오기 완료");

            WebElement detailedResultElement = resultElement.findElement(By.tagName("p"));
            String detailedResultText = detailedResultElement.getText();
            log.info("운세 결과 상세 내용 가져오기 완료");

            return resultText + "\n상세 내용: " + detailedResultText;
        } catch (Exception e) {
            log.error("운세를 가져오는 데 실패했습니다.", e);
            return "운세를 가져오는 데 실패했습니다.";
        } finally {
            driver.quit();
        }
    }
}