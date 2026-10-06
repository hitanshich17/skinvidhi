"""Plain-language "why this routine" from the rules engine's own output.

The LLM only explains decisions the rules already made; it never picks products or makes safety calls.
Its answer is checked before use, and core-api falls back to a template explanation otherwise.
"""

import json
import re

from pydantic import BaseModel, Field

from app.llm import Llm


class StepFacts(BaseModel):
    time: str  # AM or PM
    step: str  # cleanser, treatment, moisturizer, sunscreen
    product: str  # brand and name
    key_ingredients: list[str] = Field(default_factory=list, alias="keyIngredients")
    already_owned: bool = Field(default=False, alias="alreadyOwned")


class ExplanationFacts(BaseModel):
    """What the rules decided. Contains no pregnancy answer and no city (privacy, docs/feedback.md)."""

    skin_type: str = Field(alias="skinType")
    concerns: list[str]
    reactivity: str
    avoid: list[str] = Field(default_factory=list)
    actives_experience: str = Field(alias="activesExperience")
    climate: list[str] = Field(default_factory=list)  # e.g. "humid air"
    treatment_active: str | None = Field(default=None, alias="treatmentActive")
    treatment_changed_for_budget: bool = Field(default=False, alias="treatmentChangedForBudget")
    steps: list[StepFacts]
    avoided_for_you: list[str] = Field(default_factory=list, alias="avoidedForYou")
    notes: list[str] = Field(default_factory=list)
    within_budget: bool = Field(default=True, alias="withinBudget")


SYSTEM_PROMPT = """You explain a skincare routine that a rules engine has already chosen. Write for a general \
US audience in plain, friendly American English.

Rules:
- Use only the facts in the JSON you are given. Do not add products, ingredients, benefits or advice that are not \
in the facts.
- Explain why the night treatment fits the main concern, and how the answers and climate shaped the other steps.
- Name products exactly as given. Never name any other product or brand.
- This is not medical advice: never diagnose, never promise results, never use words like "cure" or "guarantee".
- If the facts include a treatment that can make skin sun-sensitive (retinoid, AHA, BHA) or vitamin C, remind the \
reader to wear the sunscreen every morning.
- 3 to 5 sentences, one paragraph, no lists, no headings, no links."""

MIN_LENGTH = 80
MAX_LENGTH = 1200
MAX_TOKENS = 400
# Words an explanation must never use: medical claims and promises.
BANNED = re.compile(r"\b(cure[sd]?|diagnos\w*|prescri\w*|guarantee\w*|miracle|clinically proven)\b", re.IGNORECASE)
LINK = re.compile(r"https?://|www\.", re.IGNORECASE)


class InvalidExplanation(Exception):
    pass


def explain(facts: ExplanationFacts, llm: Llm) -> str:
    user = "Facts:\n" + json.dumps(facts.model_dump(by_alias=True), indent=2)
    text = llm.complete(SYSTEM_PROMPT, user, MAX_TOKENS).strip()
    check(text)
    return text


def check(text: str) -> None:
    """Raises InvalidExplanation if the text breaks a rule we can check mechanically."""
    if not MIN_LENGTH <= len(text) <= MAX_LENGTH:
        raise InvalidExplanation(f"length {len(text)}")
    if match := BANNED.search(text):
        raise InvalidExplanation(f"banned word: {match.group(0)}")
    if LINK.search(text):
        raise InvalidExplanation("contains a link")
    if "\n-" in text or text.lstrip().startswith(("-", "*", "#")):
        raise InvalidExplanation("not a paragraph")
