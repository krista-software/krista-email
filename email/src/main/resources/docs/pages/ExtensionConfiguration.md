# Extension Configuration

## Overview

The Email Extension requires OAuth 2.0 credentials to connect to Gmail or Outlook/Office 365 email services. This page describes all configuration parameters and setup steps required to configure the extension.

## Configuration Parameters

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| **IMAP Server** | Text | Yes | IMAP server hostname for receiving emails | `imap.gmail.com` or `outlook.office365.com` |
| **SMTP Server** | Text | Yes | SMTP server hostname for sending emails | `smtp.gmail.com` or `smtp.office365.com` |
| **Email** | Text | Yes | Email address of the account | `user@gmail.com` or `user@company.com` |
| **Client ID** | Text | Yes | OAuth 2.0 Client ID from Google/Azure | `123456789.apps.googleusercontent.com` |
| **Client Secret** | Text (Secured) | Yes | OAuth 2.0 Client Secret (encrypted) | `GOCSPX-xxxxxxxxxxxxx` |
| **Tenant ID** | Text | No | Azure AD Tenant ID (required for Outlook only) | `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |

## Step-by-Step Setup

### 1. Create OAuth 2.0 Application

**For Gmail:**
- Follow the [Creating Gmail App](pages/CreatingGmailApp.md) guide
- Create project in Google Cloud Console
- Enable Gmail API
- Create OAuth 2.0 credentials

**For Outlook/Office 365:**
- Follow the [Creating Outlook App](pages/CreatingOutlookApp.md) guide
- Register app in Azure Portal
- Configure API permissions
- Create client secret

### 2. Configure Extension in Krista

1. Navigate to **Extensions** in Krista Studio
2. Add **Email Extension**
3. Fill in configuration parameters:

**Gmail Example:**
```
IMAP Server: imap.gmail.com
SMTP Server: smtp.gmail.com
Email: yourname@gmail.com
Client ID: 123456789-abc.apps.googleusercontent.com
Client Secret: GOCSPX-xxxxxxxxxxxxx
Tenant ID: (leave empty for Gmail)
```

**Outlook Example:**
```
IMAP Server: outlook.office365.com
SMTP Server: smtp.office365.com
Email: yourname@company.com
Client ID: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
Client Secret: xxxxxxxxxxxxxxxxxxxxx
Tenant ID: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

### 3. Configure Redirect URI

The redirect URI must be configured in your OAuth 2.0 application:

**Format:**
```
https://<your-krista-domain>/api/extension/oauth2/callback
```

**Example:**
```
https://app.krista.ai/api/extension/oauth2/callback
```

> **📝 Note**: The redirect URI must match exactly in both the OAuth app configuration and Krista extension setup.

### 4. Test Connection

1. Click **Test Connection** button in Krista
2. You will be redirected to Google/Microsoft login page
3. Grant permissions to the application
4. You will be redirected back to Krista
5. Connection status should show **Connected**

## Server Configuration Details

### Gmail Servers

| Protocol | Server | Port | Security |
|----------|--------|------|----------|
| IMAP | imap.gmail.com | 993 | SSL/TLS |
| SMTP | smtp.gmail.com | 587 | STARTTLS |

### Outlook/Office 365 Servers

| Protocol | Server | Port | Security |
|----------|--------|------|----------|
| IMAP | outlook.office365.com | 993 | SSL/TLS |
| SMTP | smtp.office365.com | 587 | STARTTLS |

## Validation Rules

The extension validates configuration parameters before saving:

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| IMAP server must be Gmail or Outlook | "unsupported imap or smtp host" | Use `imap.gmail.com` or `outlook.office365.com` |
| SMTP server must be Gmail or Outlook | "unsupported imap or smtp host" | Use `smtp.gmail.com` or `smtp.office365.com` |
| IMAP and SMTP must be from same provider | "Invalid domains for imap and smtp protocol. Both domain should be of same vendor." | Use matching Gmail or Outlook servers |
| Tenant ID required for Outlook | "Tenant Id cannot be null or empty in case of Microsoft credentials" | Provide Azure AD Tenant ID |
| OAuth connection must succeed | "Failed to test connection" | Check credentials and network connectivity |

## Security Considerations

### 1. Client Secret Protection
- Client Secret is marked as **secured** and encrypted in Krista
- Never share or expose client secrets in logs or documentation
- Rotate client secrets periodically

### 2. OAuth 2.0 Scopes

**Gmail Scopes:**
- `https://mail.google.com/` - Full access to Gmail (read, send, delete)

**Outlook Scopes:**
- `https://outlook.office365.com/IMAP.AccessAsUser.All` - IMAP access
- `https://outlook.office365.com/SMTP.Send` - SMTP send access
- `offline_access` - Refresh token support

### 3. Token Storage
- Access tokens and refresh tokens are securely stored in Krista
- Tokens are automatically refreshed before expiration
- Tokens are encrypted at rest

## Troubleshooting

### Issue: "unsupported imap or smtp host"

**Cause**: Invalid IMAP or SMTP server hostname

**Solution**:
- For Gmail: Use `imap.gmail.com` and `smtp.gmail.com`
- For Outlook: Use `outlook.office365.com` and `smtp.office365.com`

### Issue: "Invalid domains for imap and smtp protocol"

**Cause**: Mixing Gmail and Outlook servers

**Solution**: Use matching servers from the same provider:
- ✅ Gmail IMAP + Gmail SMTP
- ✅ Outlook IMAP + Outlook SMTP
- ❌ Gmail IMAP + Outlook SMTP (not allowed)

### Issue: "Tenant Id cannot be null or empty"

**Cause**: Tenant ID is required for Outlook but not provided

**Solution**: Get Tenant ID from Azure Portal:
1. Go to Azure Active Directory
2. Copy **Tenant ID** from Overview page
3. Paste into extension configuration

### Issue: "Failed to test connection"

**Possible Causes:**
1. Invalid OAuth credentials
2. Redirect URI mismatch
3. Required APIs not enabled
4. Network connectivity issues

**Solution:**
1. Verify Client ID and Client Secret are correct
2. Check redirect URI matches exactly
3. Enable Gmail API (Gmail) or Microsoft Graph API (Outlook)
4. Test network connectivity to email servers

## Next Steps

After configuring the extension:

1. Review [Authentication](pages/Authentication.md) to understand OAuth 2.0 flow
2. Test catalog requests:
   - [Send Mail](pages/SendMail.md)
   - [Retrieve Unread Emails](pages/RetrieveUnreadEmailsFromFolder.md)
3. Integrate into your Krista workflows

## See Also

- [Authentication](pages/Authentication.md) - OAuth 2.0 authentication details
- [Creating Gmail App](pages/CreatingGmailApp.md) - Gmail OAuth setup
- [Creating Outlook App](pages/CreatingOutlookApp.md) - Outlook OAuth setup
