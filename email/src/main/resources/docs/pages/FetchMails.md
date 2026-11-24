# Fetch Mails

## Overview

Retrieves emails from a specified folder based on time intervals and optional filters (sender, subject, To/Cc). This catalog request is designed for scheduled email fetching, tracking the last fetch time to retrieve only new emails since the last execution.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes - Read-only operation, safe to retry

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| **Folder** | Text | Yes | Folder name to fetch emails from | `"INBOX"`, `"Sent Items"` |
| **Sender** | Text | No | Filter emails by sender email address | `"john@example.com"` |
| **Subject** | Text | No | Filter emails by subject (partial match) | `"Invoice"` |
| **To or Cc** | Text | No | Filter emails by To or Cc recipients | `"support@company.com"` |

### Parameter Details

#### Folder
- **Required**: Yes
- **Format**: Folder name as it appears in email client
- **Case Sensitive**: Yes
- **Common Values**:
  - Gmail: `"INBOX"`, `"[Gmail]/Sent Mail"`, `"[Gmail]/All Mail"`
  - Outlook: `"INBOX"`, `"Sent Items"`, `"Deleted Items"`
- **Tooltip**: "Mails will be fetched from this folder"

#### Sender
- **Required**: No
- **Format**: Email address
- **Matching**: Exact match on sender email address
- **Example**: `"john.doe@example.com"`
- **Use Case**: Fetch emails only from specific senders

#### Subject
- **Required**: No
- **Format**: Text (partial match)
- **Matching**: Case-insensitive substring match
- **Example**: `"Invoice"` matches "Invoice #123", "Monthly Invoice", etc.
- **Use Case**: Fetch emails with specific keywords in subject

#### To or Cc
- **Required**: No
- **Format**: Email address
- **Matching**: Checks if email address appears in To or Cc fields
- **Example**: `"support@company.com"`
- **Use Case**: Fetch emails sent to specific recipients

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| **Mails** | [ Entity(Mail Details) ] | List of emails matching the criteria |

**Output Structure**: Array of [Mail Details](pages/MailDetailsEntity.md) entities

**Example Output**:
```json
[
  {
    "From": "sender@example.com",
    "To": "recipient@example.com",
    "Subject": "Invoice #123",
    "Message": "<p>Please find the invoice attached.</p>",
    "Message ID": "<abc123@mail.gmail.com>",
    "Send Date and Time": 1704326400000,
    "Is Read": false,
    "File Attachment": [...]
  }
]
```

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Folder is required | `"Folder cannot be null or empty"` | Provide a folder name |
| Folder not found | `"Folder [name] not found"` | Verify folder name is correct (case-sensitive) |
| IMAP connection failed | `"Failed to connect to IMAP server"` | Check network and OAuth credentials |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing required parameters

**Common Scenarios:**
- Folder parameter is null or empty
- Invalid folder name

**Resolution**:
1. Verify Folder parameter is provided
2. Check folder name is correct (case-sensitive)
3. Use exact folder names from email client

### System Errors (SYSTEM_ERROR)

**Cause**: IMAP server connection or operation failures

**Common Scenarios:**
- IMAP connection timeout
- OAuth token expired
- Folder doesn't exist
- Network connectivity issues

**Resolution**:
1. Click "Test Connection" to refresh OAuth token
2. Verify network connectivity
3. Check folder exists in email client
4. Retry the operation

## Usage Examples

### Example 1: Fetch All New Emails from INBOX

**Input**:
```
Folder: "INBOX"
Sender: (empty)
Subject: (empty)
To or Cc: (empty)
```

**Output**:
```json
[
  {
    "From": "user1@example.com",
    "Subject": "Meeting Tomorrow",
    "Message": "Don't forget our meeting at 10 AM.",
    ...
  },
  {
    "From": "user2@example.com",
    "Subject": "Project Update",
    "Message": "Here's the latest project status.",
    ...
  }
]
```

**Result**: All emails received since last fetch

### Example 2: Fetch Emails from Specific Sender

**Input**:
```
Folder: "INBOX"
Sender: "invoices@vendor.com"
Subject: (empty)
To or Cc: (empty)
```

**Output**:
```json
[
  {
    "From": "invoices@vendor.com",
    "Subject": "Invoice #12345",
    "Message": "Your invoice is attached.",
    ...
  }
]
```

**Result**: Only emails from invoices@vendor.com

### Example 3: Fetch Emails with Subject Filter

**Input**:
```
Folder: "INBOX"
Sender: (empty)
Subject: "Order"
To or Cc: (empty)
```

**Output**:
```json
[
  {
    "Subject": "Order Confirmation #789",
    ...
  },
  {
    "Subject": "New Order Received",
    ...
  }
]
```

**Result**: Only emails with "Order" in subject

### Example 4: Fetch Emails Sent to Support

**Input**:
```
Folder: "INBOX"
Sender: (empty)
Subject: (empty)
To or Cc: "support@company.com"
```

**Output**:
```json
[
  {
    "To": "support@company.com",
    "Subject": "Help Request",
    ...
  }
]
```

