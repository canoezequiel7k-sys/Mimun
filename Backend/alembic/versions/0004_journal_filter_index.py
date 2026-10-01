"""add journal filter index

Revision ID: 0004
Revises: 0003
Create Date: 2026-10-01
"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "0004"
down_revision: str | None = "0003"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_index(
        "ix_journal_entries_user_mood_created_active",
        "journal_entries",
        ["user_id", "mood_entry_id", sa.text("created_at DESC"), "id"],
        postgresql_where=sa.text("deleted_at IS NULL"),
    )


def downgrade() -> None:
    op.drop_index("ix_journal_entries_user_mood_created_active", table_name="journal_entries")