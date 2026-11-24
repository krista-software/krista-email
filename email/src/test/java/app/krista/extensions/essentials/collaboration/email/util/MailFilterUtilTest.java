/*
 * Email Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.essentials.collaboration.email.util;

import app.krista.extensions.essentials.collaboration.email.mock.EmailMock;
import app.krista.extensions.essentials.collaboration.email.service.Email;
import app.krista.extensions.essentials.collaboration.email.service.EmailAddress;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class MailFilterUtilTest {
    private Email emailMock;

    @Before
    public void setup() {
        EmailAddress kInstance = new EmailAddress("Krista Instance", "mail@krista_instance.com");
        EmailAddress kAutomationInstance = new EmailAddress("Krista Automation", "mail@automation_instance.com");
        EmailAddress ccInstance = new EmailAddress("Krista Instance", "mail@krista_instance.com");
        EmailAddress ccAutomationInstance = new EmailAddress("Krista Next Gen Automation", "mail@krista_automation.com");
        emailMock = new EmailMock("abc.pqr@xyz.com", "Purchase item to test", "Test", List.of(kInstance, kAutomationInstance), List.of(ccInstance, ccAutomationInstance));
    }

    @Test
    public void test_FilterMail_When_Only_Subject_Is_Provided_And_Matched() {
        Email email = MailFilterUtil.filterMail(emailMock, null, null, "test", null);
        Assert.assertNotNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Subject_Is_Provided_And_Unmatched() {
        Email email = MailFilterUtil.filterMail(emailMock, null, null, "sell item", null);
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Sender_Is_Provided_And_Matched() {
        Email email = MailFilterUtil.filterMail(emailMock, null, "abc.pqr@xyz.com", null, null);
        Assert.assertNotNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Sender_Is_Provided_And_Unmatched() {
        Email email = MailFilterUtil.filterMail(emailMock, null, "pqr@kbc.com", null, null);
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Folder_Is_Provided_And_Matched() {
        Email email = MailFilterUtil.filterMail(emailMock, "Test", null, null, null);
        Assert.assertNotNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Folder_Is_Provided_And_Unmatched() {
        Email email = MailFilterUtil.filterMail(emailMock, "inbox", null, null, null);
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_ToOrCc_Is_Provided_And_Matched() {
        Email email = MailFilterUtil.filterMail(emailMock, null, null, null, "automation.com");
        Assert.assertNotNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_ToOrCc_Is_Provided_And_Unmatched() {
        Email email = MailFilterUtil.filterMail(emailMock, null, null, null, "amazon.com");
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Folder_Sender_Is_Provided_And_Matched() {
        Email email = MailFilterUtil.filterMail(emailMock, "Test", "abc.pqr@xyz.com", null, null);
        Assert.assertNotNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Folder_Sender_Is_Provided_And_Unmatched() {
        Email email = MailFilterUtil.filterMail(emailMock, "inbox", "pqr.pqr@xyz.com", null, null);
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Folder_Sender_Is_Provided_And_Unmatched_Folder() {
        Email email = MailFilterUtil.filterMail(emailMock, "inbox", "abc.pqr@xyz.com", null, null);
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Folder_Sender_Is_Provided_And_Unmatched_Sender() {
        Email email = MailFilterUtil.filterMail(emailMock, "Test", "pqr.pqr@xyz.com", null, null);
        Assert.assertNull(email);
    }
    @Test
    public void test_FilterMail_When_Only_Folder_Subject_Is_Provided_And_Matched() {
        Email email = MailFilterUtil.filterMail(emailMock, "Test", null, "purchase item", null);
        Assert.assertNotNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Folder_Subject_Is_Provided_And_Unmatched() {
        Email email = MailFilterUtil.filterMail(emailMock, "inbox", null, "sold item", null);
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Folder_Subject_Is_Provided_And_Unmatched_Subject() {
        Email email = MailFilterUtil.filterMail(emailMock, "Test", null, "sell item", null);
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Folder_Subject_Is_Provided_And_Unmatched_Folder() {
        Email email = MailFilterUtil.filterMail(emailMock, "inbox", null, "purchase item", null);
        Assert.assertNull(email);
    }
    @Test
    public void test_FilterMail_When_Only_Folder_ToOrCc_Is_Provided_And_Matched() {
        Email email = MailFilterUtil.filterMail(emailMock, "Test", null, null, "automation.com");
        Assert.assertNotNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Folder_ToOrCc_Is_Provided_And_Unmatched() {
        Email email = MailFilterUtil.filterMail(emailMock, "inbox", null, null, "abc.com");
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Folder_ToOrCc_Is_Provided_And_Unmatched_ToOrCc() {
        Email email = MailFilterUtil.filterMail(emailMock, "Test", null, null, "abc.com");
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Folder_ToOrCc_Is_Provided_And_Unmatched_Folder() {
        Email email = MailFilterUtil.filterMail(emailMock, "inbox", null, null, "automation.com");
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Sender_Subject_Is_Provided_And_Matched() {
        Email email = MailFilterUtil.filterMail(emailMock, null, "abc.pqr@xyz.com", "purchase item", null);
        Assert.assertNotNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Sender_Subject_Is_Provided_And_Unmatched() {
        Email email = MailFilterUtil.filterMail(emailMock, null, "pqr.pqr@xyz.com", "sold item", null);
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Sender_Subject_Is_Provided_And_Unmatched_Subject() {
        Email email = MailFilterUtil.filterMail(emailMock, null, "abc.pqr@xyz.com", "sell item", null);
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Sender_Subject_Is_Provided_And_Unmatched_Sender() {
        Email email = MailFilterUtil.filterMail(emailMock, null, "pqr.pqr@xyz.com", null, null);
        Assert.assertNull(email);
    }
    @Test
    public void test_FilterMail_When_Only_Sender_ToOrCc_Is_Provided_And_Matched() {
        Email email = MailFilterUtil.filterMail(emailMock, null, "abc.pqr@xyz.com", null, "automation.com");
        Assert.assertNotNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Sender_ToOrCc_Is_Provided_And_Unmatched() {
        Email email = MailFilterUtil.filterMail(emailMock, null, "pqr.pqr@xyz.com", null, "abc.com");
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Sender_ToOrCc_Is_Provided_And_Unmatched_ToOrCc() {
        Email email = MailFilterUtil.filterMail(emailMock, null, "abc.pqr@xyz.com", null, "abc.com");
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Sender_ToOrCc_Is_Provided_And_Unmatched_Sender() {
        Email email = MailFilterUtil.filterMail(emailMock, null, "pqr.pqr@xyz.com", null, "automation.com");
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Subject_ToOrCc_Is_Provided_And_Matched() {
        Email email = MailFilterUtil.filterMail(emailMock, null, null, "purchase item", "automation.com");
        Assert.assertNotNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Subject_ToOrCc_Is_Provided_And_Unmatched() {
        Email email = MailFilterUtil.filterMail(emailMock, null, null, "sold item", "flipkart.com");
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Subject_ToOrCc_Is_Provided_And_Unmatched_Subject() {
        Email email = MailFilterUtil.filterMail(emailMock, null, null, "sell item", "automation.com");
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Only_Subject_ToOrCc_Is_Provided_And_Unmatched_ToOrCc() {
        Email email = MailFilterUtil.filterMail(emailMock, null, null, "purchase item", "flipkart.com");
        Assert.assertNull(email);
    }



    @Test
    public void test_FilterMail_When_Folder_Sender_Subject_Is_Provided_And_Matched() {
        Email email = MailFilterUtil.filterMail(emailMock, "test", "abc.pqr@xyz.com", "purchase item", null);
        Assert.assertNotNull(email);
    }

    @Test
    public void test_FilterMail_When_Folder_Sender_Subject_Is_Provided_And_Unmatched_Subject() {
        Email email = MailFilterUtil.filterMail(emailMock, "test", "abc.pqr@xyz.com", "sell item", null);
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Folder_Sender_Subject_Is_Provided_And_Unmatched_Sender() {
        Email email = MailFilterUtil.filterMail(emailMock, "test", "abv.pqr@xyz.com", "purchase item", null);
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Folder_Sender_Subject_Is_Provided_And_Unmatched_Folder() {
        Email email = MailFilterUtil.filterMail(emailMock, "inbox", "abc.pqr@xyz.com", "purchase item", null);
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Folder_Sender_ToOrCc_Is_Provided_And_Matched() {
        Email email = MailFilterUtil.filterMail(emailMock, "test", "abc.pqr@xyz.com", null, "automation.com");
        Assert.assertNotNull(email);
    }

    @Test
    public void test_FilterMail_When_Folder_Sender_ToOrCc_Is_Provided_And_Unmatched_ToOrCc() {
        Email email = MailFilterUtil.filterMail(emailMock, "test", "abc.pqr@xyz.com", null, "abc.com");
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Folder_Sender_ToOrCc_Is_Provided_And_Unmatched_Sender() {
        Email email = MailFilterUtil.filterMail(emailMock, "test", "abv.pqr@xyz.com", "purchase item", "automation.com");
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Folder_Sender_ToOrCc_Is_Provided_And_Unmatched_Folder() {
        Email email = MailFilterUtil.filterMail(emailMock, "inbox", "abc.pqr@xyz.com", "purchase item", "automation.com");
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Sender_Subject_ToOrCc_Is_Provided_And_Matched() {
        Email email = MailFilterUtil.filterMail(emailMock, null, "abc.pqr@xyz.com", "purchase item", "automation.com");
        Assert.assertNotNull(email);
    }

    @Test
    public void test_FilterMail_When_Sender_Subject_ToOrCc_Is_Provided_And_Unmatched_ToOrCc() {
        Email email = MailFilterUtil.filterMail(emailMock, null, "abc.pqr@xyz.com", "purchase item", "abc.com");
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Sender_Subject_ToOrCc_Is_Provided_And_Unmatched_Sender() {
        Email email = MailFilterUtil.filterMail(emailMock, null, "abv.pqr@xyz.com", "purchase item", "automation.com");
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Sender_Subject_ToOrCc_Is_Provided_And_Unmatched_Subject() {
        Email email = MailFilterUtil.filterMail(emailMock, null, "abc.pqr@xyz.com", "sell item", "automation.com");
        Assert.assertNull(email);
    }
    @Test
    public void test_FilterMail_When_Folder_Sender_Subject_ToOrCc_Is_Provided_And_Matched() {
        Email email = MailFilterUtil.filterMail(emailMock, "Test", "abc.pqr@xyz.com", "purchase item", "automation.com");
        Assert.assertNotNull(email);
    }

    @Test
    public void test_FilterMail_When_Folder_Sender_Subject_ToOrCc_Is_Provided_And_Unmatched_ToOrCc() {
        Email email = MailFilterUtil.filterMail(emailMock, "Test", "abc.pqr@xyz.com", "purchase item", "abc.com");
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Folder_Sender_Subject_ToOrCc_Is_Provided_And_Unmatched_Sender() {
        Email email = MailFilterUtil.filterMail(emailMock, "Test", "abv.pqr@xyz.com", "purchase item", "automation.com");
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Folder_Sender_Subject_ToOrCc_Is_Provided_And_Unmatched_Subject() {
        Email email = MailFilterUtil.filterMail(emailMock, "Test", "abc.pqr@xyz.com", "sell item", "automation.com");
        Assert.assertNull(email);
    }
    @Test
    public void test_FilterMail_When_Folder_Sender_Subject_ToOrCc_Is_Provided_And_Unmatched_Folder() {
        Email email = MailFilterUtil.filterMail(emailMock, "inbox", "abc.pqr@xyz.com", "sell item", "automation.com");
        Assert.assertNull(email);
    }
    @Test
    public void test_FilterMail_When_Folder_Sender_Subject_ToOrCc_Is_Not_Provided_And_Matched() {
        Email email = MailFilterUtil.filterMail(emailMock, null, null, null, null);
        Assert.assertNull(email);
    }

    @Test
    public void test_FilterMail_When_Folder_Multiple_Sender_Subject_ToOrCc_Is_Not_Provided_And_Matched() {
        Email email = MailFilterUtil.filterMail(emailMock, "Test", "outlook.com; baranwalg2602@gmail.com; abc.pqr@xyz.com", "purchase item ", null);
        Assert.assertNotNull(email);
    }
}
