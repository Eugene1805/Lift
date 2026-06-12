# Exercise Image Backlog

Current state:
- Local seed remains the source of truth for the visible exercise catalog.
- `39` seeded exercises now resolve to local `webp` assets.
- Drawable naming is aligned with `ExerciseImageMapper`: `0` missing targets and `0` unused local `webp` files.
- Existing installs need the versioned seed worker rerun to backfill the new image paths.

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
- Goblet Squat (Dumbbell)
- Cable Row (Machine)
- Incline Dumbbell Press
- T-Bar Row
- Hip Abduction (Machine)
- Glute Kickback (Cable)
- Wrist Curl (Barbell)
- Chest Press (Machine)
- Hack Squat (Machine)
- Smith Machine Bulgarian Split Squat

## Batch 1

Highest-value gaps still missing a local asset:

- Dumbbell Press
- Push-up
- Cable Fly
- Tricep Extension
- Lunges
- Leg Curl
- Cable Tricep Pushdown
- Skull Crusher
- Machine Row
- Dumbbell Fly
- Cable Crossover
- Smith Machine Squat
- Seated Leg Curl
- Weighted Pull-up
- Arnold Press

## Batch 2

Strong next layer after the core catalog gaps:

- Plank
- Hanging Leg Raise
- Russian Twist
- Reverse Pec Deck
- EZ Bar Curl
- Cable Curl
- Reverse Curl
- Front Raise
- Upright Row
- Reverse Wrist Curl
- Glute Bridge
- Back Extension
- Front Lat Pulldown
- Smith Machine Bench Press
- Smith Machine Shoulder Press

## Batch 3

Lower-priority variants and specialty movements:

- Box Jump
- Farmer's Walk
- Power Clean
- Burpee
- Mountain Climber
- Jump Squat
- Pistol Squat
- Dragon Flag
- Muscle Up
- Front Lever
- Machine Fly
- Machine Pullover
- Cable Pullover (Rope)
- Cable Pullover (Bar)
- Machine Overhead Triceps Extension

## Naming Rules

- Put files in `app/src/main/res/drawable/`
- Use lowercase ASCII `snake_case`
- Prefer canonical movement names over equipment-first names when they are shown in the UI
- Keep legacy typos only when already persisted in shipped DB data, for example `weigthed_dips`
- Prefer `webp`

## Visual Rules

- Keep transparent or neutral consistent backgrounds
- Keep framing and camera angle consistent across the set
- Show the key movement pattern clearly
- Prefer 1200-1600 px on the long side
- Keep file sizes lean enough for APK use

## Integration Steps

1. Add the `webp` file in `app/src/main/res/drawable/`
2. Match the drawable basename in `ExerciseImageMapper`
3. Wire the corresponding `seed_*` entry in `ExerciseSeeder` with `imageFor(context, R.string.seed_...)`
4. Bump `SEED_DB_WORK_NAME` if existing installs need backfill
5. Run unit/build checks and visually confirm list/detail rendering
