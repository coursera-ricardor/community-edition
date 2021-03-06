/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 * This file is part of Alfresco
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.test.wqs;

import org.alfresco.po.share.AlfrescoVersion;
import org.alfresco.po.share.MyTasksPage;
import org.alfresco.po.share.site.document.*;
import org.alfresco.po.share.steps.LoginActions;
import org.alfresco.po.share.steps.SiteActions;
import org.alfresco.po.share.util.ShareTestProperty;
import org.alfresco.po.wqs.FactoryWqsPage;
import org.alfresco.po.wqs.WcmqsBlogPage;
import org.alfresco.po.wqs.WcmqsBlogPostPage;
import org.alfresco.po.wqs.WcmqsEditPage;
import org.alfresco.po.wqs.WcmqsHomePage;
import org.alfresco.po.wqs.WcmqsLoginPage;
import org.alfresco.po.wqs.WcmqsNewsArticleDetails;
import org.alfresco.po.wqs.WcmqsNewsPage;
import org.alfresco.test.AlfrescoTests;
import org.alfresco.test.util.BasicAuthPublicApiFactory;
import org.alfresco.test.util.SiteService;
import org.alfresco.test.util.UserService;
import org.alfresco.webdrone.HtmlPage;
import org.alfresco.webdrone.RenderTime;
import org.alfresco.webdrone.WebDrone;
import org.alfresco.webdrone.WebDroneImpl;
import org.alfresco.webdrone.exception.PageException;
import org.alfresco.webdrone.exception.PageRenderTimeException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Class includes: Abstract class holds all common methods for WQS tests
 *
 * @author Oana Caciuc
 */
public abstract class AbstractWQS implements AlfrescoTests
{
    public static final String SLASH = File.separator;
    protected static final String SITE_WEB_QUICK_START_DASHLET = "site-wqs";
    protected static final String QUICK_START_LIVE = "Quick Start Live";
    protected static final String NEWS = "news";
    protected static final String INDEX_HTML = "slide1.html";
    protected static final String ACCOUNTING = "accounting";
    protected static final String ACCOUNTING_DATA = "Accounting";
    protected static final String DEFAULT_PASSWORD = "password";
    private static final String SRC_ROOT = System.getProperty("user.dir") + SLASH;
    protected static final String DATA_FOLDER = SRC_ROOT + "webquickstart-tests" + SLASH + "testdata" + SLASH;
    public static long maxWaitTime;
    protected static ApplicationContext ctx;
    protected static String shareUrl;
    protected static String wqsURL;
    protected static AlfrescoVersion alfrescoVersion;
    protected static String ADMIN_USERNAME;
    protected static String ADMIN_PASSWORD;
    protected static ShareTestProperty testProperties;
    protected static BasicAuthPublicApiFactory dataPrepProperties;
    protected static String UNIQUE_TESTDATA_STRING = "newdata";
    protected static String DOMAIN_FREE = "freethtnew.test";
    protected static String DOMAIN_HYBRID = "hybridnew.test";
    private static WqsTestProperty wqsTestProperties;
    private static String RESULTS_FOLDER = SRC_ROOT + "test-output" + SLASH;
    private static Log logger = LogFactory.getLog(AbstractWQS.class);
    protected final String ALFRESCO_QUICK_START = "Alfresco Quick Start";
    protected final String QUICK_START_EDITORIAL = "Quick Start Editorial";
    protected final String ROOT = "root";
    protected final String DOCLIB = "DocumentLibrary";
    protected SiteService siteService;
    protected UserService userService;
    protected String testName;
    protected WebDrone drone;
    protected LoginActions loginActions = new LoginActions();
    protected SiteActions siteActions = new SiteActions();
    protected long MAX_WAIT_TIME_MINUTES = 120000;
    Map<String, WebDrone> droneMap = new HashMap<String, WebDrone>();

