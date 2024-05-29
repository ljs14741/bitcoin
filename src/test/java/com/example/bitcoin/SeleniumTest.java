package com.example.bitcoin;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class SeleniumTest {
    public static void main(String[] args) {
        // ChromeDriver 경로 설정
        System.setProperty("webdriver.chrome.driver", "C:/chromedriver-win64/chromedriver.exe");

        // ChromeDriver 인스턴스 생성
        WebDriver driver = new ChromeDriver();

        // 웹 페이지 열기
        driver.get("https://www.google.com");

        // 페이지 타이틀 출력
        System.out.println("Page title is: " + driver.getTitle());

        // 드라이버 종료
        driver.quit();
    }
}