# Move Message

## Overview

Moves an email message from one folder to another based on the message ID. This operation is useful for organizing emails, archiving messages, or implementing custom email workflows.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes - Operation is idempotent; moving the same message multiple times has the same effect

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| **Message ID** | Text | Yes | Unique identifier of the email message to move | `"<CABc123@mail.gmail.com>"` |
| **Folder Name** | Text | Yes | Destination folder name where the message should be moved | `"Archive"`, `"Processed"`, `"INBOX"` |

### Parameter Details

#### Message ID
- **Format**: Email message ID (typically enclosed in angle brackets)
- **Source**: Obtained from [Fetch Mails](pages/FetchMails.md) or [Retrieve Unread Emails](pages/RetrieveUnreadEmailsFromFolder.md) catalog requests
- **Validation**: Must be a valid message ID
- **Example**: `"<CABc123xyz@mail.gmail.com>"`

> **📝 Note**: Message IDs are unique identifiers assigned by email servers. They are returned in the "Message ID" field of the Mail Details entity.

#### Folder Name
- **Format**: Folder name as it appears in the email client
- **Case Sensitive**: Yes (use exact folder name)
- **Common Folders**:
  - Gmail: `"INBOX"`, `"[Gmail]/Sent Mail"`, `"[Gmail]/Trash"`, `"[Gmail]/All Mail"`
  - Outlook: `"INBOX"`, `"Sent Items"`, `"Deleted Items"`, `"Archive"`
- **Custom Folders**: Any user-created folder name

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| **Result** | Text | Success message indicating the message was moved |

**Example Output**: `"Message moved successfully to Archive"`

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Message ID is required | `"Message ID cannot be null or empty"` | Provide a valid message ID |
| Folder Name is required | `"Folder Name cannot be null or empty"` | Provide a destination folder name |
| Message not found | `"Message with ID [id] not found"` | Verify the message ID is correct and the message exists |
| Folder not found | `"Folder [name] not found"` | Verify the folder name is correct (case-sensitive) |
| IMAP connection failed | `"Failed to connect to IMAP server"` | Check network connectivity and OAuth credentials |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing required parameters

**Common Scenarios:**
- Message ID is null or empty
- Folder Name is null or empty
- Invalid message ID format

**Error Message**: Specific validation error

**Resolution**:
1. Verify Message ID is provided and valid
2. Verify Folder Name is provided and correct
3. Check message ID format (should include angle brackets)

### System Errors (SYSTEM_ERROR)

**Cause**: IMAP server connection or operation failures

**Common Scenarios:**
- Message not found in source folder
- Destination folder doesn't exist
- IMAP connection timeout
- OAuth token expired

**Error Message**: `"Failed to move message: [specific error]"`

**Resolution**:
1. Verify message exists in the source folder
2. Verify destination folder exists (create it if needed)
3. Click "Test Connection" to refresh OAuth token
4. Check network connectivity to IMAP server
5. Retry the operation

## Usage Examples

### Example 1: Move Email to Archive

**Scenario**: Move a processed email to Archive folder

**Input**:
```
Message ID: "<CABc123xyz@mail.gmail.com>"
Folder Name: "Archive"
```

**Output**:
```
"Message moved successfully to Archive"
```

**Result**: Email is moved from current folder (e.g., INBOX) to Archive folder

### Example 2: Move Email to Custom Folder (Gmail)

**Scenario**: Move email to a custom label/folder in Gmail

**Input**:
```
Message ID: "<CADef456abc@mail.gmail.com>"
Folder Name: "Processed Orders"
```

**Output**:
```
"Message moved successfully to Processed Orders"
```

**Result**: Email is moved to the "Processed Orders" label in Gmail

### Example 3: Move Email to Sent Items (Outlook)

**Scenario**: Move a draft email to Sent Items

**Input**:
```
Message ID: "<abc123@outlook.com>"
Folder Name: "Sent Items"
```

**Output**:
```
"Message moved successfully to Sent Items"
```

**Result**: Email is moved to Sent Items folder in Outlook

## Business Rules

1. **Source Folder Detection**: The extension automatically detects the current folder of the message
2. **Idempotent Operation**: Moving the same message multiple times to the same folder has no additional effect
3. **Message ID Uniqueness**: Message IDs are globally unique and persistent
4. **Folder Creation**: Destination folder must exist; the extension does not create folders automatically
5. **Gmail Labels**: In Gmail, moving a message applies the destination label and removes the source label

## Limitations

