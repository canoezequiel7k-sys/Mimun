from enum import StrEnum


class MoodType(StrEnum):
    """Estados emocionales. Idénticos a `MoodType` en Android y a `Docs/api/`.

    Escala de mejor a peor: RAD → GOOD → MEH → BAD → AWFUL.
    `NORMAL` no existe (decisión 0002).
    """

    RAD = "RAD"
    GOOD = "GOOD"
    MEH = "MEH"
    BAD = "BAD"
    AWFUL = "AWFUL"

    @property
    def score(self) -> int:
        """Puntaje para estadísticas: 5 (RAD) a 1 (AWFUL)."""
        return _MOOD_SCORES[self]


_MOOD_SCORES: dict[MoodType, int] = {
    MoodType.RAD: 5,
    MoodType.GOOD: 4,
    MoodType.MEH: 3,
    MoodType.BAD: 2,
    MoodType.AWFUL: 1,
}
