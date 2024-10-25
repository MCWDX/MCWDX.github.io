import java.util.ArrayList;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import java.util.Scanner;

public class SeleniumTest {
    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // 设置ChromeDriver的路径
        System.setProperty("webdriver.chrome.driver", "C:/Program Files/Google/Chrome/Application/chromedriver-win64/chromedriver.exe");

        // 创建一个ChromeDriver对象
        WebDriver driver = new ChromeDriver();
            jdbc db = new jdbc();
            db.CreOrSwiTable();
            int pageNum = 1,nextPageNum = 0,i = 0,getArt = 0;
            String toSearch = new String();
            //打开知网查找 计算机 相关的论文
            driver.get("https://www.cnki.net/");
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
                driver.quit();
                e.printStackTrace();
            }
            WebElement element = driver.findElement(By.className("search-input"));
            element.sendKeys("计算机");
            WebElement searchButton = driver.findElement(By.className("search-btn"));
            searchButton.click();
            //然后sleep一会,以免因为网速慢没加载出来搜索结果,从而引发NoSuchElementException
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                driver.quit();
                e.printStackTrace();
            }
            //开始爬取论文,这里设置了爬够400条就截止
            while(getArt < 400)
            {
                //我爬取了论文的 标题、作者、来源、发布时间、论文详细页链接、还有论文概述
                String Title,Author,Sources,PublicationTime,Link,Abstract;
                WebElement nextArt;
                for(i = 1;i<=20;i++)
                {
                    try{
                        //获取知网上面的标题,作者名,来源,发布时间,还有详细链接
                        //xpath是我直接用浏览器复制的
                        nextArt = driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[2]/div[2]/div/div[2]/div/div[1]/div/div/table/tbody/tr["+i+"]/td[2]/a"));
                        Title = nextArt.getText();
                        Author = driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[2]/div[2]/div/div[2]/div/div[1]/div/div/table/tbody/tr["+i+"]/td[3]")).getText();
                        Sources = driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[2]/div[2]/div/div[2]/div/div[1]/div/div/table/tbody/tr["+i+"]/td[4]")).getText();
                        PublicationTime = driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[2]/div[2]/div/div[2]/div/div[1]/div/div/table/tbody/tr["+i+"]/td[5]")).getText();
                        Link = nextArt.getAttribute("href");
                        Abstract = getArtText(driver,Link);
                    } catch(NoSuchElementException e){
                        continue;
                    }
                    //不是论文就不管,爬取下一条
                    if(Abstract.equals("Not an article")) {
                        continue;
                    } else {
                        if(db.add(Title, Author, Sources, PublicationTime, Abstract, Link)){
                            getArt++;
                        }
                        System.out.printf("Total inserted %d data\n",getArt);
                        if(getArt >=400) {
                            break;
                        }
                    }
                }
                if(getArt >=400) {
                    break;
                }
                //我的浏览器中一页的搜索结果是20条,遍历完当前页的20条之后,就去按翻页
                for(i = 1;i<=15;i++){
                    try{
                        element = driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[2]/div[2]/div/div[2]/div/div[2]/div["+i+"]"));
                        try{
                            nextPageNum = Integer.parseInt(element.getText());
                        } catch(NumberFormatException e){
                            continue;
                        }
                        //我翻页是通过遍历知网上下面的小方块,直到小方块中页码大于当前页记录的页码才点击进入下一页
                        if(nextPageNum > pageNum) {
                            pageNum++;
                            element.click();
                            break;
                        }
                    }catch(NoSuchElementException e) {
                        break;
                    }
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    driver.quit();
                    e.printStackTrace();
                }
            }
        driver.quit();
        sc.close();
    }

    //获取概述的方法
    public static String getArtText(WebDriver driver,String link) {
        JavascriptExecutor js = (JavascriptExecutor)driver;
        ArrayList<String> newTab;
        String Abstract = "",columnName = "";
        //开新窗口进入论文详细页
        js.executeScript("window.open()");
        newTab = new ArrayList<String>(driver.getWindowHandles());
        //切换driver操作页面
        driver.switchTo().window(newTab.get(1));
        driver.get(link);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            driver.quit();
            e.printStackTrace();
        }
        try{
            //检查是不是摘要,不是就放弃这一页
            columnName = driver.findElement(By.xpath("/html/body/div[2]/div[1]/div[3]/div/div/div[4]/span[1]")).getText();
            if(columnName.equals("摘要：")){
                Abstract = driver.findElement(By.xpath("/html/body/div[2]/div[1]/div[3]/div/div/div[4]/span[2]")).getText();
            } else {
                Abstract = "Not an article";
            }
        } catch(NoSuchElementException e) {
            Abstract = "Not an article";
        } finally {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                driver.quit();
                e.printStackTrace();
            }
            driver.close();
            //最后必须把操作页切回去第0页,否则无法继续爬取
            driver.switchTo().window(newTab.get(0));
        }
        return Abstract;
    }
}
