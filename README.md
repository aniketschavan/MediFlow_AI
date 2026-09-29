# MediFlow AI — Smart Hospital Automation & Outpatient Workflow System

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVVM%20%2B%20UDF-008080)](#architecture--design-patterns)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-brightgreen)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34%20(Android%2014)-blue)](https://developer.android.com)

---

## Executive Summary

**MediFlow AI** is an enterprise-grade hospital automation and workflow orchestration platform developed natively for Android using **100% Jetpack Compose** and **Material Design 3**. 

Modern healthcare facilities struggle with fragmented hospital operations: prolonged outpatient waiting times, manual paper prescriptions, disconnected diagnostic laboratories, and manual bed allocations. MediFlow AI unifies the entire clinical journey into a responsive, cohesive digital ecosystem connecting **Patients**, **Doctors**, **Pathologists**, **Pharmacists**, and **Administrators** in real time.

---

## Key Modules & Capabilities

```
┌────────────────────────────────────────────────────────────────────────┐
│                        MediFlow AI Ecosystem                           │
└────────────────────────────────────────────────────────────────────────┘
          │                                           │
    ┌─────▼──────────────────┐                  ┌─────▼──────────────────┐
    │     PATIENT DESK       │                  │      DOCTOR DESK       │
    │  • OPD Token Check-In  │                  │  • Live Queue Monitor  │
    │  • Real-Time Queue Pos │                  │  • Clinical Diagnosis  │
    │  • Prescription View   │                  │  • E-Prescriptions     │
    │  • Billing Breakdown   │                  │  • Lab Test Orders     │
    └─────┬──────────────────┘                  └─────┬──────────────────┘
          │                                           │
          │                   Central Data Hub        │
          └───────────────► [HospitalRepository] ◄────┘
                                  ▲       ▲
          ┌───────────────────────┘       └───────────────────────┐
          │                                                       │
    ┌─────▼──────────────────┐                              ┌─────▼──────────────────┐
    │     DIAGNOSTICS LAB    │                              │     PHARMACY DESK      │
    │  • Test Worklist       │                              │  • Digital Rx Queue    │
    │  • Sample Collection   │                              │  • Stock Verification  │
    │  • Normal Range Entry  │                              │  • Batch Dispensation  │
    └────────────────────────┘                              └────────────────────────┘
                                  ▲
          ┌───────────────────────┴───────────────────────┐
          │                                               │
    ┌─────▼──────────────────┐                      ┌─────▼──────────────────┐
    │    BED MANAGEMENT      │                      │   BILLING OPERATIONS   │
    │  • Ward Floor Status   │                      │  • Unified Invoicing   │
    │  • Real-Time Occupancy │                      │  • Itemized Charges    │
    │  • Patient Admissions  │                      │  • Payment Settlement  │
    └────────────────────────┘                      └────────────────────────┘
```

### 1. Patient Portal & OPD Self-Service
- **Dynamic OPD Token Lifecycle**: Separates permanent patient medical identity (MRN/ID) from transient daily OPD queue tokens. Patients with no active daily visits can self check-in and generate an instant OPD Token.
- **Real-Time Queue Tracking**: Displays dynamic queue status (**Waiting**, **In Consultation**, or **Completed**), estimated wait time, and consulting physician details.
- **Prescription & Lab Access**: Instant view of doctor's clinical prescriptions and pathology test status.
- **Transparent Billing**: Comprehensive breakdown of consultation, lab, and medication charges.

### 2. Doctor OPD Consultation Suite
- **Interactive OPD Queue**: Real-time triage list organized by token status with quick patient call-in.
- **Clinical Encounter Workflow**: Record vitals, symptoms, diagnosis, clinical advice, and follow-up timeline.
- **Integrated Ordering**: Order digital lab investigations (CBC, Lipid Panel, Blood Sugar, X-Ray) and add structured drug prescriptions in one tap.

### 3. Pathology / Diagnostic Laboratory
- **Real-Time Worklist**: Filter investigations by status (`Pending`, `Sample Collected`, `In Progress`, `Completed`).
- **Clinical Result Entry**: Parameter value logging with normal reference intervals and automated status updates.

### 4. Hospital Pharmacy Desk
- **Prescription Fulfillment Queue**: Instant access to doctor-prescribed medications.
- **Drug Inventory & Verification**: Dosage and timing verification with single-tap batch dispensing.

### 5. Inpatient Ward & Bed Management
- **Floor-by-Floor Ward Map**: Visual layout across **ICU**, **General Ward**, **Private Ward**, and **Emergency**.
- **Real-Time Occupancy**: Color-coded bed statuses (`Available`, `Occupied`, `Cleaning`, `Maintenance`).
- **Admission & Transfer Operations**: Direct patient bed assignment and discharge coordination.

### 6. Centralized Billing & Revenue Operations
- **Itemized Ledger**: Auto-aggregated charges for doctor consultation, diagnostic tests, dispensed medications, and bed stay.
- **Payment Processing**: Multi-mode payment tracking (`Paid`, `Pending`, `Partial`) with downloadable/viewable invoice summaries.

### 7. Security & Role-Based Access Control (RBAC)
- **Role Auto-Detection**: Instant credential classification based on email input (`admin@`, `doc@`, `lab@`, `pharm@`).
- **Session Protection**: Dedicated logout confirmation dialogs preventing accidental session termination.

---

## Demo Credentials Reference

You can use the following pre-configured credentials to experience each role in the application:

| Role | Login Identifier | Password | Workflow Focus |
|---|---|---|---|
| **Administrator** | `admin@mediflow.com` | `admin123` | Full Hospital Overview, Bed Management, Invoicing |
| **Doctor / Physician** | `doc@mediflow.com` | `doc123` | OPD Queue, Diagnosis, Lab Ordering, E-Rx |
| **Lab Technician** | `lab@mediflow.com` | `lab123` | Pathology Worklist, Sample Tracking, Result Entry |
| **Pharmacist** | `pharm@mediflow.com` | `pharm123` | Digital Rx Queue, Stock Validation, Dispensing |
| **Patient (Active Visit)** | `9822012345` | `1234` | Rajesh Sharma (Token #101 — In Waiting Queue) |
| **Patient (New / Check-In)** | `9822044321` | `1234` | Pooja Deshmukh (No Token — Try OPD Check-In) |
| **Patient (Completed Visit)** | `9822077441` | `1234` | Ramesh Gaikwad (Outpatient History & Invoicing) |

---

## Architecture & Design Patterns

The codebase adheres strictly to **Clean Architecture** principles and **Unidirectional Data Flow (UDF)**:

```
┌────────────────────────────────────────────────────────┐
│                      UI LAYER                          │
│   Pure Jetpack Compose Screens, Scaffolds, Dialogs     │
└───────────────────────────▲────────────────────────────┘
                            │ Observed StateFlow / Events
┌───────────────────────────┴────────────────────────────┐
│                   VIEWMODEL LAYER                      │
│   State Management, UI Logic, Coroutines Scopes        │
└───────────────────────────▲────────────────────────────┘
                            │ Suspended / Flow Calls
┌───────────────────────────┴────────────────────────────┐
│                  REPOSITORY LAYER                      │
│   HospitalRepository (Single Source of Truth, Caching) │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│                    DATA LAYER                          │
│   Domain Models, Queue Entities, Clinical Records      │
└────────────────────────────────────────────────────────┘
```

- **Zero XML UI Screens**: All screens, navigation bars, cards, and dialogs are written entirely in Jetpack Compose. (XML is reserved strictly for vector adaptive launcher drawables).
- **Design System Standards**: All typography strictly adheres to accessibility standards with font sizes $\ge 12\text{sp}$.
- **Medical Teal Palette**: Primary Teal (`#006D77`), Secondary Teal (`#004D40`), Mint Accent (`#83C5BE`), Surface White (`#FFFFFF`).

---

## Directory Structure

```
E:\Aniket\app\src\main\java\com\example\mediflow_ai\
├── data
│   ├── model              # Core domain models (Patient, Queue, Prescription, Bed, Invoice, etc.)
│   └── repository         # HospitalRepository implementation & reactive data streams
├── ui
│   ├── admin              # Administrator dashboard & hospital management
│   ├── auth               # Login, Role Auto-Resolution & Session State
│   ├── beds               # Bed allocation & Ward floor visualizer
│   ├── billing            # Invoicing, charge aggregation & payments
│   ├── common             # Shared components (LogoutDialog, MetricsCard, Badges)
│   ├── doctor             # Doctor OPD Queue, Vitals & Consultation desk
│   ├── lab                # Diagnostic test worklist & result entry
│   ├── navigation         # RootNavigationContainer & screen routing
│   ├── patient            # Patient Desk, Registration, Token Generator & History
│   ├── pharmacy           # Drug dispensing & prescription verification
│   ├── profile            # Patient & Staff profile dialogs
│   ├── splash             # Animated branding splash screen
│   ├── staff              # StaffPortalScaffold & unified staff workspace
│   └── theme              # Color, Type, Shape & MediFlowTheme definition
└── MainActivity.kt        # Single activity container
```

---

## Getting Started & Build Instructions

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1), Iguana, Ladybug or newer
- **JDK**: Java 17 or higher
- **Android SDK**: API 34 (Android 14) platform and build tools installed
- **Device / Emulator**: Android 8.0 (API 26) or higher

### Steps to Run
1. **Clone the Repository**:
   ```bash
   git clone <YOUR_GITHUB_REPO_URL>
   ```
2. **Open in Android Studio**:
   - Launch Android Studio.
   - Select **Open** and browse to the cloned project root.
3. **Gradle Sync**:
   - Allow Android Studio to automatically sync Gradle dependencies and build files.
4. **Run on Device / Emulator**:
   - Select a target device running Android API 26 or higher.
   - Click the green **Run (Shift + F10)** button.

---

## Compliance & Code Quality

- **Declarative UI**: 100% Jetpack Compose with reactive Material 3 components.
- **Accessibility & Readability**: Guaranteed $\ge 12\text{sp}$ typography across all screens.
- **Memory Safety**: Clean cancellation of coroutines via `viewModelScope`.
- **Zero Forbidden Words**: Sanitized codebase with zero references to institutional prefixes.

---

## License

This project is distributed under the [MIT License](LICENSE) — free for educational, academic, and clinical workflow evaluation.
