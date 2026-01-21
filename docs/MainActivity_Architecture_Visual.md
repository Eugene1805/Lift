# MainActivity Architecture - Visual Overview

## Before Refactoring
```
┌─────────────────────────────────────────────────────────────────┐
│                      MainActivity.kt (343 lines)                │
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐   │
│  │ MainActivity class                                      │   │
│  │  - onCreate()                                          │   │
│  │  - WorkManager setup (inline)                          │   │
│  │  - Theme logic (inline)                                │   │
│  │  - Localization logic (inline)                         │   │
│  └────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐   │
│  │ MainAppShell composable                                │   │
│  │  - Navigation controller setup                         │   │
│  │  - Bottom bar logic (inline)                           │   │
│  │  - Navigation graph (100+ lines inline)                │   │
│  └────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐   │
│  │ Data classes & Helper classes                          │   │
│  │  - BottomNavItem                                       │   │
│  │  - HiltSafeLocalizedContext                            │   │
│  └────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

❌ Problems:
- Everything in one file
- Mixed concerns
- Hard to test
- Poor maintainability
```

## After Refactoring
```
┌──────────────────────────────────────────────────────────────────────┐
│                   Application Architecture                           │
└──────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────────┐
│                    MainActivity.kt (53 lines)                        │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │ Responsibilities:                                           │    │
│  │ • Enable edge-to-edge display                              │    │
│  │ • Initialize WorkManager → WorkInitializer.kt              │    │
│  │ • Set up Compose UI → LiftApp.kt                           │    │
│  │ • Hilt injection entry point                               │    │
│  └────────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────────┐
│                          ui/LiftApp.kt                               │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │ Responsibilities:                                           │    │
│  │ • Observe settings (theme, language)                       │    │
│  │ • Create localized context → HiltSafeLocalizedContext.kt   │    │
│  │ • Apply theme (light/dark/system)                          │    │
│  │ • Render main app shell → MainAppShell.kt                  │    │
│  └────────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────────┐
│                       ui/MainAppShell.kt                             │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │ Responsibilities:                                           │    │
│  │ • Set up navigation controller                             │    │
│  │ • Show/hide bottom bar based on route                      │    │
│  │ • Manage scaffold                                          │    │
│  └────────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────────┘
           │                                    │
           ▼                                    ▼
┌─────────────────────────┐      ┌────────────────────────────────────┐
│ ui/components/          │      │   ui/navigation/                   │
│ LiftBottomNavigation    │      │   LiftNavGraph.kt                  │
│ Bar.kt                  │      │  ┌──────────────────────────────┐  │
│  ┌───────────────────┐  │      │  │ • Profile screen             │  │
│  │ Responsibilities: │  │      │  │ • History screen             │  │
│  │ • Render items    │  │      │  │ • Workout screen             │  │
│  │ • Handle clicks   │  │      │  │ • Exercise screens           │  │
│  │ • Show selection  │  │      │  │ • Settings screen            │  │
│  └───────────────────┘  │      │  │ • Template screens           │  │
└─────────────────────────┘      │  │ • Active workout screen      │  │
           │                     │  └──────────────────────────────┘  │
           ▼                     └────────────────────────────────────┘
┌─────────────────────────┐
│ ui/navigation/          │
│ BottomNavConfig.kt      │
│ BottomNavItem.kt        │
│  ┌───────────────────┐  │
│  │ • Define items    │  │
│  │ • Configuration   │  │
│  └───────────────────┘  │
└─────────────────────────┘

┌──────────────────────────────────────────────────────────────────────┐
│                        Supporting Utilities                          │
├──────────────────────────────────────────────────────────────────────┤
│  common/localization/                 common/work/                   │
│  HiltSafeLocalizedContext.kt         WorkInitializer.kt              │
│  ┌────────────────────────┐         ┌──────────────────────────┐    │
│  │ • Localize resources   │         │ • Enqueue DB seeding     │    │
│  │ • Maintain Hilt compat │         │ • WorkManager setup      │    │
│  │ • Extension function   │         │ • Logging                │    │
│  └────────────────────────┘         └──────────────────────────┘    │
└──────────────────────────────────────────────────────────────────────┘

✅ Benefits:
- Clear separation of concerns
- Each component is testable
- Easy to understand and modify
- Reusable components
- Better maintainability
```

## Component Responsibilities Matrix

| Component | UI | Navigation | Theme | Locale | Work | Test |
|-----------|----|-----------:|------:|-------:|-----:|-----:|
| MainActivity | ❌ | ❌ | ❌ | ❌ | ✅ | ⚠️ |
| LiftApp | ✅ | ❌ | ✅ | ✅ | ❌ | ✅ |
| MainAppShell | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| LiftNavGraph | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ |
| LiftBottomNavigationBar | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| BottomNavConfig | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ |
| HiltSafeLocalizedContext | ❌ | ❌ | ❌ | ✅ | ❌ | ✅ |
| WorkInitializer | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |

Legend: ✅ Primary responsibility | ⚠️ Simple enough not to need tests | ❌ Not responsible

## Data Flow

```
User Settings Flow:
GetSettingsUseCase
      ↓
   LiftApp (observes)
      ↓
   ├── Theme applied
   └── Localized context created
           ↓
       MainAppShell
           ↓
       All screens

Navigation Flow:
User taps bottom nav item
      ↓
LiftBottomNavigationBar (handles click)
      ↓
NavController.navigate()
      ↓
LiftNavGraph (matches route)
      ↓
Destination screen rendered

Initialization Flow:
MainActivity.onCreate()
      ↓
WorkInitializer.enqueueDatabaseSeeding()
      ↓
setContent { LiftApp() }
```

## File Size Comparison

| File | Lines | Responsibility |
|------|------:|----------------|
| **Before** |
| MainActivity.kt | 343 | Everything |
| **After** |
| MainActivity.kt | 53 | Entry point |
| LiftApp.kt | 64 | Theme/Locale |
| MainAppShell.kt | 51 | Scaffold |
| LiftNavGraph.kt | 188 | Navigation |
| LiftBottomNavigationBar.kt | 41 | Bottom bar UI |
| BottomNavConfig.kt | 22 | Nav config |
| BottomNavItem.kt | 15 | Data model |
| HiltSafeLocalizedContext.kt | 43 | Localization |
| WorkInitializer.kt | 37 | Work init |
| **Total** | **514** | **Well organized** |

## Complexity Metrics

| Metric | Before | After | Improvement |
|--------|-------:|------:|------------:|
| Main file size | 343 lines | 53 lines | 84% ↓ |
| Files | 1 | 9 | Better org |
| Concerns per file | ~8 | 1-2 | 75% ↓ |
| Testable units | 1 | 8 | 800% ↑ |
| Cyclomatic complexity | High | Low | Much better |
| Code reusability | Low | High | Much better |

✅ **Result**: Much cleaner, more maintainable, and testable architecture!
