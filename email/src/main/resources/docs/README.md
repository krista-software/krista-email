# Email Extension

## Overview

The **Email Extension** enables seamless integration with email services (Gmail and Outlook/Office 365) through the Krista platform. It provides comprehensive email management capabilities including sending emails, retrieving messages, moving messages between folders, and fetching emails based on various criteria.

This extension supports both **Gmail** and **Microsoft Outlook/Office 365** email providers with OAuth 2.0 authentication, ensuring secure and reliable email operations.

## Key Features

✅ **Multi-Provider Support**: Works with both Gmail and Outlook/Office 365  
✅ **OAuth 2.0 Authentication**: Secure authentication with automatic token refresh  
✅ **Send Emails**: Send emails with attachments, CC, BCC, and Reply-To support  
✅ **Retrieve Emails**: Fetch unread emails with pagination support  
✅ **Move Messages**: Move emails between folders (Inbox, Sent, Archive, etc.)  
✅ **Scheduled Email Fetching**: Fetch emails based on time intervals with filtering  
✅ **Rich Text Support**: Send HTML-formatted emails  
✅ **File Attachments**: Support for multiple file attachments  
✅ **Folder Management**: Access to all email folders (INBOX, Sent Items, Drafts, etc.)

## Quick Start Guide

### 1. Prerequisites
- Gmail or Outlook/Office 365 account
- OAuth 2.0 credentials (Client ID, Client Secret, Tenant ID for Outlook)
- Krista platform access

### 2. Configuration Steps
1. Create OAuth 2.0 application in Google Cloud Console or Azure Portal
2. Configure redirect URIs
3. Set up extension in Krista with OAuth credentials
4. Test connection
5. Start using catalog requests

### 3. First Email
```
Catalog Request: Send Mail
Subject: "Hello from Krista"
Message: "This is my first email sent via Krista Email Extension"
To: "recipient@example.com"
```

## Documentation Structure

### Getting Started
- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration parameters
- [Authentication](pages/Authentication.md) - OAuth 2.0 authentication details
- [Creating Gmail App](pages/CreatingGmailApp.md) - Step-by-step Gmail OAuth setup
- [Creating Outlook App](pages/CreatingOutlookApp.md) - Step-by-step Outlook OAuth setup

### Messaging Operations
- [Send Mail](pages/SendMail.md) - Send emails with attachments and formatting
- [Move Message](pages/MoveMessage.md) - Move emails between folders
- [Fetch Mails](pages/FetchMails.md) - Scheduled email fetching with filters

### IMAP Operations
- [Retrieve Unread Emails From Folder](pages/RetrieveUnreadEmailsFromFolder.md) - Get unread emails with pagination

### Reference
- [Mail Details Entity](pages/MailDetailsEntity.md) - Email entity structure and fields

## Supported Email Providers

| Provider | IMAP Server | SMTP Server | OAuth 2.0 |
|----------|-------------|-------------|-----------|
| **Gmail** | imap.gmail.com:993 | smtp.gmail.com:587 | ✅ Yes |
| **Outlook/Office 365** | outlook.office365.com:993 | smtp.office365.com:587 | ✅ Yes |

## Support & Resources

- **Extension Version**: 1.0.5
- **Java Version**: Java 21
- **Supported Protocols**: IMAP, SMTP
- **Authentication**: OAuth 2.0 (Authorization Code Grant)
- **Execution Mode**: LIVE

## Common Use Cases

### 1. Automated Email Notifications
Send automated emails when specific events occur in your Krista workflows.

### 2. Email Monitoring
Fetch and process incoming emails based on sender, subject, or recipient filters.

### 3. Email Organization
Automatically move emails to specific folders based on business rules.

### 4. Customer Communication
Send personalized emails to customers with attachments and rich formatting.

## Security Considerations

- OAuth 2.0 tokens are securely stored and automatically refreshed
- Client secrets are encrypted
- All email communication uses TLS/SSL encryption
- Supports both user and service account authentication modes

## Getting Help

For detailed information on each catalog request, refer to the individual documentation pages in the sidebar navigation.

---