**Result**: Only emails sent to support@company.com

## Business Rules

1. **Time-Based Fetching**: Only fetches emails received since the last fetch time
2. **Last Fetch Time Tracking**: Extension automatically tracks last fetch timestamp
3. **Filter Combination**: All provided filters are applied with AND logic
4. **Empty Result**: Returns empty array if no emails match criteria
5. **Scheduled Execution**: Designed for scheduled/periodic execution (e.g., every 5 minutes)

## Limitations

1. **Time Window**: Only fetches emails since last execution (not historical emails)
2. **First Execution**: On first execution, fetches all emails in folder (no previous timestamp)
3. **Filter Matching**: Filters use exact match (sender, To/Cc) or substring match (subject)
4. **Folder Scope**: Searches only the specified folder (not subfolders)
5. **Rate Limits**: Subject to IMAP connection limits

## Best Practices

### 1. Schedule Regular Execution
Run this catalog request on a schedule (e.g., every 5 minutes) to continuously fetch new emails.

### 2. Use Specific Folders
Fetch from specific folders (e.g., INBOX) rather than "All Mail" to improve performance.

### 3. Apply Filters
Use sender, subject, or To/Cc filters to reduce the number of emails processed.

### 4. Handle Empty Results
Check if the result array is empty before processing emails.

### 5. Process and Move
After processing fetched emails, use [Move Message](pages/MoveMessage.md) to move them to a "Processed" folder.

## Common Use Cases

### 1. Automated Email Processing
```
Scenario: Process incoming support emails
Workflow:
1. Schedule Fetch Mails every 5 minutes from INBOX
2. Filter by To: "support@company.com"
3. Process each email (create ticket, send auto-reply)
4. Move processed emails to "Processed" folder
Result: Automated support ticket creation
```

### 2. Invoice Processing
```
Scenario: Extract and process invoices from emails
Workflow:
1. Schedule Fetch Mails every hour from INBOX
2. Filter by Sender: "invoices@vendor.com"
3. Extract invoice attachments
4. Process invoice data
5. Move to "Processed Invoices" folder
Result: Automated invoice processing
```

### 3. Order Monitoring
```
Scenario: Monitor new order emails
Workflow:
1. Schedule Fetch Mails every 10 minutes from INBOX
2. Filter by Subject: "Order"
3. Parse order details
4. Update order management system
Result: Real-time order tracking
```

## Related Catalog Requests

- [Retrieve Unread Emails From Folder](pages/RetrieveUnreadEmailsFromFolder.md) - Alternative for fetching unread emails with pagination
- [Move Message](pages/MoveMessage.md) - Move processed emails to another folder
- [Send Mail](pages/SendMail.md) - Send automated responses

## Technical Implementation

### Service Class
- **Class**: MessagingArea
- **Method**: `fetchMails(folder, sender, subject, toOrCc)`
- **Storage**: Last fetch time stored in EmailAttributeStore

### Operation Flow
1. Get current request time
2. Retrieve last fetch time from EmailAttributeStore
3. Save current time as new last fetch time
4. Connect to IMAP folder
5. Search for emails received after last fetch time
6. Apply filters (sender, subject, To/Cc)
7. Convert to MailDetails entities
8. Return filtered list

### Time Tracking
```java
long currentRequestTime = System.currentTimeMillis();
long lastRequestProcessedTime = emailStore.getLastFetchMailsRequestTime();
emailStore.saveLastFetchMailsRequestTime(currentRequestTime);
```

### Filtering Logic
- **Sender Filter**: Exact match on From address
- **Subject Filter**: Case-insensitive substring match
- **To/Cc Filter**: Checks if address appears in To or Cc fields

### Telemetry Metrics
- `email.fetch.success` - Emails fetched successfully
- `email.fetch.count` - Number of emails fetched
- `email.fetch.filtered_count` - Number of emails after filtering

## Troubleshooting

### Issue: No emails returned even though new emails exist

**Cause**: Filters are too restrictive or last fetch time is incorrect

**Solution**:
1. Remove filters and test again
2. Check last fetch time in EmailAttributeStore
3. Verify folder name is correct
4. Check email received time vs. last fetch time

### Issue: First execution returns too many emails

**Cause**: No previous fetch time exists, so all emails in folder are returned

**Solution**:
This is expected behavior on first execution. Options:
1. Accept all emails on first run
2. Manually set last fetch time before first execution
3. Use [Retrieve Unread Emails](pages/RetrieveUnreadEmailsFromFolder.md) with pagination instead

### Issue: "Folder [name] not found"

**Cause**: Folder name is incorrect or doesn't exist

**Solution**:
1. Verify folder name is correct (case-sensitive)
2. For Gmail: Use `"INBOX"` not `"Inbox"`
3. For Outlook: Use `"Sent Items"` not `"Sent"`
4. Check folder exists in email client

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - IMAP server configuration
- [Authentication](pages/Authentication.md) - OAuth 2.0 authentication
- [Mail Details Entity](pages/MailDetailsEntity.md) - Email entity structure
