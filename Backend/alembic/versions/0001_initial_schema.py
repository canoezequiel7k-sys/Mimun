"""initial schema

Revision ID: 0001
Revises:
Create Date: 2026-09-30
"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "0001"
down_revision: str | None = None
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def _timestamps() -> list[sa.Column]:
    return [
        sa.Column(
            "created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False
        ),
        sa.Column(
            "updated_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False
        ),
    ]


def upgrade() -> None:
    op.create_table(
        "users",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("email", sa.Text(), nullable=False),
        sa.Column("password_hash", sa.Text(), nullable=False),
        *_timestamps(),
        sa.PrimaryKeyConstraint("id", name="pk_users"),
    )
    op.create_index("uq_users_email_lower", "users", [sa.text("lower(email)")], unique=True)

    op.create_table(
        "mood_entries",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("date", sa.Date(), nullable=False),
        sa.Column("mood", sa.Text(), nullable=False),
        sa.Column("note", sa.Text(), nullable=True),
        *_timestamps(),
        sa.PrimaryKeyConstraint("id", name="pk_mood_entries"),
        sa.ForeignKeyConstraint(
            ["user_id"], ["users.id"], name="fk_mood_entries_user_id_users", ondelete="CASCADE"
        ),
        sa.UniqueConstraint("user_id", "date", name="uq_mood_entries_user_id_date"),
        sa.CheckConstraint(
            "mood IN ('RAD', 'GOOD', 'MEH', 'BAD', 'AWFUL')", name="ck_mood_entries_mood"
        ),
        sa.CheckConstraint("char_length(note) <= 500", name="ck_mood_entries_note_length"),
    )

    op.create_table(
        "journal_entries",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("mood_entry_id", sa.Uuid(), nullable=True),
        sa.Column("title", sa.Text(), nullable=True),
        sa.Column("content", sa.Text(), nullable=False),
        *_timestamps(),
        sa.PrimaryKeyConstraint("id", name="pk_journal_entries"),
        sa.ForeignKeyConstraint(
            ["user_id"], ["users.id"], name="fk_journal_entries_user_id_users", ondelete="CASCADE"
        ),
        sa.ForeignKeyConstraint(
            ["mood_entry_id"],
            ["mood_entries.id"],
            name="fk_journal_entries_mood_entry_id_mood_entries",
            ondelete="SET NULL",
        ),
        sa.CheckConstraint("char_length(title) <= 120", name="ck_journal_entries_title_length"),
        sa.CheckConstraint(
            "char_length(content) BETWEEN 1 AND 10000", name="ck_journal_entries_content_length"
        ),
    )
    op.create_index(
        "ix_journal_entries_user_id_created_at",
        "journal_entries",
        ["user_id", sa.text("created_at DESC")],
    )
    op.create_index("ix_journal_entries_mood_entry_id", "journal_entries", ["mood_entry_id"])


def downgrade() -> None:
    op.drop_table("journal_entries")
    op.drop_table("mood_entries")
    op.drop_index("uq_users_email_lower", table_name="users")
    op.drop_table("users")
