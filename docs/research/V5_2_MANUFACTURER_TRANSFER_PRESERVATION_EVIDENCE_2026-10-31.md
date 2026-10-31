# V5.2-B — Manufacturer-supported transfer and original-preservation evidence

**Date:** 2026-10-31
**Scope:** public-document research and a prepared evidence request. No message was sent to a manufacturer, seller, or support contact. No hardware or network protocol was exercised.

## Finding

Public product/manual material documents transfer of AIMB-G1-family media through the companion app over Wi-Fi. The reviewed material says to use the app's Import/save flow, but does **not** promise that source media remains on the glasses after transfer. No public, manufacturer-authorized “keep original,” read-only export, USB mass-storage, removable-card, alternate endpoint, or backup mode was found in the sources reviewed.

Accordingly, the only documented ordinary path found is the Cyan/CyanGlasses app's Wi-Fi import flow, but its original-retention property is **undocumented and contradicted as a safe assumption by this project's v0.6.7 observation**. It is not cleared for use by K G1. No currently evidenced manufacturer-supported preserving path satisfies the owner requirement.

## Source assessment

| Source | Source status | What it documents | Preservation evidence |
|---|---|---|---|
| AIMB-G1 manual filed with FCC ID `2BN53-AIMB-G1`, applicant Shenzhen Huaxinchen Technology Co., Ltd. | Regulatory-hosted manual associated with the device filing; strongest device-specific public manual found. The filing identifies the applicant and the manual, but is not a transfer protocol specification. | CyanGlasses pairing; app album; click Import to save photos/audio to phone; Wi-Fi transfer needs app permissions. | None. It does not say whether Import copies, moves, hides, or deletes the source. |
| Vikor Technology “G1 AI Glasses” product page | Vendor product listing, not established here as the OEM or firmware authority for the user's unit. | AIMB-G1 listing; Wi-Fi file transfer; Cyan Glasses app for file management; NAND storage and media formats. | None. Transfer/file-management wording gives no source-retention guarantee. |
| Shenzhen Vidhon “Intelligent AI Camera Glasses G1” product/spec page | Supplier/OEM-style listing with matching G1/Cyan details; exact relationship to the user's manufactured unit/firmware not established. | Wi-Fi transmission, app transfer for video/audio/photos, Cyan Glasses app, listed media formats. | None. No keep-original option or semantics stated. |
| Kelanatech HeyCyan user manual page | Retailer/support-style instructions for a HeyCyan product family, not sufficient to establish it applies to this AIMB-G1 hardware/firmware. | App Import and save-to-phone flow. | None. Not used to establish device-specific support. |

### Source and scope notes

- FCC filing/manual identification: `https://fccid.io/2BN53-AIMB-G1/User-Manual/User-manual-8453862` (document ID 8453862; SHA-256 shown by the filing mirror as `7cd49c013077d52bbaf8fd2a829a218aad39fc5e6022c14b92e63f30e8066496`). The filing mirror links the exhibit submitted to FCC at `https://apps.fcc.gov/eas/GetApplicationAttachment.html?id=8453862`; prefer that FCC-hosted exhibit when archiving the document.
- Vikor product listing: `https://www.vikor.com.cn/products/1779381687789`.
- Vidhon product/spec listing: `https://www.szvidhon.com/product/intelligent-ai-camera-glasses-g1.html`.
- Kelanatech guide: `https://kelanatech.com/pages/ai-smart-glasses-user-manual`.
- Reviewed 2026-10-31. Product listings and variant manuals may describe related but non-identical devices; chipset, storage capacity, camera, formats, controls and apps differ across public listings. None of these differences should be silently mapped onto the user's unit. Exact device and firmware confirmation is required.

## Prepared manufacturer/authorized-support evidence request (NOT SENT)

Subject: **AIMB-G1: confirm supported media transfer that retains originals on glasses**

> We use AIMB-G1 glasses with the Cyan Glasses application. Our requirement is to copy photos, videos, and audio to a phone while keeping the original media available on the glasses.
>
> Please answer for the exact AIMB-G1 hardware revision and firmware version we provide:
>
> 1. Does the supported Cyan/CyanGlasses “Import” or “Save” operation retain each original on the glasses after a successful transfer? Please distinguish file bytes stored on the glasses from whether the item remains visible in the app/catalog.
> 2. Our controlled record shows one successful 599,645-byte JPG media GET followed by BLE transfer EXIT and Wi-Fi teardown; after reconnect, that exact item was absent from the remote catalog. Does this behavior match the intended firmware? Was the item deleted, marked imported, hidden, or moved to another state?
> 3. Which event changes the source state: successful GET/body completion, HTTP response/connection close, an app acknowledgment, BLE EXIT (observed command bytes `02 01 09`), Wi-Fi group teardown, or an asynchronous firmware task? Please cite a specification, source-code section, or diagnostic trace if available.
> 4. Is there an officially supported option, app setting, SDK/API, endpoint, desktop utility, cable/storage mode, or firmware that copies media without deleting or hiding originals? Please provide exact instructions, supported models/firmware, media types, and limitations.
> 5. For failed/interrupted transfers, what are the supported retry and recovery semantics? Can a repeated request consume, hide, or duplicate an item?
> 6. Is the remote catalog an authoritative view of files physically retained on the device? If an item disappears from the catalog, what supported procedure can establish whether its bytes remain?
> 7. Can you provide a written, model/firmware-scoped assurance that a supported method preserves originals, and a test/verification procedure that does not risk user media?
>
> Please do not ask us to test on irreplaceable or client media. We will only consider a later disposable-media test under a separately approved plan.

Before any request is sent, the exact serial/model marking, app version, firmware version, purchase/channel information, and recipient authority should be confirmed privately and supplied only as needed. Do not publish identifying device values, addresses, or credentials in this repository.

## Acceptance bar for a vendor claim

A general “use Import,” “the app copies media,” or marketing statement about Wi-Fi transfer is insufficient. A response is actionable only if it:

1. identifies the exact hardware/model and firmware range;
2. explicitly states what happens to source bytes and catalog visibility after success;
3. names the supported method, versions, media types and failure/retry behavior;
4. distinguishes a copy from a move/hide/import-state transition; and
5. provides an independently reviewable source/specification or a safe vendor-defined validation procedure.

Until that evidence exists, keep real media GET disabled and preserve the current NO-GO decision.