    @BeforeSuite(alwaysRun = true)
    @Parameters({"contextFileName"})
    public static void setupContext(@Optional("wqs-context.xml") String contextFileName)
    {
        List<String> contextXMLList = new ArrayList<String>();
        contextXMLList.add(contextFileName);
        ctx = new ClassPathXmlApplicationContext(contextXMLList.toArray(new String[contextXMLList.size()]));
        testProperties = (ShareTestProperty) ctx.getBean("shareTestProperties");
        wqsTestProperties = (WqsTestProperty) ctx.getBean("wqsProperties");
        shareUrl = testProperties.getShareUrl();
        wqsURL = wqsTestProperties.getWcmqs().replace((shareUrl).replaceAll(".*\\//|\\:.*", ""), getIpAddress());

        ADMIN_USERNAME = testProperties.getUsername();
        ADMIN_PASSWORD = testProperties.getPassword();
        alfrescoVersion = testProperties.getAlfrescoVersion();

    }

    /**
     * Get the IP address of the shareUrl
     *
     * @return String
     */
    public static String getIpAddress()
    {
        String hostName = (shareUrl).replaceAll(".*\\//|\\:.*", "");
        String ipAddress = "";
        try
        {
            ipAddress = InetAddress.getByName(hostName).toString().replaceAll(".*/", "");
            logger.info("Ip address from Alfresco server was obtained");
        }
        catch (IOException | SecurityException e)
        {
            logger.error("Ip address from Alfresco server could not be obtained");
        }

        return ipAddress;
    }

    /*@BeforeClass(alwaysRun = true)
    public void getWebDrone() throws Exception
    {
        drone = (WebDrone) ctx.getBean("webDrone");
        drone.maximize();
    }*/

    public void setup() throws Exception
    {
        drone = (WebDrone) ctx.getBean("webDrone");
        drone.maximize();
        siteService = (SiteService) ctx.getBean("siteService");
        userService = (UserService) ctx.getBean("userService");
        dataPrepProperties = (BasicAuthPublicApiFactory) ctx.getBean("basicAuthPublicApiFactory");
        maxWaitTime = ((WebDroneImpl) drone).getMaxPageRenderWaitTime();
    }

