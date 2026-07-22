# REQUIREMENTS — Triple Match Fruit Puzzle

> **Product:** Cross-platform offline Triple Match puzzle game with fruit theme
> **Platforms:** Android, iOS, Desktop (Windows, macOS, Linux)
> **Architecture:** Kotlin Multiplatform + Compose Multiplatform, 100% shared UI in `commonMain`
> **Monetization:** None (no ads, no IAP, fully offline)

---

## User Stories

### US-01: Start the Game
**As a** player,
**I want** to see a Start Page with a background image, "Start" button, and conditional "Exit" button,
**So that** I can launch the game or close the app.

**Acceptance Criteria:**
- [ ] Static fruit-themed background loaded via `painterResource` from `composeResources`
- [ ] "Start" button navigates to the Game Screen
- [ ] "Exit" button visible **only on Desktop** — calls `System.exit(0)` via `expect/actual`
- [ ] On iOS/Android, "Exit" button is hidden; back-stack handles exit

---

### US-02: View Game Board
**As a** player,
**I want** to see a game board with overlapping fruit tiles and a 7-slot rack,
**So that** I can interact with the puzzle.

**Acceptance Criteria:**
- [ ] Header shows current level: "Level N"
- [ ] 7 empty visual slots displayed in a horizontal row (Slot Rack)
- [ ] Board area shows procedurally generated, overlapping fruit tiles
- [ ] Fruits are clearly distinguishable (distinct colors/shapes/emojis)

---

### US-03: Select & Move Fruit to Rack
**As a** player,
**I want** to tap a fruit tile on the board and see it fly to the rack,
**So that** I can play the matching game.

**Acceptance Criteria:**
- [ ] Tapping a fruit removes it from the board
- [ ] A "flying" clone animates from board position to rack position using spring physics
- [ ] Animation not clipped by parent containers (Flying Overlay at root level)
- [ ] Fruit lands in the correct slot position

---

### US-04: Grouping & Auto-Shift in Rack
**As a** player,
**I want** matching fruits in the rack to automatically group together,
**So that** I can form triples more easily.

**Acceptance Criteria:**
- [ ] When a fruit enters the rack, it groups next to identical fruits already present
- [ ] Non-matching fruits shift right to make room
- [ ] Grouping animation is smooth and visible

---

### US-05: Triple Match Destruction
**As a** player,
**I want** 3 identical grouped fruits to be destroyed automatically,
**So that** I can clear space and progress.

**Acceptance Criteria:**
- [ ] When 3 identical fruits group together, they vanish with `scaleOut()` + `fadeOut()` animation
- [ ] Remaining fruits shift left to fill gaps
- [ ] Rack slot count updates correctly

---

### US-06: Win Condition
**As a** player,
**I want** to see a "Congratulations" popup when I clear the board and rack,
**So that** I know I completed the level.

**Acceptance Criteria:**
- [ ] Popup appears when board is empty AND rack is empty
- [ ] Popup has a "Next Level" button advancing to `Level N + 1`
- [ ] `currentLevel` is persisted to local storage

---

### US-07: Loss & Life System
**As a** player,
**I want** to have 3 lives and see a "Try Again" popup when I fill the rack without matching,
**So that** I understand the penalty system.

**Acceptance Criteria:**
- [ ] When 7 slots are full with no group of 3, game pauses
- [ ] "Try Again" popup shows with retry option
- [ ] Retrying costs 1 life and resets the current level
- [ ] When lives reach 0: lives refill to 3, player sent back to `max(1, Level N - 1)`
- [ ] `livesCounter` is persisted to local storage

---

### US-08: Solvable Level Generation
**As a** player,
**I want** every level to be 100% solvable,
**So that** I never encounter an impossible puzzle.

**Acceptance Criteria:**
- [ ] "Reverse Play" algorithm: start from empty board, add groups of 3 incrementally
- [ ] Level difficulty scales with level number (more fruit types, more tiles)
- [ ] Algorithm runs fast (no heavy recursive testing)

---

### US-09: Persistence
**As a** player,
**I want** my progress (level, lives) saved locally,
**So that** I can continue where I left off.

**Acceptance Criteria:**
- [ ] `currentLevel` and `livesCounter` persisted via `multiplatform-settings`
- [ ] State restored on app launch
- [ ] No internet connection required

---

### US-10: Desktop Deployment
**As a** desktop user,
**I want** standalone executables for Windows, macOS, and Linux,
**So that** I can install the game without a JRE.

**Acceptance Criteria:**
- [ ] `nativeDistributions` configured with `jlink` + `jpackage`
- [ ] Output: `.dmg` (macOS), `.msi` (Windows), `.deb` (Linux)
- [ ] Unused Java modules stripped for minimal binary size

---

## Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|-------------|
| NFR-01 | Offline | App works 100% offline, zero network calls |
| NFR-02 | Performance | Board generation < 100ms on mid-range devices |
| NFR-03 | Animation | 60fps fly + destruction animations |
| NFR-04 | Size | Android APK < 30MB, Desktop < 80MB |
| NFR-05 | Platforms | Android 7+ (API 24), iOS 15+, JVM Desktop (JDK 17+) |
