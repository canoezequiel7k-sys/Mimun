import datetime as dt
from typing import Annotated
from uuid import UUID

from fastapi import APIRouter, Depends, Query, Response

from app.application.use_cases.mood_entries import (
    CreateMoodEntryCommand,
    ListMoodEntriesQuery,
    MoodEntryUseCases,
    UpsertMoodEntryCommand,
)
from app.presentation.api.deps import get_current_user_id, get_mood_entry_use_cases
from app.presentation.api.schemas.mood_entry import (
    MoodEntryCreateRequest,
    MoodEntryListResponse,
    MoodEntryResponse,
    MoodEntryUpsertRequest,
)

router = APIRouter(prefix="/mood-entries", tags=["mood-entries"])

UserId = Annotated[UUID, Depends(get_current_user_id)]
UseCases = Annotated[MoodEntryUseCases, Depends(get_mood_entry_use_cases)]


@router.post("", response_model=MoodEntryResponse, status_code=201)
def create_mood_entry(
    body: MoodEntryCreateRequest, user_id: UserId, use_cases: UseCases
) -> MoodEntryResponse:
    entry = use_cases.create.execute(
        CreateMoodEntryCommand(
            user_id=user_id,
            date=body.date,
            mood=body.mood,
            note=body.note,
            id=body.id,
            edited_at=body.edited_at,
        )
    )
    return MoodEntryResponse.from_entity(entry)


@router.get("", response_model=MoodEntryListResponse)
def list_mood_entries(
    user_id: UserId,
    use_cases: UseCases,
    date_from: Annotated[dt.date | None, Query(alias="from")] = None,
    date_to: Annotated[dt.date | None, Query(alias="to")] = None,
    updated_since: dt.datetime | None = None,
    limit: Annotated[int, Query(ge=1, le=366)] = 100,
    offset: Annotated[int, Query(ge=0)] = 0,
) -> MoodEntryListResponse:
    page = use_cases.list_entries.execute(
        ListMoodEntriesQuery(
            user_id=user_id,
            date_from=date_from,
            date_to=date_to,
            limit=limit,
            offset=offset,
            updated_since=updated_since,
        )
    )
    return MoodEntryListResponse(
        items=[MoodEntryResponse.from_entity(e) for e in page.items],
        total=page.total,
        limit=page.limit,
        offset=page.offset,
    )


@router.get("/{entry_id}", response_model=MoodEntryResponse)
def get_mood_entry(entry_id: UUID, user_id: UserId, use_cases: UseCases) -> MoodEntryResponse:
    return MoodEntryResponse.from_entity(use_cases.get.execute(user_id, entry_id))


@router.put("/{entry_id}", response_model=MoodEntryResponse)
def upsert_mood_entry(
    entry_id: UUID,
    body: MoodEntryUpsertRequest,
    response: Response,
    user_id: UserId,
    use_cases: UseCases,
) -> MoodEntryResponse:
    result = use_cases.upsert.execute(
        UpsertMoodEntryCommand(
            user_id=user_id,
            entry_id=entry_id,
            mood=body.mood,
            note=body.note,
            date=body.date,
            edited_at=body.edited_at,
        )
    )
    response.status_code = 201 if result.created else 200
    return MoodEntryResponse.from_entity(result.entry)


@router.delete("/{entry_id}", status_code=204)
def delete_mood_entry(entry_id: UUID, user_id: UserId, use_cases: UseCases) -> None:
    use_cases.delete.execute(user_id, entry_id)
