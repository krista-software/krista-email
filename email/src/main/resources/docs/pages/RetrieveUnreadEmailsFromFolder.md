# Retrieve Unread Emails From Folder

## Overview

Retrieves unread emails from a specified folder with pagination support. This catalog request is ideal for fetching unread emails on-demand without tracking last fetch time, and supports large result sets through page-based retrieval.

## Request Details

- **Area**: Imap
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes - Read-only operation, safe to retry

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| **Folder name** | Text | Yes | Folder name to retrieve unread emails from | `"INBOX"`, `"Sent Items"` |
| **Page Index** | Number | No | Zero-based page index for pagination (default: 0) | `0`, `1`, `2` |
| **Page Size** | Number | No | Number of emails per page (default: 50) | `10`, `50`, `100` |

### Parameter Details

#### Folder name
- **Required**: Yes
- **Format**: Folder name as it appears in email client
- **Case Sensitive**: Yes
- **Common Values**:
  - Gmail: `"INBOX"`, `"[Gmail]/Sent Mail"`, `"[Gmail]/All Mail"`
  - Outlook: `"INBOX"`, `"Sent Items"`, `"Deleted Items"`
- **Custom Folders**: Any user-created folder name

#### Page Index
- **Required**: No
- **Type**: Number (Double)
- **Default**: 0 (first page)
- **Format**: Zero-based index (0 = first page, 1 = second page, etc.)
- **Example**: 
  - `0` - First 50 emails (if Page Size = 50)
  - `1` - Emails 51-100
  - `2` - Emails 101-150

#### Page Size
- **Required**: No
- **Type**: Number (Double)
- **Default**: 50
- **Recommended Range**: 10-100
- **Maximum**: No hard limit, but large page sizes may cause performance issues
- **Example**:
  - `10` - Retrieve 10 emails per page
  - `50` - Retrieve 50 emails per page (default)
  - `100` - Retrieve 100 emails per page

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| **Mail Details** | [ Entity(Mail Details) ] | List of unread emails from the specified folder |

**Output Structure**: Array of [Mail Details](pages/MailDetailsEntity.md) entities

**Example Output**:
```json
[
  {
    "From": "sender@example.com",
    "To": "recipient@example.com",
    "Subject": "Unread Email 1",
    "Message": "<p>This email is unread.</p>",
    "Message ID": "<abc123@mail.gmail.com>",
    "Is Read": false,
    "Received Date and Time": 1704326400000,
    "File Attachment": [...]
  },
  {
    "From": "another@example.com",
    "To": "recipient@example.com",
    "Subject": "Unread Email 2",
    "Message": "<p>Another unread email.</p>",
    "Message ID": "<def456@mail.gmail.com>",
    "Is Read": false,
    "Received Date and Time": 1704330000000
  }
]
```

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Folder name is required | `"Folder name cannot be null or empty"` | Provide a folder name |
| Folder not found | `"Folder [name] not found"` | Verify folder name is correct (case-sensitive) |
| Invalid page index | `"Page Index must be >= 0"` | Provide a non-negative page index |
| Invalid page size | `"Page Size must be > 0"` | Provide a positive page size |
| IMAP connection failed | `"Failed to connect to IMAP server"` | Check network and OAuth credentials |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing required parameters

**Common Scenarios:**
- Folder name is null or empty
- Invalid folder name
- Negative page index
- Zero or negative page size

**Resolution**:
1. Verify Folder name is provided
2. Check folder name is correct (case-sensitive)
3. Ensure Page Index >= 0
4. Ensure Page Size > 0

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

### Empty Result

**Scenario**: No unread emails in folder

**Output**: Empty array `[]`

**Note**: This is not an error; it indicates all emails in the folder have been read.

## Usage Examples

### Example 1: Retrieve First Page of Unread Emails

**Input**:
```
Folder name: "INBOX"
Page Index: (empty - defaults to 0)
Page Size: (empty - defaults to 50)
```

**Output**:
```json
[
  { "Subject": "Unread Email 1", "Is Read": false, ... },
  { "Subject": "Unread Email 2", "Is Read": false, ... },
  ...
  (up to 50 emails)
]
```

**Result**: First 50 unread emails from INBOX

### Example 2: Retrieve Specific Page with Custom Page Size

**Input**:
```
Folder name: "INBOX"
Page Index: 1
Page Size: 20
```

**Output**:
```json
[
  { "Subject": "Unread Email 21", "Is Read": false, ... },
  { "Subject": "Unread Email 22", "Is Read": false, ... },
  ...
  (emails 21-40)
]
```

**Result**: Second page (emails 21-40) with 20 emails per page

### Example 3: Retrieve Unread Emails from Custom Folder

**Input**:
```
Folder name: "Support Requests"
Page Index: 0
Page Size: 10
```

**Output**:
```json
[
  { "Subject": "Support Request #123", "Is Read": false, ... },
  { "Subject": "Support Request #124", "Is Read": false, ... },
  ...
  (up to 10 emails)
]
```

**Result**: First 10 unread emails from "Support Requests" folder

### Example 4: No Unread Emails

**Input**:
```
Folder name: "INBOX"
Page Index: 0
Page Size: 50
```

**Output**:
```json
[]
```

**Result**: Empty array (all emails have been read)

## Business Rules

1. **Unread Only**: Only retrieves emails with "Is Read" = false
2. **Pagination**: Supports page-based retrieval for large result sets
3. **Zero-Based Indexing**: Page Index starts at 0 (first page)
4. **Default Values**: Page Index defaults to 0, Page Size defaults to 50
5. **Empty Result**: Returns empty array if no unread emails exist
6. **No Time Tracking**: Does not track last fetch time (unlike [Fetch Mails](pages/FetchMails.md))

