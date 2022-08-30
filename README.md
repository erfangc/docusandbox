# docusandbox

This application demonstrates how to fill out a PDF and send it to DocuSign for signature

## Concepts

- `Template` - A template is a PDF file with an unfilled AcroForm
- `Form` - Each time someone attempts to fill out this PDF file, we copy the template and create a `Form` object
- `Envelope` - When the user fully fills out a `Form` (just prior to signature) we stick the `Form` into an DocuSign `Envelope` and send it off for signing

## Sequence Diagram

```mermaid
sequenceDiagram
    participant ts as TemplatesService
    actor mgr as Template Manger
    actor user as User
    participant up as UserProfileService
    participant fs as FormsService
    participant ds as DocuSign
    
    note over mgr: ... creates the AcroForm in Adobe, uploads it via createTemplate()
    mgr->>ts: createTemplate()
    
    note over ts: TemplatesService memorizes The fields contained in the AcroForm. <br/> Template manager can update fields for auto-fill
    mgr->>ts: updateField() 
    
    user->>up: createUserProfile(email = "...")
    up-->>user: { ... }
    
    user->>fs: createForm(templateFilename = "...", input = "...")
    fs-->>user: { formId }
    
    note over user: ... user can come back modify form before submission
    user->>fs: updateForm(input = "...")
    
    user->>fs: submitForm()
    note over fs: FormsService uses data from UserProfile and Form's bespoke input to fill out the PDF, respecting each field's `autoFillInstruction` where provided <br/>. The completed PDF shall be sent to DocuSign for signing
    fs->>up: getUserProfile()
    up-->fs: { userProfile }
    fs->>ts: getTemplate()
    ts-->>fs: { template }
    fs-->>fs: Combines template, userProfile and forms.input - then uses internal FormFiller service to fill out the PDF
    fs->>ds: createEnvelope()
    
    note over ds: When all recipients completes signing ...
    ds->>fs: Notifies signing complete
```