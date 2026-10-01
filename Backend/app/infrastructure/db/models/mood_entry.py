import datetime as dt
import uuid

from sqlalchemy import CheckConstraint, Date, DateTime, ForeignKey, Index, Text, func, text
from sqlalchemy.orm import Mapped, mapped_column

from app.domain.value_objects import MoodType
from app.infrastructure.db.base import Base

_MOOD_VALUES = ", ".join(f"'{mood.value}'" for mood in MoodType)


class MoodEntryModel(Base):
    __tablename__ = "mood_entries"
    __table_args__ = (
        # Un registro activo por usuario y día: los borrados lógicamente no cuentan.
        Index(
            "uq_mood_entries_user_id_date",
            "user_id",
            "date",
            unique=True,
            postgresql_where=text("deleted_at IS NULL"),
        ),
        Index("ix_mood_entries_user_id_updated_at", "user_id", "updated_at"),
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
    edited_at: Mapped[dt.datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )
    deleted_at: Mapped[dt.datetime | None] = mapped_column(DateTime(timezone=True))
