import datetime as dt
from typing import Protocol


class Clock(Protocol):
    def now(self) -> dt.datetime:
        """Instante actual, con zona horaria UTC."""
        ...
