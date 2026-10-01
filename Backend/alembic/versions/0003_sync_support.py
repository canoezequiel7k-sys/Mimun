"""sync support: edited_at, deleted_at y unicidad parcial por día

Revision ID: 0003
Revises: 0002
Create Date: 2026-09-30
"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "0003"
down_revision: str | None = "0002"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None

_TABLES = ("mood_entries", "journal_entries")


def upgrade() -> None:
    for table in _TABLES:
        op.add_column(
            table,
            sa.Column(
                "edited_at",
                sa.DateTime(timezone=True),
                server_default=sa.func.now(),
                nullable=False,
            ),
        )
        op.add_column(table, sa.Column("deleted_at", sa.DateTime(timezone=True), nullable=True))
        op.execute(f"UPDATE {table} SET edited_at = updated_at")
        op.create_index(f"ix_{table}_user_id_updated_at", table, ["user_id", "updated_at"])

    # La unicidad (user_id, date) pasa a valer solo para registros no borrados.
    op.drop_constraint("uq_mood_entries_user_id_date", "mood_entries", type_="unique")
    op.create_index(
        "uq_mood_entries_user_id_date",
        "mood_entries",
        ["user_id", "date"],
        unique=True,
        postgresql_where=sa.text("deleted_at IS NULL"),
    )


def downgrade() -> None:
    op.drop_index("uq_mood_entries_user_id_date", table_name="mood_entries")
    # Los borrados lógicos no existen en el esquema anterior: se eliminan definitivamente.
    op.execute("DELETE FROM journal_entries WHERE deleted_at IS NOT NULL")
    op.execute("DELETE FROM mood_entries WHERE deleted_at IS NOT NULL")
    op.create_unique_constraint("uq_mood_entries_user_id_date", "mood_entries", ["user_id", "date"])

    for table in _TABLES:
        op.drop_index(f"ix_{table}_user_id_updated_at", table_name=table)
        op.drop_column(table, "deleted_at")
        op.drop_column(table, "edited_at")
