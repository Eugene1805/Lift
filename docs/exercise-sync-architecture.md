# Exercise Sync Architecture

## Goals

- Keep local `Exercise.id` stable for templates, sessions, and history.
- Preserve the offline bootstrap catalog when the device has no network.
- Layer remote sync on top of bootstrap without partial catalog writes.

## Planned Flow

1. App startup enqueues exercise preparation work.
2. Local bootstrap seeds the base catalog if the database is empty.
3. Remote sync runs only when connectivity is available.
4. Remote payloads map into local exercises using `remoteId` as the stable external key.
5. A full sync commit happens inside a single Room transaction.

## Contracts

- `ExerciseBootstrapDataSource`: owns offline-first bootstrap of the base catalog.
- `ExerciseSyncDataSource`: owns remote exercise fetches for synchronization.
- `ExerciseRemoteDataSource`: compatibility alias over the sync contract for current Wger work.

## Data Rules

- `remoteId` is optional and never replaces the local primary key.
- `source` records whether an exercise came from local bootstrap or Wger.
- `lastSyncedAt` and `syncVersion` are sync bookkeeping metadata.
- Existing exercises referenced by templates or workout history must remain valid across syncs.
