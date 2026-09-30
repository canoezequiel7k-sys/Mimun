from typing import Annotated
from uuid import UUID

from fastapi import APIRouter, Depends, Query

from app.application.use_cases.journal_entries import (
    CreateJournalEntryCommand,
    JournalEntryUseCases,
    ListJournalEntriesQuery,
    UpdateJournalEntryCommand,
)
from app.presentation.api.deps import get_current_user_id, get_journal_entry_use_cases
from app.presentation.api.schemas.journal_entry import (
    JournalEntryCreateRequest,
    JournalEntryListResponse,
    JournalEntryResponse,
    JournalEntryUpdateRequest,
)

router = APIRouter(prefix="/journal-entries", tags=["journal-entries"])

UserId = Annotated[UUID, Depends(get_current_user_id)]
UseCases = Annotated[JournalEntryUseCases, Depends(get_journal_entry_use_cases)]


@router.post("", response_model=JournalEntryResponse, status_code=201)
def create_journal_entry(
    body: JournalEntryCreateRequest, user_id: UserId, use_cases: UseCases
) -> JournalEntryResponse:
    entry = use_cases.create.execute(
        CreateJournalEntryCommand(
            user_id=user_id,
            mood_entry_id=body.mood_entry_id,
            title=body.title,
            content=body.content,
            id=body.id,
        )
    )
    return JournalEntryResponse.from_entity(entry)


@router.get("", response_model=JournalEntryListResponse)
def list_journal_entries(
    user_id: UserId,
    use_cases: UseCases,
    mood_entry_id: UUID | None = None,
    limit: Annotated[int, Query(ge=1, le=100)] = 20,
    offset: Annotated[int, Query(ge=0)] = 0,
) -> JournalEntryListResponse:
    page = use_cases.list_entries.execute(
        ListJournalEntriesQuery(
            user_id=user_id, mood_entry_id=mood_entry_id, limit=limit, offset=offset
        )
    )
    return JournalEntryListResponse(
        items=[JournalEntryResponse.from_entity(e) for e in page.items],
        total=page.total,
        limit=page.limit,
        offset=page.offset,
    )


@router.get("/{entry_id}", response_model=JournalEntryResponse)
def get_journal_entry(entry_id: UUID, user_id: UserId, use_cases: UseCases) -> JournalEntryResponse:
    return JournalEntryResponse.from_entity(use_cases.get.execute(user_id, entry_id))


@router.put("/{entry_id}", response_model=JournalEntryResponse)
def update_journal_entry(
    entry_id: UUID, body: JournalEntryUpdateRequest, user_id: UserId, use_cases: UseCases
) -> JournalEntryResponse:
    entry = use_cases.update.execute(
        UpdateJournalEntryCommand(
            user_id=user_id,
            entry_id=entry_id,
            mood_entry_id=body.mood_entry_id,
            title=body.title,
            content=body.content,
        )
    )
    return JournalEntryResponse.from_entity(entry)


@router.delete("/{entry_id}", status_code=204)
def delete_journal_entry(entry_id: UUID, user_id: UserId, use_cases: UseCases) -> None:
    use_cases.delete.execute(user_id, entry_id)