    /**
     * Helper returns the test / methodname. This needs to be called as the 1st
     * step of the test. Common Test code can later be introduced here.
     *
     * @return String testcaseName
     */
    public String getTestName()
    {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    /**
     * Helper to consistently get the userName in the specified domain, in the
     * desired format.
     *
     * @param testID String Name of the test for uniquely identifying / mapping
     *               test data with the test
     * @return String userName
     */
    protected String getUserNameForDomain(String testID, String domainName)
    {
        String userName;
        if (domainName.isEmpty())
        {
            domainName = DOMAIN_FREE;
        }
        // ALF: Workaround needs toLowerCase to be added. to be removed when
        // jira is fixed
        userName = String.format("user%s@%s", testID, domainName).toLowerCase();

        return userName;
    }

    /**
     * Helper to consistently get the filename.
     *
     * @param partFileName String Part Name of the file for uniquely identifying /
     *                     mapping test data with the test
     * @return String fileName
     */
    protected String getFileName(String partFileName)
    {
        String fileName;

        fileName = String.format("File%s-%s", UNIQUE_TESTDATA_STRING, partFileName);

        return fileName;
    }


    public void tearDown()
    {
        if (logger.isTraceEnabled())
        {
            logger.trace("shutting web drone");
        }
        // Close the browser
        if (drone != null)
        {
            drone.deleteCookies();
            drone.quit();
            drone = null;
        }
    }

    public void savePageSource(String methodName) throws IOException
    {
        for (Map.Entry<String, WebDrone> entry : droneMap.entrySet())
        {
            if (entry.getValue() != null)
            {
                String htmlSource = ((WebDroneImpl) entry.getValue()).getDriver().getPageSource();
                File file = new File(RESULTS_FOLDER + methodName + "_" + entry.getKey() + "_Source.html");
                FileUtils.writeStringToFile(file, htmlSource);
            }
        }
    }

    /**
     * Helper to Take a ScreenShot. Saves a screenshot in target folder
     * <RESULTS_FOLDER>
     *
     * @param methodName String This is the Test Name / ID
     * @throws IOException if error
     */
    public void saveScreenShot(String methodName) throws IOException
    {
        for (Map.Entry<String, WebDrone> entry : droneMap.entrySet())
        {
            if (entry.getValue() != null)
            {
                File file = entry.getValue().getScreenShot();
                File tmp = new File(RESULTS_FOLDER + methodName + "_" + entry.getKey() + ".png");
                FileUtils.copyFile(file, tmp);
            }
        }

    }

    @BeforeMethod
    protected String getMethodName(Method method)
    {
        String methodName = method.getName();
        logger.info("[Test: " + methodName + " ]: START");
        return methodName;
    }

    @AfterMethod
    public void logTestResult(ITestResult result)

    {
        logger.info("[Test: " + result.getMethod().getMethodName() + " ]: " + result.toString().toUpperCase());
    }

    /**
     * Return the {@link WebDrone} Configured starting of test.
     *
     * @return {@link WebDrone}
     */
    public WebDrone getDrone()
    {
        return drone;
    }

    public String getShareUrl()
    {
        return testProperties.getShareUrl();
    }

    protected void waitForCommentPresent(MyTasksPage myTasksPage, String taskName)
    {
        int count = 1;
        while (!myTasksPage.isTaskPresent(taskName) && count <= 10)
        {
            siteActions.getSharePage(drone).getNav().selectMyDashBoard().render();
            siteActions.getSharePage(drone).getNav().selectWorkFlowsIHaveStarted().render();
            synchronized (this)
            {
                try
                {
                    this.wait(maxWaitTime);
                }
                catch (InterruptedException ex)
                {
                }
            }
            count++;
        }
    }

    protected DocumentLibraryPage navigateToFolderAndCreateContent(String folderName, String fileName, String fileContent, String fileTitle)
            throws Exception
    {
        String root_folder_path = ALFRESCO_QUICK_START + File.separator + QUICK_START_EDITORIAL + File.separator + ROOT + File.separator + folderName;

        ContentDetails contentDetails1 = new ContentDetails();
        contentDetails1.setName(fileName);
        contentDetails1.setTitle(fileTitle);
        contentDetails1.setContent(fileContent);
        DocumentLibraryPage documentLibPage = siteActions.navigateToFolder(drone, root_folder_path).render();
        documentLibPage = siteActions.createContent(drone, contentDetails1, ContentType.HTML).render();

        documentLibPage = siteActions.navigateToFolder(drone, root_folder_path).render();
        InlineEditPage inlineEditPage = documentLibPage.getFileDirectoryInfo(fileName).selectInlineEdit().render();
        inlineEditPage.insertTextInContent(fileContent);
        EditHtmlDocumentPage editDocPage = (EditHtmlDocumentPage) inlineEditPage.getInlineEditDocumentPage(MimeType.HTML);
        documentLibPage = editDocPage.saveText().render();

        return  documentLibPage.getSiteNav().selectSiteDocumentLibrary().render();
    }



    protected String generateRandomStringOfLength(int length)
    {
        char[] chars = "abc defghijkl mnopqrs tu vwxyz".toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++)
        {
            char c = chars[random.nextInt(chars.length)];
            stringBuilder.append(c);
        }

        return stringBuilder.toString();
    }

    /**
     * Open the article from the selected category
     *
     * @param newsCategory
     * @param newsTitle
     * @return WcmqsNewsArticleDetails
     */
    public WcmqsNewsArticleDetails openNewsFromCategory(String newsCategory, String newsTitle)
    {

        WcmqsHomePage homePage = new WcmqsHomePage(drone);
        WcmqsNewsPage newsPage = homePage.openNewsPageFolder(newsCategory).render();

        try
        {
            return newsPage.clickLinkByTitle(newsTitle).render();
        }
        catch (PageException e)
        {
            logger.error("The Article details page did not opened ", e);
            return null;
        }
    }

