import pytest
from sqlalchemy import Engine, text


@pytest.mark.integration
def test_can_connect_to_test_database(test_engine: Engine) -> None:
    with test_engine.connect() as connection:
        assert connection.execute(text("SELECT 1")).scalar() == 1
