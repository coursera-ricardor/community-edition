/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.share.repository;

import static org.testng.Assert.assertFalse;

import java.io.File;

import org.alfresco.po.share.enums.ViewType;
import org.alfresco.po.share.site.document.DocumentDetailsPage;
import org.alfresco.po.share.site.document.ShareLinkPage;
import org.alfresco.share.util.AbstractUtils;
import org.alfresco.share.util.ShareUser;
import org.alfresco.share.util.ShareUserRepositoryPage;
import org.alfresco.share.util.ShareUserSitePage;
import org.alfresco.test.FailedTestListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Repository Document Share Tests
 *
 * @author Jamie Allison
 * @since 4.3
 */
@Listeners(FailedTestListener.class)
public class RepositoryDocumentShareTests extends AbstractUtils
{
    private static Log logger = LogFactory.getLog(RepositoryDocumentShareTests.class);

    protected String testUser;

    @Override
    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception
    {
        super.setup();
        testName = this.getClass().getSimpleName();
        testUser = testName + "@" + DOMAIN_FREE;
        logger.info("Starting Tests: " + testName);
    }

    @Test(groups = { "DataPrepRepository", "EnterpriseOnly" })
    public void dataPrep_AONE_3503() throws Exception
    {
        String testName = getTestName();
        String folderName = getFolderName(testName);

        ShareUser.login(drone, ADMIN_USERNAME, ADMIN_PASSWORD);

        ShareUserRepositoryPage.openRepositorySimpleView(drone);

        ShareUserRepositoryPage.createFolderInRepository(drone, folderName, folderName);
    }

    @Test(groups = "EnterpriseOnly")
    public void AONE_3503() throws Exception
    {
        String testName = getTestName();
        String folderName = getFolderName(testName);
        String subFolderName = folderName + System.currentTimeMillis();
        String fileName1 = getFileName(testName) + System.currentTimeMillis() + "_1.txt";
        String fileName2 = getFileName(testName) + System.currentTimeMillis() + "_2.txt";
        String fileName3 = getFileName(testName) + System.currentTimeMillis() + "_3.txt";
        File file1 = newFile(fileName1, fileName1);
        File file2 = newFile(fileName2, fileName2);
        File file3 = newFile(fileName3, fileName3);

        ShareUser.login(drone, ADMIN_USERNAME, ADMIN_PASSWORD);

        ShareUserRepositoryPage.openRepositoryDetailedView(drone);

        ShareUserRepositoryPage.navigateToFolderInRepository(drone, REPO + SLASH + folderName);
        ShareUserRepositoryPage.createFolderInRepository(drone, subFolderName, subFolderName);

        ShareUserRepositoryPage.navigateToFolderInRepository(drone, REPO + SLASH + folderName + SLASH + subFolderName);

        ShareUserRepositoryPage.uploadFileInRepository(drone, file1);
        ShareUserRepositoryPage.uploadFileInRepository(drone, file2);
        ShareUserRepositoryPage.uploadFileInRepository(drone, file3);

        ShareUserRepositoryPage.selectView(drone, ViewType.DETAILED_VIEW);

        ShareUserSitePage.getFileDirectoryInfo(drone, fileName1).clickShareLink().render();
        ShareUserSitePage.getFileDirectoryInfo(drone, fileName2).clickShareLink().render();
        ShareUserSitePage.getFileDirectoryInfo(drone, fileName3).clickShareLink().render();

        ShareLinkPage shareLinkPage = ShareUserSitePage.getFileDirectoryInfo(drone, fileName1).clickShareLink().render();
        shareLinkPage.clickOnUnShareButton().render();
        assertFalse(ShareUserSitePage.getFileDirectoryInfo(drone, fileName1).isFileShared(), "File[" + fileName1 + "] shared after UnShare Button clicked.");

        ShareUserRepositoryPage.openRepositoryGalleryView(drone);
        ShareUserRepositoryPage.navigateToFolderInRepository(drone, REPO + SLASH + folderName + SLASH + subFolderName);

        shareLinkPage = ShareUserSitePage.getFileDirectoryInfo(drone, fileName2).clickShareLink().render();
        shareLinkPage.clickOnUnShareButton().render();
        assertFalse(ShareUserSitePage.getFileDirectoryInfo(drone, fileName2).isFileShared(), "File[" + fileName2 + "] shared after UnShare Button clicked.");

        DocumentDetailsPage detailsPage = ShareUser.openDocumentDetailPage(drone, fileName3);

        shareLinkPage = detailsPage.clickShareLink().render();
        detailsPage = shareLinkPage.clickOnUnShareButton().render();
        assertFalse(detailsPage.isFileShared(), "File[" + fileName3 + "] shared after UnShare Button clicked.");
    }
}