## Limitations

1. **Unread Status**: Only retrieves unread emails; read emails are excluded
2. **Folder Scope**: Searches only the specified folder (not subfolders)
3. **No Filtering**: Does not support sender, subject, or To/Cc filters (use [Fetch Mails](pages/FetchMails.md) for filtering)
4. **Page Size Performance**: Very large page sizes (>100) may cause performance issues
5. **IMAP Limits**: Subject to IMAP connection limits (15 concurrent for Gmail, 20 for Outlook)

## Best Practices

### 1. Use Pagination for Large Mailboxes
If you have many unread emails, use pagination to retrieve them in manageable chunks.

**Example**:
```
Page 1: Page Index = 0, Page Size = 50 (emails 1-50)
Page 2: Page Index = 1, Page Size = 50 (emails 51-100)
Page 3: Page Index = 2, Page Size = 50 (emails 101-150)
```

### 2. Choose Appropriate Page Size
- **Small mailboxes**: Page Size = 50 (default)
- **Large mailboxes**: Page Size = 100
- **Real-time processing**: Page Size = 10-20

### 3. Handle Empty Results
Always check if the result array is empty before processing.

```javascript
if (emails.length === 0) {
  // No unread emails
} else {
  // Process emails
}
```

### 4. Mark Emails as Read After Processing
After processing unread emails, consider marking them as read to avoid reprocessing.

### 5. Use Specific Folders
Retrieve from specific folders (e.g., INBOX) rather than "All Mail" for better performance.

## Common Use Cases

### 1. Unread Email Dashboard
```
Scenario: Display unread email count and list
Workflow:
1. Call Retrieve Unread Emails with Page Index = 0, Page Size = 10
2. Display first 10 unread emails
3. Show total unread count
Result: User sees latest unread emails
```

### 2. Batch Email Processing
```
Scenario: Process all unread emails in batches
Workflow:
1. Page 1: Retrieve Page Index = 0, Page Size = 50
2. Process 50 emails
3. Page 2: Retrieve Page Index = 1, Page Size = 50
4. Process next 50 emails
5. Repeat until empty result
Result: All unread emails processed in batches
```

### 3. Priority Email Handling
```
Scenario: Process unread emails from high-priority folder
Workflow:
1. Retrieve unread emails from "VIP" folder
2. Process each email with high priority
3. Send notifications
Result: VIP emails handled immediately
```

## Related Catalog Requests

- [Fetch Mails](pages/FetchMails.md) - Alternative for time-based fetching with filters
- [Move Message](pages/MoveMessage.md) - Move processed emails to another folder
- [Send Mail](pages/SendMail.md) - Send automated responses

## Technical Implementation

### Service Class
- **Class**: ImapArea
- **Method**: `retrieveUnreadEmailsFromFolder(folderName, pageIndex, pageSize)`
- **Protocol**: IMAP SEARCH UNSEEN

### Operation Flow
1. Connect to IMAP folder
2. Open folder in read-only mode
3. Check if folder has messages
4. Search for unread emails (UNSEEN flag)
5. Apply pagination (Page Index * Page Size)
6. Retrieve email details
7. Convert to MailDetails entities
8. Return list

### IMAP Commands Used
```
1. SELECT [folder_name]
2. SEARCH UNSEEN
3. FETCH [message_numbers] (ENVELOPE BODY[])
```

### Pagination Logic
```java
int startIndex = pageIndex * pageSize;
int endIndex = startIndex + pageSize;
List<Email> pagedEmails = allUnreadEmails.subList(startIndex, Math.min(endIndex, allUnreadEmails.size()));
```

### Telemetry Metrics
- `email.retrieve_unread.success` - Unread emails retrieved successfully
- `email.retrieve_unread.count` - Number of unread emails retrieved
- `email.retrieve_unread.page_index` - Page index used
- `email.retrieve_unread.page_size` - Page size used

## Troubleshooting

### Issue: Empty result even though unread emails exist

**Cause**: Folder name is incorrect or emails are marked as read

**Solution**:
1. Verify folder name is correct (case-sensitive)
2. Check email client to confirm unread emails exist
3. Verify "Is Read" status in email client

### Issue: "Folder [name] not found"

**Cause**: Folder name is incorrect or doesn't exist

**Solution**:
1. Verify folder name is correct (case-sensitive)
2. For Gmail: Use `"INBOX"` not `"Inbox"`
3. For Outlook: Use `"Sent Items"` not `"Sent"`
4. Check folder exists in email client

### Issue: Page Index returns empty result

**Cause**: Page Index exceeds available pages

**Solution**:
1. Check total unread email count
2. Calculate max page index: `Math.floor(totalUnreadEmails / pageSize)`
3. Use valid page index within range

### Issue: Performance is slow with large Page Size

**Cause**: Retrieving too many emails at once

**Solution**:
1. Reduce Page Size to 50 or less
2. Use pagination to retrieve emails in smaller batches
3. Consider using [Fetch Mails](pages/FetchMails.md) with time-based filtering

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - IMAP server configuration
- [Authentication](pages/Authentication.md) - OAuth 2.0 authentication
- [Mail Details Entity](pages/MailDetailsEntity.md) - Email entity structure
- [Fetch Mails](pages/FetchMails.md) - Alternative time-based fetching with filters