    /**
     * Method that waits for the news article to appear on the page for maximum minutesToWait
     * and then opens
     *
     * @param newsPage
     * @param newsArticleTitle
     */

    public WcmqsNewsArticleDetails waitAndOpenNewsArticle(WcmqsNewsPage newsPage, String newsArticleTitle)
    {
        long waitInSeconds = 3;
        long maxTimeWaitInSeconds = 3 * MAX_WAIT_TIME_MINUTES;
        boolean newsArticleFound = false;

        while (!newsArticleFound && maxTimeWaitInSeconds > 0)
        {
            try
            {
                newsPage.clickLinkByTitle(newsArticleTitle);
                newsArticleFound = true;
            }
            catch (Exception e)
            {
                synchronized (this)
                {
                    try
                    {
                        this.wait(waitInSeconds);
                    }
                    catch (InterruptedException ex)
                    {
                    }
                }
                drone.refresh();
                maxTimeWaitInSeconds = maxTimeWaitInSeconds - waitInSeconds;
            }

        }
        return FactoryWqsPage.resolveWqsPage(drone).render();
    }

    public void navigateTo(String url)
    {
        drone.navigateTo(url);
    }

    /**
     * Method that waits for the blog post to appear on the page for maximum minutesToWait
     * and then opens it.
     *
     * @param blogPage
     * @param blogPostTitle
     */

    public WcmqsBlogPostPage waitAndOpenBlogPost(WcmqsBlogPage blogPage, String blogPostTitle)
    {
        long waitInSeconds = 3;
        long maxTimeWaitInSeconds = 3 * MAX_WAIT_TIME_MINUTES;
        boolean newsArticleFound = false;
        WcmqsBlogPostPage blogPost = null;

        while (!newsArticleFound && maxTimeWaitInSeconds > 0)
        {
            try
            {
                blogPage.openBlogPost(blogPostTitle);
                blogPost = FactoryWqsPage.resolveWqsPage(drone).render();
                blogPost.render();
                newsArticleFound = true;
            }
            catch (Exception e)
            {
                synchronized (this)
                {
                    try
                    {
                        logger.info("Waiting for edited name of blog post...");
                        this.wait(waitInSeconds);
                    }
                    catch (InterruptedException ex)
                    {
                    }
                }
                drone.refresh();
                maxTimeWaitInSeconds = maxTimeWaitInSeconds - waitInSeconds;
            }

        }

        if (blogPost == null)
        {
            throw new PageException(" Blog Post Page was not found.");
        }

        return blogPost;

    }

    /**
     * Assume the WcmqsEditPage is opened. Add name, title and content, then submit the form
     *
     * @param postName
     * @param postTitle
     * @param postContent
     * @return HtmlPage (WcmqsBlogPage or WcmqsNewsPage)
     */

    public HtmlPage fillWqsCreateForm(String postName, String postTitle, String postContent)
    {
        WcmqsEditPage editPage = new WcmqsEditPage(drone);

        editPage.editName(postName);
        editPage.editTitle(postTitle);
        editPage.insertTextInContent(postContent);

        return editPage.clickSubmitButton();
    }

    public DocumentLibraryPage navigateToWqsFolderFromRoot(DocumentLibraryPage documentLibraryPage, String folderName)
    {
        documentLibraryPage = (DocumentLibraryPage) documentLibraryPage.selectFolder(ALFRESCO_QUICK_START).render();
        documentLibraryPage = (DocumentLibraryPage) documentLibraryPage.selectFolder(QUICK_START_EDITORIAL).render();
        documentLibraryPage = (DocumentLibraryPage) documentLibraryPage.selectFolder(ROOT).render();
        documentLibraryPage = (DocumentLibraryPage) documentLibraryPage.selectFolder(folderName).render();
        return documentLibraryPage;
    }

