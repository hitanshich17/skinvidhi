from fastapi.testclient import TestClient

from app.llm import LlmError
from app.main import app, get_llm

client = TestClient(app)

FACTS = {
    "skinType": "OILY",
    "concerns": ["breakouts", "dark spots and marks"],
    "reactivity": "SOMETIMES",
    "avoid": [],
    "activesExperience": "A_LITTLE",
    "climate": ["dry air"],
    "treatmentActive": "retinoid",
    "steps": [
        {"time": "AM", "step": "cleanser", "product": "Senka Perfect Whip", "keyIngredients": []},
        {"time": "PM", "step": "treatment", "product": "The INKEY List Starter Retinol Serum",
         "keyIngredients": ["retinoid"]},
    ],
    "notes": ["Use your retinoid at night only."],
}

GOOD = ("Your night treatment is The INKEY List Starter Retinol Serum, because a retinoid is one of the "
        "best-supported options for breakouts. Start every other night, and wear your sunscreen every morning.")


class FakeLlm:
    def __init__(self, answer=GOOD, error=None):
        self.answer, self.error, self.calls = answer, error, []

    def complete(self, system, user, max_tokens):
        self.calls.append((system, user))
        if self.error:
            raise self.error
        return self.answer


def use(llm):
    app.dependency_overrides[get_llm] = lambda: llm


def teardown_function():
    app.dependency_overrides.clear()


def test_returns_the_llm_explanation():
    llm = FakeLlm()
    use(llm)
    response = client.post("/explanations", json=FACTS)
    assert response.status_code == 200
    assert response.json() == {"text": GOOD}


def test_sends_only_the_facts():
    llm = FakeLlm()
    use(llm)
    client.post("/explanations", json=FACTS)
    system, user = llm.calls[0]
    assert "Use only the facts" in system
    assert "Senka Perfect Whip" in user
    assert "pregnan" not in user.lower()


def test_no_llm_configured_is_503():
    use(None)
    assert client.post("/explanations", json=FACTS).status_code == 503


def test_llm_error_is_503():
    use(FakeLlm(error=LlmError("APITimeoutError")))
    assert client.post("/explanations", json=FACTS).status_code == 503


def test_medical_claims_are_rejected():
    use(FakeLlm(answer=GOOD + " This retinoid will cure your acne."))
    assert client.post("/explanations", json=FACTS).status_code == 503


def test_links_and_lists_are_rejected():
    use(FakeLlm(answer=GOOD + " See https://example.com for more."))
    assert client.post("/explanations", json=FACTS).status_code == 503
    use(FakeLlm(answer="- " + GOOD))
    assert client.post("/explanations", json=FACTS).status_code == 503


def test_too_short_is_rejected():
    use(FakeLlm(answer="Use it."))
    assert client.post("/explanations", json=FACTS).status_code == 503


def test_bad_facts_are_422():
    use(FakeLlm())
    assert client.post("/explanations", json={"concerns": []}).status_code == 422
