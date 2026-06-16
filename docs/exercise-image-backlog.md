# Exercise Image Backlog

Current state:
- Local seed remains the source of truth for the visible exercise catalog.
- `99` seeded exercises now resolve to local drawable assets.
- Seeded exercises now persist a stable `seedKey`, so names/instructions can localize without mutating the catalog rows.
- Seeded exercises are treated as read-only in the app; custom variants should come from user-created exercises.
- Existing installs need the versioned seed worker rerun to backfill the new image paths.
- `0` local `webp` assets are orphaned in `drawable/`.
- `0` tracked drawable aliases remain seedless; every local `webp` now maps to at least one seeded exercise.

## Already Covered

- Bench Press (Barbell)
- Back Squat
- Deadlift (Barbell)
- Overhead Press (Barbell)
- Barbell Row
- Dumbbell Shoulder Press
- Lat Pulldown (Cable)
- Leg Press (Machine)
- Pull-ups
- Dumbbell Row
- Bicep Curl (Dumbbell)
- Lateral Raise (Dumbbell)
- Romanian Deadlift (Barbell)
- Front Squat (Barbell)
- Leg Extension (Machine)
- Face Pulls (Cable)
- Dips
- Hip Thrust (Barbell)
- Incline Bench Press (Barbell)
- Standing Calf Raise (Machine)
- Hammer Curl (Dumbbell)
- Bulgarian Split Squat (Dumbbell)
- Chest Supported Row (Machine)
- Cable Lateral Raise
- Pec Deck (Machine)
- Seated Cable Row (Machine)
- Machine Shoulder Press
- Preacher Curl (Machine)
- Weighted Dips
- Weighted Pull-ups
- Goblet Squat (Dumbbell)
- Cable Row (Machine)
- Incline Dumbbell Press
- T-Bar Row
- Barbell Shrug
- Hip Abduction (Machine)
- Hip Adduction (Machine)
- Cable Fly
- Plank
- Leg Curl (Machine)
- Skull Crushers (Barbell)
- Cable Crunch
- Russian Twists
- Reverse Pec Deck (Machine)
- Farmer's Walk (Dumbbell)
- Machine Row (Seated)
- Cable Curl
- Back Extension (Hyperextension)
- Single Leg Deadlift (Dumbbell)
- Glute Bridge
- Glute Kickback (Cable)
- Bicycle Crunch
- Wrist Curl (Barbell)
- Chest Press (Machine)
- Hack Squat (Machine)
- Smith Machine Squat
- Seated Leg Curl (Machine)
- Smith Machine Bench Press
- Smith Machine Incline Bench Press
- Smith Machine Hip Thrust
- Smith Machine Romanian Deadlift
- Dragon Flag
- Smith Machine Bulgarian Split Squat
- Machine Fly (Pec Fly)
- Machine Pullover
- Cable Pullover (Bar)
- Cable Triceps Pushdown
- Arnold Press (Dumbbell)
- Box Jumps
- Chin-ups
- Ab Wheel Rollout
- Dumbbell Fly
- Front Raise (Dumbbell)
- Sissy Squat
- Upright Row (Barbell)
- Power Clean (Barbell)
- Muscle Up
- Front Lever
- Dumbbell Chest Press
- Push-ups
- Hanging Leg Raises
- Cable Crossover
- EZ Bar Curl
- Dumbbell Overhead Press (Standing)
- Weighted Crunch
- Rope Triceps Extension (Cable)
- Rope Face Pull (Cable)
- Sumo Deadlift (Barbell)
- Decline Dumbbell Press
- Reverse Wrist Curl (Barbell)
- Smith Machine Shoulder Press
- Cable Overhead Triceps Extension (Bilateral)
- Cable Overhead Triceps Extension (Unilateral)
- Cable Overhead Triceps Extension with Cuff (Unilateral)
- Machine Lateral Raise
- Reverse Curl (EZ Bar)
- Step Up (Dumbbell)
- Cable Oblique Twist
- Single Arm Triceps Extension

## Batch 1

Highest-value gaps still missing a local asset:

- Tricep Extension
- Lunges
- Concentration Curl

## Batch 2

Strong next layer after the core catalog gaps:

- Reverse Curl
- Front Lat Pulldown
- Single Leg Curl
- Good Morning
- Rack Pull

## Batch 3

Lower-priority variants and specialty movements:

- Burpee
- Mountain Climber
- Jump Squat
- Pistol Squat
- Cable Pullover (Rope)
- Machine Overhead Triceps Extension
- Neck Extension
- Neck Curl

## Naming Rules

- Put files in `app/src/main/res/drawable/`
- Use lowercase ASCII `snake_case`
- Prefer canonical movement names over equipment-first names when they are shown in the UI
- Keep legacy typos only when already persisted in shipped DB data, for example `weigthed_dips`
- Prefer `webp`, but `png` is acceptable when the imported source is already clean and optimized

## Visual Rules

- Keep transparent or neutral consistent backgrounds
- Keep framing and camera angle consistent across the set
- Show the key movement pattern clearly
- Prefer 1200-1600 px on the long side
- Keep file sizes lean enough for APK use

## Integration Steps

1. Add the drawable asset in `app/src/main/res/drawable/`
2. Match the drawable basename in `ExerciseImageMapper`
3. Wire the corresponding `seed_*` entry in `ExerciseSeeder` with `imageFor(context, R.string.seed_...)`
4. Bump `SEED_DB_WORK_NAME` if existing installs need backfill
5. Run unit/build checks and visually confirm list/detail rendering