    public void waitAndOpenNewSection(WcmqsHomePage homePage, String menuOption, int minutesToWait)
    {
        int waitInMilliSeconds = 3000;
        int maxTimeWaitInMilliSeconds = 60000 * minutesToWait;
        boolean sectionFound = false;

        while (!sectionFound && maxTimeWaitInMilliSeconds > 0)
        {
            try
            {
                homePage.selectMenu(menuOption);
                sectionFound = true;
            }
            catch (Exception e)
            {
                synchronized (this)
                {
                    try
                    {
                        this.wait(waitInMilliSeconds);
                    }
                    catch (InterruptedException ex)
                    {
                    }
                }
                drone.refresh();
                maxTimeWaitInMilliSeconds = maxTimeWaitInMilliSeconds - waitInMilliSeconds;
            }

        }
    }


    /*
     * Make sure the admin is logged in WQS for edit mode.
     * When open an article for the first time, the Login dialog is displayed.
     */

    public void loginToWqs()
    {

        if (drone.isElementDisplayed(By.cssSelector("div[id='awe-login']")))
        {
            WcmqsLoginPage wcmqsLoginPage = new WcmqsLoginPage(drone);
            wcmqsLoginPage.render();
            wcmqsLoginPage.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
        else
        {
            synchronized (this)
            {
                try
                {
                    wait(1000);
                    loginToWqs();
                }
                catch (InterruptedException e)
                {
                }
            }
        }
    }

    /**
     * Assume the user is on HomePage.
     * This method is used to make sure the user is logged in before doing any other action.
     */

    public void loginToWqsFromHomePage()
    {
        try
        {
            WcmqsHomePage wcmqsHomePage = new WcmqsHomePage(drone);
            wcmqsHomePage.render();
            wcmqsHomePage.selectFirstArticleFromLeftPanel().render();
        }
        catch (PageRenderTimeException e)
        {
            logger.error("The articles details were not opened ", e);
        }
        loginToWqs();
    }


    /**
     * Before navigating to WQS, the imported files need time to index
     */

    public void waitForWcmqsToLoad()
    {
        WcmqsHomePage homePage = new WcmqsHomePage(drone);
        RenderTime timer = new RenderTime(240000);
        try
        {
            while (true)
            {
                timer.start();
                synchronized (this)
                {
                    try
                    {
                        this.wait(100L);
                    }
                    catch (InterruptedException e)
                    {
                    }
                }
                try
                {
                    navigateTo(wqsURL);
                    if (homePage.isAlfrescoLogoDisplay())
                    {
                        break;
                    }
                    else
                    {
                        drone.refresh();
                        navigateTo(wqsURL);
                    }
                }
                catch (StaleElementReferenceException ste)
                {
                    logger.error("DOM has changed therefore page should render once change", ste);
                }
                finally
                {
                    timer.end();
                }
            }
        }
        catch (PageRenderTimeException te)
        {
            throw new PageException("The WQS page was not loading at " + wqsURL);
        }
    }


    /**
     * Method that navigates back to home page
     *
     * @return
     */
    public HtmlPage returnToHomePage()
    {
        try
        {
            drone.find(By.cssSelector("div[id='logo']>a")).click();
        }
        catch (NoSuchElementException e)
        {
            logger.error("The logo was not found ", e);

        }
        return FactoryWqsPage.resolveWqsPage(drone);
    }


    /**
     * Any updates on documents from wqs or share need a time to index
     */
    public void waitForDocumentsToIndex()
    {
        synchronized (this)
        {
            try
            {
                logger.info("Waiting 2 minutes for documents to index.");
                wait(MAX_WAIT_TIME_MINUTES);
            }
            catch (InterruptedException ex)
            {
            }
        }
    }
}