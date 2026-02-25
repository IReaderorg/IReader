# Continuity Ledger

## Goal (incl. success criteria)
- Fix character art sharing showing pending/ URLs instead of approved/ URLs
- Success: Shared character art uses approved/ folder URLs after approval

## Constraints/Assumptions
- Kotlin Multiplatform project (Android/iOS)
- Using Cloudflare R2 for character art images
- Using Supabase for metadata storage
- Images stored in "ireader-character-art" bucket
- Approval flow: pending/ → approved/ folder move

## Key Decisions
- Root cause: x-amz-copy-source header wasn't included in AWS Signature V4 calculation
- Fix: Modified appendAwsHeaders() to accept copySource parameter and include it in signature
- AWS Sig V4 requires ALL headers starting with x-amz- to be in canonical headers (sorted alphabetically)

## State

### Done
- ✅ Investigated character art sharing functionality
- ✅ Verified code flow for approval process
- ✅ User confirmed: shared URLs still point to pending/ folder
- ✅ Found root cause: x-amz-copy-source header not included in AWS signature
- ✅ Fixed CloudflareR2DataSource.kt:
  - Added copySource parameter to appendAwsHeaders()
  - Include x-amz-copy-source in canonical headers for signing
  - Updated approveImage() to pass copySource to signature

### Now
- Fix complete, ready for testing

### Next
- User will test approving new character art
- Verify approved art URLs use approved/ folder
- Test sharing on Discord

## Open Questions
- None

## Working Set (files/ids/commands)
- data/src/commonMain/kotlin/ireader/data/characterart/CloudflareR2DataSource.kt (approveImage method)
- data/src/commonMain/kotlin/ireader/data/characterart/CharacterArtDataSource.kt (approveArt method)
- data/src/commonMain/kotlin/ireader/data/characterart/SupabaseCharacterArtMetadata.kt (approveArt method)
