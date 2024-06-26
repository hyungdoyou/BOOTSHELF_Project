import { describe, expect, test, beforeAll, afterAll } from "@jest/globals";
const { Builder, By, until } = require("selenium-webdriver");
const chrome = require("selenium-webdriver/chrome");

describe("회원 로그인 실패 테스트", () => {
  let driver;
  beforeAll(async () => {
    driver = await new Builder()
      .forBrowser("chrome")
      .setChromeOptions(
        new chrome.Options().addArguments("--headless") // 크롬을 띄우지 않도록 설정
      )
      .build();

    await driver.get("http://localhost:8081/");

    await driver.wait(() =>
      driver
        .executeScript("return document.readyState")
        .then((readyState) => readyState === "complete")
    );
  });

  afterAll(async () => {
    await driver.quit();
  }, 40000);

  test("회원 로그인 실패 - 이메일 미존재", async () => {
    const loginStep1_btn = await driver.wait(
      until.elementLocated(By.id("loginStep-1"))
    );
    //   await loginStep1_btn.click();
    await driver.executeScript("arguments[0].click();", loginStep1_btn);

    const loginStep2_btn = await driver.wait(
      until.elementLocated(By.id("loginStep-2"))
    );
    await loginStep2_btn.click();

    const input_email = await driver.wait(
      until.elementLocated(By.id("custEmail"))
    );
    await input_email.sendKeys("test1651616@gmail.com");

    const input_pw = await driver.wait(until.elementLocated(By.id("custPw")));
    await input_pw.sendKeys("Qwer1234@");

    const login_btn = await driver.wait(
      until.elementLocated(By.id("loginBtn"))
    );
    await login_btn.click();

    await driver.wait(until.alertIsPresent());

    let alert = await driver.switchTo().alert();

    let alertText = await alert.getText();

    await alert.accept();

    expect(alertText).toContain(
      "이메일을 찾을 수 없습니다. 가입한 이메일인지 다시 확인해주세요."
    );
  });
});