1. **Folder Must Exist**: Destination folder must already exist; operation fails if folder doesn't exist
2. **Message Must Exist**: Message must exist in the mailbox; operation fails if message was deleted
3. **IMAP Protocol**: Uses IMAP COPY + DELETE operations (not a true "move" at protocol level)
4. **Gmail Behavior**: Gmail uses labels, so "moving" actually changes labels
5. **Rate Limits**: Subject to IMAP connection limits (15 concurrent for Gmail, 20 for Outlook)

## Best Practices

### 1. Verify Folder Exists
Before moving messages, verify the destination folder exists using folder listing operations.

### 2. Handle Errors Gracefully
Implement error handling for cases where messages or folders don't exist.

### 3. Use Correct Folder Names
- Gmail: Use exact label names (e.g., `"[Gmail]/Sent Mail"` not `"Sent"`)
- Outlook: Use exact folder names (e.g., `"Sent Items"` not `"Sent"`)

### 4. Batch Operations
If moving multiple messages, consider batching operations to reduce IMAP connections.

### 5. Log Message IDs
Keep track of message IDs for audit trails and troubleshooting.

## Common Use Cases

### 1. Email Workflow Automation
```
Scenario: Automatically archive processed emails
Workflow:
1. Fetch unread emails from INBOX
2. Process each email
3. Move processed emails to "Processed" folder
Result: Organized mailbox with clear separation of processed/unprocessed emails
```

### 2. Email Classification
```
Scenario: Classify emails based on content
Workflow:
1. Fetch emails from INBOX
2. Analyze email content (subject, sender, etc.)
3. Move to appropriate folder (e.g., "Orders", "Support", "Marketing")
Result: Automatically organized emails by category
```

### 3. Email Archival
```
Scenario: Archive old emails
Workflow:
1. Fetch emails older than 30 days
2. Move to "Archive" folder
Result: Clean INBOX with archived historical emails
```

## Related Catalog Requests

- [Fetch Mails](pages/FetchMails.md) - Retrieve emails to get message IDs
- [Retrieve Unread Emails From Folder](pages/RetrieveUnreadEmailsFromFolder.md) - Get unread emails with message IDs
- [Send Mail](pages/SendMail.md) - Send emails

## Technical Implementation

### Service Class
- **Class**: EmailClient → ImapClient
- **Method**: `enqueueMessageForMove(folderName, messageId)`
- **Protocol**: IMAP COPY + DELETE

### Operation Flow
1. Decode message ID from input
2. Connect to IMAP server with OAuth authentication
3. Search for message by message ID
4. Copy message to destination folder
5. Mark original message for deletion
6. Expunge deleted messages
7. Return success message

### IMAP Commands Used
```
1. SELECT [source_folder]
2. SEARCH HEADER Message-ID [message_id]
3. COPY [message_number] [destination_folder]
4. STORE [message_number] +FLAGS (\Deleted)
5. EXPUNGE
```

### Telemetry Metrics
- `email.move.success` - Message moved successfully
- `email.move.failure` - Message move failed
- `email.move.message_not_found` - Message ID not found
- `email.move.folder_not_found` - Destination folder not found

## Troubleshooting

### Issue: "Message with ID [id] not found"

**Cause**: Message doesn't exist or was already deleted

**Solution**:
1. Verify message ID is correct
2. Check if message was already moved or deleted
3. Fetch fresh list of messages to get current message IDs

### Issue: "Folder [name] not found"

**Cause**: Destination folder doesn't exist or folder name is incorrect

**Solution**:
1. Verify folder name is correct (case-sensitive)
2. For Gmail: Use `"[Gmail]/Sent Mail"` not `"Sent"`
3. For Outlook: Use `"Sent Items"` not `"Sent"`
4. Create the folder manually in email client if it doesn't exist

### Issue: "Failed to connect to IMAP server"

**Cause**: IMAP connection failure or OAuth token expired

**Solution**:
1. Click "Test Connection" to refresh OAuth token
2. Verify network connectivity to IMAP server
3. Check IMAP server configuration (imap.gmail.com or outlook.office365.com)

### Issue: Message appears in both source and destination folders (Gmail)

**Cause**: Gmail uses labels, not folders; message may have multiple labels

**Solution**:
This is expected Gmail behavior. The message has both labels. To remove from source:
1. The extension automatically removes the source label
2. If message still appears, it may have "All Mail" label
3. This is normal Gmail behavior

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - IMAP server configuration
- [Authentication](pages/Authentication.md) - OAuth 2.0 authentication
- [Mail Details Entity](pages/MailDetailsEntity.md) - Email entity structure with Message ID field

