# Email Extension - Architecture Documentation

**Version:** 1.0.6 
**Extension Name:** Email  
**Java Version:** Java 21  
**Execution Mode:** LIVE  
**Last Updated:** 2026-02-14

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture Layers](#architecture-layers)
3. [Component Architecture](#component-architecture)
4. [Data Flow Diagrams](#data-flow-diagrams)
5. [OAuth 2.0 Authentication Flow](#oauth-20-authentication-flow)
6. [Performance Limitations](#performance-limitations)
7. [Error Scenarios](#error-scenarios)
8. [Security Considerations](#security-considerations)

---

## 1. Overview

### 1.1 Purpose

The **Email Extension** provides a comprehensive abstraction layer for email operations (IMAP/SMTP) with OAuth 2.0 authentication for Gmail and Outlook/Office 365. It enables Krista workflows to send, receive, and manage emails securely.

### 1.2 Key Capabilities

- **Send Emails**: SMTP-based email sending with attachments
- **Retrieve Emails**: IMAP-based email retrieval with filtering
- **Move Messages**: Folder management operations
- **OAuth 2.0**: Automatic token refresh and management
- **Multi-Provider**: Gmail and Outlook/Office 365 support

### 1.3 Technology Stack

- **Java**: 21
- **Email Protocols**: IMAP (993/SSL), SMTP (587/STARTTLS)
- **Authentication**: OAuth 2.0 (Authorization Code Grant)
- **Email Library**: Jakarta Mail (Angus Mail 2.0.3)
- **Dependency Injection**: javax.inject
- **Logging**: SLF4J + Log4j2

---

## 2. Architecture Layers

The Email Extension follows a **5-layer architecture**:

```
┌─────────────────────────────────────────────────────────┐
│                    Layer 1: API Layer                    │
│  (EmailExtension, MessagingArea, ImapArea)              │
│  - Catalog Request Handlers                             │
│  - Input Validation                                     │
│  - Response Formatting                                  │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                 Layer 2: Service Layer                   │
│  (EmailClient, SmtpClient, ImapClient, FolderClient)    │
│  - Business Logic                                       │
│  - Email Operations                                     │
│  - Folder Management                                    │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Layer 3: Authentication Layer               │
│  (AuthClient, GmailAuthClient, OutlookAuthClient)       │
│  - OAuth 2.0 Token Management                           │
│  - Token Refresh Logic                                  │
│  - SASL XOAUTH2 Authentication                          │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│               Layer 4: Protocol Layer                    │
│  (IMAPProtocol, SMTPProtocol, OAuth2SaslClient)         │
│  - IMAP/SMTP Protocol Implementation                    │
│  - Jakarta Mail Integration                             │
│  - Connection Management                                │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                Layer 5: Storage Layer                    │
│  (EmailAttributeStore, KeyValueStore)                   │
│  - OAuth Token Storage                                  │
│  - Extension Attributes                                 │
│  - Last Fetch Timestamp                                 │
└─────────────────────────────────────────────────────────┘
```

### Layer Descriptions

#### Layer 1: API Layer
- **Purpose**: Expose catalog requests to Krista platform
- **Components**: EmailExtension, MessagingArea, ImapArea
- **Responsibilities**:
  - Handle catalog request invocations
  - Validate input parameters
  - Format responses
  - Handle lifecycle events (TEST_CONNECTION, VALIDATE_ATTRIBUTES)

#### Layer 2: Service Layer
- **Purpose**: Implement business logic for email operations
- **Components**: EmailClient, SmtpClient, ImapClient, FolderClient, MessageBuilder
- **Responsibilities**:
  - Send emails via SMTP
  - Retrieve emails via IMAP
  - Manage folders
  - Build email messages
  - Filter emails

#### Layer 3: Authentication Layer
- **Purpose**: Manage OAuth 2.0 authentication and token lifecycle
- **Components**: AuthClient, GmailAuthClient, OutlookAuthClient, EmailRequestAuthenticator
- **Responsibilities**:
  - Obtain OAuth 2.0 tokens
  - Refresh expired tokens
  - Provide SASL XOAUTH2 authentication
  - Detect credential changes

#### Layer 4: Protocol Layer
- **Purpose**: Implement IMAP/SMTP protocols using Jakarta Mail
- **Components**: IMAPProtocol, IMAPStore, IMAPFolder, SMTPMessage, OAuth2SaslClient
- **Responsibilities**:
  - Connect to IMAP/SMTP servers
  - Authenticate using XOAUTH2
  - Execute protocol commands
  - Handle protocol-level errors

#### Layer 5: Storage Layer
- **Purpose**: Persist extension state and OAuth tokens
- **Components**: EmailAttributeStore, KeyValueStore
- **Responsibilities**:
  - Store OAuth access/refresh tokens
  - Store extension configuration
  - Track last fetch timestamp
  - Manage temporary state during validation

---

## 3. Component Architecture

### 3.1 Core Components

```
┌──────────────────────────────────────────────────────────────┐
│                      EmailExtension                          │
│  - testConnection()                                          │
│  - validateAttributes()                                      │
│  - getRequestAuthenticator()                                 │
└────────────────────┬─────────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
┌───────▼────────┐       ┌────────▼────────┐
│ MessagingArea  │       │   ImapArea      │
│ - sendMail()   │       │ - retrieveUnread│
│ - moveMessage()│       │   EmailsFrom    │
│ - fetchMails() │       │   Folder()      │
└───────┬────────┘       └────────┬────────┘
        │                         │
        └────────────┬────────────┘
                     │
        ┌────────────▼────────────┐
        │     EmailClient         │
        │ - getSmtpClient()       │
        │ - getImapClient()       │
        │ - getFolderClient()     │
        │ - me()                  │
        └────────────┬────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
┌───────▼────────┐       ┌────────▼────────┐
│  SmtpClient    │       │  ImapClient     │
│ - send()       │       │ - getFolder()   │
│ - getSession() │       │ - search()      │
└────────────────┘       └─────────────────┘
```

### 3.2 Authentication Components

```
┌──────────────────────────────────────────────────────────────┐
│                   AuthClientFactory                          │
│  + getClient(EmailAttributes, EmailAttributeStore, Mode)     │
└────────────────────┬─────────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
┌───────▼────────┐       ┌────────▼────────┐
│GmailAuthClient │       │OutlookAuthClient│
│- testConnection│       │- testConnection │
│- getAccessToken│       │- getAccessToken │
│- refreshToken  │       │- refreshToken   │
└───────┬────────┘       └────────┬────────┘
        │                         │
        └────────────┬────────────┘
                     │
        ┌────────────▼────────────┐
        │  OAuth2SaslClient       │
        │ - XOAUTH2 SASL Mechanism│
        │ - Token-based Auth      │
        └─────────────────────────┘
```

---

## 4. Data Flow Diagrams

### 4.1 Send Mail Flow

```
┌──────┐     ┌─────────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│ User │────>│MessagingArea│────>│EmailClient│────>│SmtpClient│────>│SMTP Server│
└──────┘     └─────────────┘     └──────────┘     └──────────┘     └──────────┘
                    │                   │                │                │
                    │ 1. sendMail()     │                │                │
                    │──────────────────>│                │                │
                    │                   │ 2. getSmtpClient()              │
                    │                   │──────────────>│                │
                    │                   │                │ 3. Authenticate│
                    │                   │                │───────────────>│
                    │                   │                │ 4. Send Email  │
                    │                   │                │───────────────>│
                    │                   │                │<───────────────│
                    │                   │<──────────────│ 5. Success     │
                    │<──────────────────│                │                │
                    │ 6. "Mail sent successfully."       │                │
```

### 4.2 Retrieve Unread Emails Flow

```
┌──────┐     ┌─────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│ User │────>│ImapArea │────>│EmailClient│────>│FolderClient│──>│IMAP Server│
└──────┘     └─────────┘     └──────────┘     └──────────┘     └──────────┘
                  │                │                 │                │
                  │ 1. retrieveUnreadEmailsFromFolder()              │
                  │────────────────>│                 │                │
                  │                │ 2. getFolderClient()             │
                  │                │────────────────>│                │
                  │                │                 │ 3. getFolder() │
                  │                │                 │───────────────>│
                  │                │                 │ 4. Open Folder │
                  │                │                 │───────────────>│
                  │                │                 │ 5. Search Unread│
                  │                │                 │───────────────>│
                  │                │                 │<───────────────│
                  │                │<────────────────│ 6. Email List  │
                  │<────────────────│                 │                │
                  │ 7. List<MailDetails>              │                │
```

### 4.3 OAuth 2.0 Token Refresh Flow

```
┌──────────┐     ┌────────────┐     ┌──────────────┐     ┌──────────┐
│EmailClient│────>│ AuthClient │────>│OAuth Provider│────>│TokenStore│
└──────────┘     └────────────┘     └──────────────┘     └──────────┘
      │                 │                    │                  │
      │ 1. getAccessToken()                  │                  │
      │────────────────>│                    │                  │
      │                 │ 2. Check Token Expiry                 │
      │                 │───────────────────────────────────────>│
      │                 │<───────────────────────────────────────│
      │                 │ 3. Token Expired                       │
      │                 │                    │                  │
      │                 │ 4. Refresh Token Request              │
      │                 │───────────────────>│                  │
      │                 │<───────────────────│ 5. New Access Token│
      │                 │ 6. Store New Token │                  │
      │                 │───────────────────────────────────────>│
      │<────────────────│ 7. Return Access Token                │
```

---

## 5. OAuth 2.0 Authentication Flow

### 5.1 Initial Authentication

```
┌──────┐  ┌────────┐  ┌──────────┐  ┌──────────────┐  ┌──────────┐
│ User │  │ Krista │  │Extension │  │OAuth Provider│  │TokenStore│
└──┬───┘  └───┬────┘  └────┬─────┘  └──────┬───────┘  └────┬─────┘
   │          │            │               │               │
   │ 1. Test Connection    │               │               │
   │─────────>│            │               │               │
   │          │ 2. Redirect to OAuth       │               │
   │<─────────│            │               │               │
   │          │            │               │               │
   │ 3. Login & Grant Permissions          │               │
   │──────────────────────────────────────>│               │
   │          │            │               │               │
   │ 4. Authorization Code │               │               │
   │<──────────────────────────────────────│               │
   │          │            │               │               │
   │ 5. Exchange Code for Tokens           │               │
   │──────────────────────>│──────────────>│               │
   │          │            │<──────────────│               │
   │          │            │ 6. Store Tokens               │
   │          │            │──────────────────────────────>│
   │          │<───────────│ 7. Connection Success         │
   │<─────────│            │               │               │
```

### 5.2 Automatic Token Refresh

```
┌──────────┐  ┌────────────┐  ┌──────────────┐  ┌──────────┐
│Email Op  │  │ AuthClient │  │OAuth Provider│  │TokenStore│
└────┬─────┘  └─────┬──────┘  └──────┬───────┘  └────┬─────┘
     │              │                │               │
     │ 1. Execute Email Operation    │               │
     │─────────────>│                │               │
     │              │ 2. Get Access Token            │
     │              │───────────────────────────────>│
     │              │<───────────────────────────────│
     │              │ 3. Token Expired               │
     │              │                │               │
     │              │ 4. Refresh Token Request       │
     │              │───────────────>│               │
     │              │<───────────────│ 5. New Token  │
     │              │ 6. Update Token│               │
     │              │───────────────────────────────>│
     │              │ 7. Retry Email Operation       │
     │<─────────────│                │               │
```

---

## 6. Performance Limitations

### 6.1 Email Provider Limits

| Provider | Limit Type | Limit Value | Impact |
|----------|-----------|-------------|--------|
| **Gmail** | Daily Send Limit (Free) | 500 emails/day | Sending fails after limit |
| **Gmail** | Daily Send Limit (Workspace) | 2,000 emails/day | Sending fails after limit |
| **Gmail** | Recipients per Message | 500 | Error if exceeded |
| **Gmail** | Attachment Size | 25 MB | Email rejected if exceeded |
| **Outlook** | Recipients per Message | 500 | Error if exceeded |
| **Outlook** | Attachment Size | 35 MB total, 20 MB per file | Email rejected if exceeded |
| **Outlook** | Daily Send Limit | Varies by account | Throttling may occur |

### 6.2 IMAP/SMTP Connection Limits

| Operation | Limit | Recommendation |
|-----------|-------|----------------|
| Concurrent IMAP Connections | 15 (Gmail), 20 (Outlook) | Reuse connections |
| IMAP Folder Open Duration | 30 minutes idle timeout | Close folders after use |
| SMTP Connection Timeout | 60 seconds | Retry on timeout |
| Email Fetch Page Size | 100 emails recommended | Use pagination |

### 6.3 OAuth Token Limits

| Token Type | Limit | Impact |
|------------|-------|--------|
| Refresh Tokens per User (Gmail) | 50 | Oldest token revoked when exceeded |
| Access Token Lifetime | 1 hour | Automatic refresh required |
| Refresh Token Lifetime (Gmail Unverified) | 7 days | Re-authentication required |
| Refresh Token Lifetime (Outlook) | 90 days (with use) | Re-authentication if unused |

### 6.4 Performance Recommendations

1. **Batch Email Operations**: Send multiple emails in batches rather than one-by-one
2. **Use Pagination**: Retrieve emails in pages of 50-100 to avoid memory issues
3. **Close Connections**: Always close IMAP folders after operations
4. **Cache Folder Lists**: Cache folder structure to avoid repeated IMAP LIST commands
5. **Optimize Attachments**: Compress large files before attaching

---

## 7. Error Scenarios

### 7.1 Authentication Errors

| Error | Cause | Resolution |
|-------|-------|------------|
| `"Failed to test connection"` | Invalid OAuth credentials | Verify Client ID/Secret, re-authenticate |
| `"invalid_grant"` | Refresh token expired/revoked | Click "Test Connection" to re-authenticate |
| `"redirect_uri_mismatch"` | Redirect URI doesn't match | Update OAuth app with correct redirect URI |
| `"Authentication failed"` | Access token expired, refresh failed | Re-authenticate via "Test Connection" |

### 7.2 SMTP Errors

| Error | Cause | Resolution |
|-------|-------|------------|
| `"Failed to send email"` | SMTP connection failure | Check network, verify SMTP server |
| `"Attachment size exceeds limit"` | Attachment too large | Reduce size or use cloud storage links |
| `"Invalid email address"` | Malformed recipient address | Validate email format |
| `"Daily send limit exceeded"` | Provider limit reached | Wait 24 hours or upgrade account |

### 7.3 IMAP Errors

| Error | Cause | Resolution |
|-------|-------|------------|
| `"Folder not found"` | Invalid folder name | Use correct folder name (e.g., "INBOX") |
| `"No Mail Found"` | No emails match criteria | Adjust search filters |
| `"Connection timeout"` | IMAP server unreachable | Check network, verify IMAP server |
| `"Too many connections"` | Exceeded concurrent connection limit | Close unused connections |

### 7.4 Configuration Errors

| Error | Cause | Resolution |
|-------|-------|------------|
| `"unsupported imap or smtp host"` | Invalid server hostname | Use Gmail or Outlook servers |
| `"Invalid domains for imap and smtp protocol"` | Mixed Gmail/Outlook servers | Use matching provider servers |
| `"Tenant Id cannot be null or empty"` | Missing Tenant ID for Outlook | Provide Azure AD Tenant ID |

---

## 8. Security Considerations

### 8.1 OAuth 2.0 Security

- **Token Encryption**: All OAuth tokens encrypted at rest
- **Secure Storage**: Tokens stored in Krista's secure KeyValueStore
- **Automatic Refresh**: Tokens refreshed before expiration
- **Scope Limitation**: Request only necessary scopes

### 8.2 Email Security

- **TLS/SSL**: All IMAP/SMTP connections use encryption
- **SASL XOAUTH2**: OAuth-based authentication (no password storage)
- **Client Secret Protection**: Client secrets marked as secured parameters

### 8.3 Best Practices

1. **Rotate Client Secrets**: Change OAuth client secrets every 90 days
2. **Monitor Token Usage**: Review OAuth consent screen regularly
3. **Use HTTPS**: Always use HTTPS for redirect URIs
4. **Validate Inputs**: Sanitize all user inputs before sending emails
5. **Limit Permissions**: Request minimum necessary OAuth scopes

---

**Document Version:** 1.0  
**Last Updated:** 2026-02-14 
**Maintained By:** Krista Extension Team

