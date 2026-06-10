# Exercise Image Backlog

Current state:
- Local seed is the source of truth for the exercise catalog.
- `17` seeded exercises already resolve to local images.
- `3` extra drawable assets already exist in the repo and can be enabled immediately.

## Already Covered

- Bench Press (Barbell)
- Back Squat
- Deadlift (Barbell)
- Overhead Press (Barbell)
- Barbell Row
- Dumbbell Shoulder Press
- Pull-ups
- Bicep Curl (Dumbbell)
- Leg Extension (Machine)
- Hip Thrust (Barbell)
- Standing Calf Raise (Machine)
- Bulgarian Split Squat (Dumbbell)
- Cable Lateral Raise
- Pec Deck (Machine)
- Preacher Curl (Machine)
- Weighted Dips
- Hip Abduction (Machine)

## Quick Wins

These already have `webp` assets in `res/drawable` and are now wired to the seed:

- Incline Dumbbell Press
- Wrist Curl (Barbell)
- Smith Machine Bulgarian Split Squat

## Batch 1

Highest-value gym catalog additions after the current covered set:

- Dumbbell Press
- Incline Bench Press
- Lat Pulldown
- Leg Press
- Dumbbell Row
- Lateral Raise (Dumbbell)
- Romanian Deadlift
- Front Squat
- Dips
- Face Pull
- Seated Cable Row
- Chest Supported Row
- Cable Tricep Pushdown
- Hammer Curl
- Goblet Squat
- Chest Press Machine

## Batch 2

- Cable Row
- Machine Row
- Arnold Press
- EZ Bar Curl
- Cable Curl
- Weighted Pull-up
- Hack Squat
- Smith Machine Squat
- Seated Leg Curl
- Dumbbell Fly
- Cable Crossover
- Upright Row
- Standing Calf Raise
- Seated Calf Raise
- Lunges

## Batch 3

Lower-priority or more specialized variants:

- Reverse Pec Deck
- Rope Face Pull
- Straight Arm Pulldown
- T-Bar Row
- Rack Pull
- Sumo Deadlift
- Landmine Press
- Power Clean
- Farmer's Walk
- Box Jump
- Glute Bridge
- Glute Kickback
- Pistol Squat
- Dragon Flag
- Muscle Up

## Asset Rules

- Put files in `app/src/main/res/drawable/`
- Use lowercase ASCII `snake_case` names
- Prefer `webp`
- Keep transparent or neutral consistent backgrounds
- Keep framing and camera angle consistent across the set
- Show the key movement pattern clearly
- Prefer 1200-1600 px on the long side
- Keep file sizes lean enough for APK use

## Integration Steps

1. Add the asset file, for example `lat_pulldown.webp`
2. Map the English seed name in `ExerciseImageMapper`
3. Use `imageFor(context.getString(...))` in `ExerciseSeeder` for that exercise
4. Run unit/build checks and visually confirm list/detail rendering

Because the exercise list now resolves drawables dynamically by resource name, new local assets no longer require a manual `when` entry in `ExercisesScreen`.
