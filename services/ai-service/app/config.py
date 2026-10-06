from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Settings come from environment variables (see docker-compose.yml / .env)."""

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    app_env: str = "local"
    llm_api_key: str = ""
    # Claude Haiku 4.5: cheap and fast, good at following the explanation rules.
    llm_model: str = "claude-haiku-4-5-20251001"
    # Kept below core-api's read timeout, so core-api gets an answer (or an error) before it gives up.
    llm_timeout_seconds: float = 4.0

    @property
    def llm_configured(self) -> bool:
        return bool(self.llm_api_key)


@lru_cache
def get_settings() -> Settings:
    return Settings()
