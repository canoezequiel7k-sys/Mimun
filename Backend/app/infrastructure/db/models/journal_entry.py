import datetime as dt
import uuid

from sqlalchemy import CheckConstraint, DateTime, ForeignKey, Index, Text, func, text
from sqlalchemy.orm import Mapped, mapped_column

from app.infrastructure.db.base import Base


class JournalEntryModel(Base):
    __tablename__ = "journal_entries"
    __table_args__ = (
        CheckConstraint("char_length(title) <= 120", name="title_length"),
        CheckConstraint("char_length(content) BETWEEN 1 AND 10000", name="content_length"),
        Index("ix_journal_entries_user_id_created_at", "user_id", text("created_at DESC")),
        Index("ix_journal_entries_mood_entry_id", "mood_entry_id"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True)
    user_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"))
    mood_entry_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("mood_entries.id", ondelete="SET NULL")
    )
    title: Mapped[str | None] = mapped_column(Text)
    content: Mapped[str] = mapped_column(Text)
    created_at: Mapped[dt.datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )
    updated_at: Mapped[dt.datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )
