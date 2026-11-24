# Authentication

## Overview

The Email Extension uses **OAuth 2.0** authentication to securely connect to Gmail and Outlook/Office 365 email services. This page explains the authentication modes, OAuth 2.0 flow, token management, and security best practices.

## Authentication Modes

The extension supports **User Account Authentication** mode:

### User Account Authentication

- **Description**: Authenticates as a specific user account
- **Use Case**: Access emails for a specific user (e.g., support@company.com)
- **OAuth Flow**: Authorization Code Grant with user consent
- **Token Type**: User access token + refresh token
- **Permissions**: Limited to what the user has access to

**When to Use:**
- Accessing a specific user's mailbox
- Sending emails from a specific user account
- User-specific email operations

> **📝 Note**: Service Account authentication is not currently supported for Gmail/Outlook email access.

## OAuth 2.0 Scopes and Permissions

### Gmail Scopes

| Scope | Permission Level | Description |
|-------|------------------|-------------|
| `https://mail.google.com/` | Full Access | Read, send, delete, and manage emails |

**Why Full Access?**
- Required for both IMAP (read) and SMTP (send) operations
- Gmail does not support granular scopes for IMAP/SMTP protocols
- Alternative scopes like `gmail.readonly` do not support IMAP authentication

### Outlook/Office 365 Scopes

| Scope | Permission Level | Description |
|-------|------------------|-------------|
| `https://outlook.office365.com/IMAP.AccessAsUser.All` | IMAP Access | Read and manage emails via IMAP |
| `https://outlook.office365.com/SMTP.Send` | SMTP Send | Send emails via SMTP |
| `offline_access` | Refresh Token | Obtain refresh token for long-term access |

**Scope Breakdown:**
- **IMAP.AccessAsUser.All**: Allows the extension to connect to IMAP server and retrieve emails
- **SMTP.Send**: Allows the extension to send emails on behalf of the user
- **offline_access**: Enables refresh token generation for automatic token renewal

## OAuth 2.0 Grant Types

### 1. Authorization Code Grant

**Used For**: Initial user authentication

**Flow:**
1. User clicks "Test Connection" in Krista
2. User is redirected to Google/Microsoft login page
3. User grants permissions to the application
4. Authorization code is returned to Krista
5. Krista exchanges code for access token and refresh token

**Request Example (Token Exchange):**
```http
POST https://oauth2.googleapis.com/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
&code=4/0AY0e-g7xxxxxxxxxxxxx
&client_id=123456789.apps.googleusercontent.com
&client_secret=GOCSPX-xxxxxxxxxxxxx
&redirect_uri=https://app.krista.ai/api/extension/oauth2/callback
```

**Response Example:**
```json
{
  "access_token": "ya29.a0AfH6SMBxxxxxxxxxxxxx",
  "refresh_token": "1//0gxxxxxxxxxxxxx",
  "expires_in": 3600,
  "scope": "https://mail.google.com/",
  "token_type": "Bearer"
}
```

### 2. Refresh Token Grant

**Used For**: Automatic token refresh when access token expires

**Flow:**
1. Access token expires (after 1 hour)
2. Extension automatically uses refresh token to get new access token
3. New access token is stored
4. Email operations continue without user intervention

**Request Example:**
```http
POST https://oauth2.googleapis.com/token
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token
&refresh_token=1//0gxxxxxxxxxxxxx
&client_id=123456789.apps.googleusercontent.com
&client_secret=GOCSPX-xxxxxxxxxxxxx
```

**Response Example:**
```json
{
  "access_token": "ya29.a0AfH6SMBxxxxxxxxxxxxx",
  "expires_in": 3600,
  "scope": "https://mail.google.com/",
  "token_type": "Bearer"
}
```

## Authentication Flow

### Initial Authentication (Authorization Code Flow)

```
┌─────────┐                                  ┌─────────────┐
│  User   │                                  │   Krista    │
└────┬────┘                                  └──────┬──────┘
     │                                              │
     │  1. Click "Test Connection"                  │
     │─────────────────────────────────────────────>│
     │                                              │
     │  2. Redirect to OAuth Provider               │
     │<─────────────────────────────────────────────│
     │                                              │
┌────▼────────────────────────────────────┐        │
│  Google/Microsoft Login Page            │        │
│  - Enter credentials                    │        │
│  - Grant permissions                    │        │
└────┬────────────────────────────────────┘        │
     │                                              │
     │  3. Authorization Code                       │
     │─────────────────────────────────────────────>│
     │                                              │
     │                                         ┌────▼─────┐
     │                                         │ Exchange │
     │                                         │   Code   │
     │                                         │   for    │
     │                                         │  Tokens  │
     │                                         └────┬─────┘
     │                                              │
     │  4. Connection Successful                    │
     │<─────────────────────────────────────────────│
     │                                              │
```

### Automatic Token Refresh

```
┌──────────────┐                    ┌─────────────────┐
│   Extension  │                    │ OAuth Provider  │
└──────┬───────┘                    └────────┬────────┘
       │                                     │
       │  1. Access Token Expired            │
       │                                     │
       │  2. Use Refresh Token               │
       │────────────────────────────────────>│
       │                                     │
       │  3. New Access Token                │
       │<────────────────────────────────────│
       │                                     │
       │  4. Continue Email Operations       │
       │                                     │
```

## Token Management

### Access Token

