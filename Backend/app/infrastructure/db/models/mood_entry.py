import datetime as dt
import uuid

from sqlalchemy import CheckConstraint, Date, DateTime, ForeignKey, Text, UniqueConstraint, func
from sqlalchemy.orm import Mapped, mapped_column

from app.domain.value_objects import MoodType
from app.infrastructure.db.base import Base

_MOOD_VALUES = ", ".join(f"'{mood.value}'" for mood in MoodType)


class MoodEntryModel(Base):
    __tablename__ = "mood_entries"
    __table_args__ = (
        UniqueConstraint("user_id", "date", name="uq_mood_entries_user_id_date"),
        CheckConstraint(f"mood IN ({_MOOD_VALUES})", name="mood"),
        CheckConstraint("char_length(note) <= 500", name="note_length"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True)
    user_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"))
    date: Mapped[dt.date] = mapped_column(Date)
    mood: Mapped[str] = mapped_column(Text)
    note: Mapped[str | None] = mapped_column(Text)
    created_at: Mapped[dt.datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )
    updated_at: Mapped[dt.datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )
