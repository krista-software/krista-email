# Send Mail

## Overview

Sends an email with optional attachments, CC, BCC, and Reply-To recipients. Supports rich HTML formatting and multiple file attachments.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ❌ No - Email sending is not idempotent; retrying could result in duplicate emails being sent

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| **Subject** | Text | Yes | Email subject line | `"Monthly Report"` |
| **Message** | RichText | Yes | Email body content (supports HTML) | `"<p>Please find the report attached.</p>"` |
| **To** | Text | Yes | Primary recipient email addresses (comma or semicolon separated) | `"user1@example.com; user2@example.com"` |
| **Attachments** | File (Multiple) | No | Files to attach to the email | PDF, DOCX, images, etc. |
| **Cc** | Text | No | Carbon copy recipients (comma or semicolon separated) | `"manager@example.com"` |
| **Bcc** | Text | No | Blind carbon copy recipients (comma or semicolon separated) | `"archive@example.com"` |
| **Reply To** | Text | No | Reply-to email address | `"noreply@example.com"` |

### Parameter Details

#### Subject
- **Validation**: Cannot be null or empty
- **Format**: Plain text
- **Max Length**: No hard limit, but email clients may truncate long subjects

#### Message
- **Format**: Rich HTML text
- **Supports**: HTML tags, inline styles, images (base64 encoded)
- **Validation**: Cannot be null or empty
- **Example**:
  ```html
  <html>
    <body>
      <h1>Welcome!</h1>
      <p>This is a <strong>formatted</strong> email.</p>
    </body>
  </html>
  ```

#### To
- **Format**: Email addresses separated by comma (`,`) or semicolon (`;`)
- **Validation**: Must contain at least one valid email address
- **Automatic Conversion**: Semicolons are automatically converted to commas
- **Examples**:
  - Single: `"user@example.com"`
  - Multiple: `"user1@example.com, user2@example.com"`
  - Semicolon: `"user1@example.com; user2@example.com"` (converted to comma)

#### Attachments
- **Type**: Multiple file upload
- **Size Limit**: Determined by email provider (typically 25MB for Gmail, 35MB for Outlook)
- **Supported Formats**: All file types
- **Validation**: Total attachment size must not exceed provider limit

#### Cc (Carbon Copy)
- **Format**: Same as "To" parameter
- **Purpose**: Recipients who should receive a copy (visible to all recipients)
- **Optional**: Can be null or empty

#### Bcc (Blind Carbon Copy)
- **Format**: Same as "To" parameter
- **Purpose**: Recipients who should receive a copy (hidden from other recipients)
- **Optional**: Can be null or empty

#### Reply To
- **Format**: Single email address
- **Purpose**: Email address where replies should be sent
- **Default**: If not specified, replies go to the sender's email address
- **Optional**: Can be null or empty

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| **Result** | Text | Success message: `"Mail sent successfully."` |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Subject is required | `"Subject cannot be null or empty"` | Provide a subject for the email |
| Message is required | `"Message cannot be null or empty"` | Provide email body content |
| To is required | `"To recipients cannot be null or empty"` | Provide at least one recipient email address |
| Invalid email format | `"Invalid email address format"` | Use valid email addresses (e.g., user@example.com) |
| Attachment size exceeds limit | `"Attachment size exceeds maximum allowed size"` | Reduce attachment size or split into multiple emails |
| SMTP authentication failed | `"Failed to authenticate with SMTP server"` | Verify OAuth credentials and re-authenticate |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing required parameters

**Common Scenarios:**
- Subject is null or empty
- Message is null or empty
- To recipients is null or empty
- Invalid email address format

**Error Message**: Specific validation error (e.g., `"Subject cannot be null or empty"`)

**Resolution**:
1. Verify all required parameters are provided
2. Check email address format (must include @ and domain)
3. Ensure Subject and Message are not empty

### System Errors (SYSTEM_ERROR)

**Cause**: SMTP server connection or authentication failures

**Common Scenarios:**
- OAuth token expired
- SMTP server unreachable
- Network connectivity issues
- Attachment size exceeds provider limit

**Error Message**: `"Failed to send email: [specific error]"`

**Resolution**:
1. Click "Test Connection" to refresh OAuth token
2. Verify network connectivity to SMTP server
3. Check attachment sizes
4. Retry the operation

### Authorization Errors

**Cause**: OAuth authentication failure

**Common Scenarios:**
- Access token expired and refresh failed
- User revoked app permissions
- Invalid OAuth credentials

**Error Message**: `"Authentication failed"` or `"Failed to test connection"`

**Resolution**:
1. Re-authenticate by clicking "Test Connection"
2. Grant all requested permissions
3. Verify OAuth credentials in extension configuration

## Usage Examples

### Example 1: Simple Email

**Input**:
```
Subject: "Hello from Krista"
Message: "This is a test email sent via Krista Email Extension."
To: "recipient@example.com"
Attachments: (none)
Cc: (none)
Bcc: (none)
Reply To: (none)
```

**Output**:
```
"Mail sent successfully."
```

**Result**: Email sent to recipient@example.com with plain text message

### Example 2: Email with Attachments and CC

**Input**:
```
Subject: "Monthly Report - January 2024"
Message: "<p>Please find the monthly report attached.</p><p>Best regards,<br>Finance Team</p>"
To: "manager@example.com"
Attachments: [report.pdf, summary.xlsx]
Cc: "team@example.com"
Bcc: (none)
Reply To: "finance@example.com"
```

**Output**:
```
"Mail sent successfully."
```

**Result**: Email sent to manager with 2 attachments, CC to team, replies go to finance@example.com

### Example 3: Multiple Recipients with BCC

**Input**:
```
Subject: "Team Meeting Reminder"
Message: "<h2>Meeting Tomorrow</h2><p>Don't forget our team meeting at 10 AM.</p>"
To: "team1@example.com; team2@example.com; team3@example.com"
Attachments: [agenda.pdf]
Cc: (none)
Bcc: "archive@example.com"
Reply To: (none)
```

**Output**:
```
"Mail sent successfully."
```

**Result**: Email sent to 3 team members with agenda attached, BCC to archive (hidden from recipients)

## Business Rules

1. **Semicolon Conversion**: Semicolons in To, Cc, Bcc, and Reply To fields are automatically converted to commas
2. **From Address**: Email is always sent from the configured email address (cannot be changed per request)
3. **Attachment Size**: Total attachment size must not exceed email provider limits (25MB Gmail, 35MB Outlook)
4. **HTML Support**: Message field supports full HTML formatting including inline styles
5. **No Retry**: This operation is not retryable to prevent duplicate emails

## Limitations

1. **Attachment Size Limits**:
   - Gmail: 25 MB total
   - Outlook: 35 MB total (individual files limited to 20 MB)

2. **Recipient Limits**:
   - Gmail: 500 recipients per day (To + Cc + Bcc combined)
   - Outlook: 500 recipients per message

3. **Rate Limits**:
   - Gmail: 500 emails per day for free accounts, 2000 for Google Workspace
   - Outlook: Varies by account type

4. **HTML Rendering**: Email clients may render HTML differently; test with target email clients

## Best Practices

### 1. Use Descriptive Subjects
Always provide clear, descriptive subject lines that help recipients understand the email purpose.

### 2. Validate Email Addresses
Validate email addresses before sending to avoid delivery failures.

### 3. Optimize Attachments
- Compress large files before attaching
- Use cloud storage links for very large files
- Consider splitting large attachments across multiple emails

### 4. Use BCC for Mass Emails
When sending to multiple recipients who don't need to see each other's addresses, use BCC instead of To.

### 5. Set Reply-To for No-Reply Emails
If you don't want recipients to reply, set Reply-To to a no-reply address and mention this in the email body.

### 6. Test HTML Formatting
Test HTML emails in multiple email clients (Gmail, Outlook, Apple Mail) to ensure consistent rendering.

## Common Use Cases

### 1. Automated Notifications
```
Scenario: Send automated notification when a workflow completes
Action: Use Send Mail with dynamic subject and message
Result: Users receive timely notifications about workflow status
```

### 2. Report Distribution
```
Scenario: Send daily/weekly reports to stakeholders
Action: Use Send Mail with report file as attachment
Result: Stakeholders receive reports automatically without manual intervention
```

### 3. Customer Communication
```
Scenario: Send personalized emails to customers
Action: Use Send Mail with customer-specific content and attachments
Result: Customers receive personalized communication
```

## Related Catalog Requests

- [Fetch Mails](pages/FetchMails.md) - Retrieve emails based on filters
- [Move Message](pages/MoveMessage.md) - Move sent emails to specific folders
- [Retrieve Unread Emails From Folder](pages/RetrieveUnreadEmailsFromFolder.md) - Check for replies

## Technical Implementation

### Helper Class
- **Class**: MessageBuilder
- **Package**: `app.krista.extensions.essentials.collaboration.email.service`
- **Validation**: Subject, Message, and To parameters validated before sending
- **Service**: SmtpClient.send()

### Email Construction Flow
1. Replace semicolons with commas in recipient fields
2. Create MessageBuilder with SMTP session
3. Set subject, message body, from, to, cc, bcc, reply-to
4. Attach files (with size validation)
5. Build javax.mail.Message object
6. Send via SMTP client

### Telemetry Metrics
- `email.send.success` - Email sent successfully
- `email.send.failure` - Email send failed
- `email.send.attachment_count` - Number of attachments
- `email.send.recipient_count` - Total recipients (To + Cc + Bcc)

## Troubleshooting

### Issue: "Mail sent successfully" but recipient didn't receive email

**Cause**: Email may be in spam folder or blocked by recipient's email server

**Solution**:
1. Ask recipient to check spam/junk folder
2. Add sender to recipient's safe senders list
3. Verify sender's domain has proper SPF/DKIM records

### Issue: "Attachment size exceeds maximum allowed size"

**Cause**: Total attachment size exceeds provider limit

**Solution**:
1. Compress files before attaching
2. Split attachments across multiple emails
3. Use cloud storage (Google Drive, OneDrive) and send links instead

### Issue: "Failed to authenticate with SMTP server"

**Cause**: OAuth token expired or invalid

**Solution**:
1. Click "Test Connection" in extension configuration
2. Re-authenticate and grant permissions
3. Retry sending email

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - SMTP server configuration
- [Authentication](pages/Authentication.md) - OAuth 2.0 authentication
- [Mail Details Entity](pages/MailDetailsEntity.md) - Email entity structure
- [Troubleshooting](pages/Troubleshooting.md) - Common issues and solutions