- **Lifetime**: 1 hour (3600 seconds)
- **Purpose**: Authenticate IMAP/SMTP connections
- **Storage**: Encrypted in Krista database
- **Refresh**: Automatic using refresh token

### Refresh Token

**Gmail:**
- **Lifetime**: 
  - **Verified Apps**: No expiration (unless revoked)
  - **Unverified Apps**: 7 days (requires re-authentication)
- **Purpose**: Obtain new access tokens
- **Storage**: Encrypted in Krista database

**Outlook/Office 365:**
- **Lifetime**: 90 days (with continuous use)
- **Purpose**: Obtain new access tokens
- **Storage**: Encrypted in Krista database
- **Renewal**: Automatically renewed when used

### Token Refresh Mechanism

The extension automatically refreshes access tokens:

1. **Before Expiration**: Token is refreshed 5 minutes before expiration
2. **On Failure**: If email operation fails with 401 Unauthorized, token is refreshed
3. **Transparent**: No user intervention required

## Token Expiration Scenarios

### 1. Access Token Expiration

**Scenario**: Access token expires after 1 hour

**Handling:**
```
1. Extension detects token expiration
2. Automatically uses refresh token to get new access token
3. Retries email operation with new token
4. User experiences no interruption
```

**Error**: None (handled automatically)

### 2. Refresh Token Expiration (Gmail Unverified Apps)

**Scenario**: Refresh token expires after 7 days for unverified Gmail apps

**Handling:**
```
1. Extension attempts to refresh access token
2. Refresh fails (refresh token expired)
3. User must re-authenticate
```

**Error**: `"Failed to test connection"` or `"Authentication failed"`

**Solution**: Click "Test Connection" again to re-authenticate

### 3. Refresh Token Revocation

**Scenarios:**
- User revokes app access in Google/Microsoft account settings
- User changes password
- Security event triggers token revocation
- Token limit exceeded (Gmail: 50 refresh tokens per user per app)

**Handling:**
```
1. Extension attempts to refresh access token
2. Refresh fails (refresh token revoked)
3. User must re-authenticate
```

**Error**: `"Failed to test connection"` or `"invalid_grant"`

**Solution**: Click "Test Connection" again to re-authenticate

### 4. Token Expiration Handling Flow

```
┌─────────────────┐
│ Email Operation │
└────────┬────────┘
         │
         ▼
    ┌────────────┐
    │ Use Access │
    │   Token    │
    └─────┬──────┘
          │
          ▼
     ┌─────────┐
     │ Success?│
     └────┬────┘
          │
    ┌─────┴─────┐
    │           │
   Yes         No (401)
    │           │
    ▼           ▼
┌────────┐  ┌──────────────┐
│ Return │  │ Refresh Token│
│ Result │  └──────┬───────┘
└────────┘         │
                   ▼
              ┌─────────┐
              │ Success?│
              └────┬────┘
                   │
             ┌─────┴─────┐
             │           │
            Yes         No
             │           │
             ▼           ▼
        ┌────────┐  ┌──────────────┐
        │ Retry  │  │ Re-auth      │
        │ Email  │  │ Required     │
        │ Op     │  └──────────────┘
        └────────┘
```

## Security Best Practices

### 1. Protect Client Credentials
- Never expose Client ID or Client Secret in public repositories
- Store Client Secret as secured parameter in Krista
- Rotate Client Secret periodically (every 90 days recommended)

### 2. Use HTTPS Only
- All OAuth redirects must use HTTPS
- Never use HTTP for redirect URIs in production

### 3. Validate Redirect URIs
- Configure exact redirect URI in OAuth app
- Do not use wildcard redirect URIs
- Verify redirect URI matches in both OAuth app and Krista

### 4. Monitor Token Usage
- Review OAuth consent screen regularly
- Monitor for unusual authentication patterns
- Revoke unused or suspicious tokens

### 5. Limit Scope Permissions
- Request only necessary scopes
- Avoid requesting admin-level permissions unless required

## Troubleshooting Authentication

### Issue: "Failed to test connection"

**Possible Causes:**
1. Invalid Client ID or Client Secret
2. Redirect URI mismatch
3. Required APIs not enabled
4. User denied permissions

**Solution:**
1. Verify OAuth credentials are correct
2. Check redirect URI matches exactly
3. Enable Gmail API (Gmail) or Microsoft Graph API (Outlook)
4. Grant all requested permissions during OAuth flow

### Issue: "invalid_grant" Error

**Cause**: Refresh token expired or revoked

**Solution**:
1. Click "Test Connection" to re-authenticate
2. Grant permissions again
3. For Gmail: Verify app is published (not in testing mode)

### Issue: "redirect_uri_mismatch"

**Cause**: Redirect URI in OAuth app doesn't match Krista redirect URI

**Solution**:
1. Get exact redirect URI from Krista: `https://<your-domain>/api/extension/oauth2/callback`
2. Add this exact URI to OAuth app configuration
3. Save changes and retry

### Issue: Frequent Re-authentication Required

**Cause**: Gmail app in testing mode (7-day token expiration)

**Solution**:
1. Publish Gmail app to production
2. Complete OAuth verification process
3. Refresh tokens will not expire

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup parameters
- [Creating Gmail App](pages/CreatingGmailApp.md) - Gmail OAuth setup
- [Creating Outlook App](pages/CreatingOutlookApp.md) - Outlook OAuth setup

