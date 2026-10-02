import math
import threading
import time
from collections import defaultdict, deque
from collections.abc import Callable

from app.domain.errors import RateLimitExceededError

_PURGE_THRESHOLD = 10_000


class InMemoryRateLimiter:
    """Ventana deslizante por clave (ej.: IP del cliente).

    Vive en la memoria de UN proceso: se reinicia con el servidor y no se comparte entre
    workers. Alcanza para una instancia única; si se escala, pasar a Redis (ver Fase B9).
    `max_attempts <= 0` lo desactiva.
    """

    def __init__(
        self,
        *,
        max_attempts: int,
        window_seconds: int,
        clock: Callable[[], float] = time.monotonic,
    ) -> None:
        self._max_attempts = max_attempts
        self._window = window_seconds
        self._clock = clock
        self._hits: dict[str, deque[float]] = defaultdict(deque)
        self._lock = threading.Lock()

    def check(self, key: str) -> None:
        """Registra un intento. Lanza RateLimitExceededError si la clave se pasó del límite."""
        if self._max_attempts <= 0:
            return
        now = self._clock()
        with self._lock:
            hits = self._hits[key]
            while hits and now - hits[0] >= self._window:
                hits.popleft()
            if len(hits) >= self._max_attempts:
                retry_after = max(1, math.ceil(self._window - (now - hits[0])))
                raise RateLimitExceededError(retry_after)
            hits.append(now)
            if len(self._hits) > _PURGE_THRESHOLD:
                self._purge(now)

    def _purge(self, now: float) -> None:
        stale = [k for k, h in self._hits.items() if not h or now - h[-1] >= self._window]
        for key in stale:
            del self._hits[key]
