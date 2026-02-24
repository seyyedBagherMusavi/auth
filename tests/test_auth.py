from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)


def create_tenant(name: str = "Acme"):
    response = client.post("/tenants", data={"name": name})
    assert response.status_code == 200
    return response.json()["id"]


def register_user(tenant_id: str, username: str, phones, national_code=None):
    payload = {
        "username": username,
        "password": "secret",
        "tenant_id": tenant_id,
        "phones": phones,
        "national_code": national_code,
        "captcha_token": "valid-captcha",
    }
    response = client.post("/registration", json=payload)
    assert response.status_code == 200
    return response.json()


def test_registration_and_login_2fa_flow():
    tenant_id = create_tenant()
    register_user(tenant_id, "john", ["+11111111111"])

    response = client.post(
        "/oauth2/token",
        headers={"x-tenant-id": tenant_id},
        data={"username": "john", "password": "secret"},
    )
    assert response.status_code == 200
    challenge = response.json()["challenge_id"]

    verify = client.post("/oauth2/verify", json={"challenge_id": challenge, "code": "123456"})
    assert verify.status_code == 200
    token = verify.json()["access_token"]

    me = client.get("/users/me", headers={"Authorization": f"Bearer {token}"})
    assert me.status_code == 200
    assert me.json()["username"] == "john"


def test_multiple_phones_needs_national_code_when_missing():
    tenant_id = create_tenant("Beta")
    register_user(tenant_id, "jane", ["+1222", "+1333"])

    response = client.post(
        "/oauth2/token",
        headers={"x-tenant-id": tenant_id},
        data={"username": "jane", "password": "secret"},
    )
    assert response.status_code == 200
    assert response.json()["requires_national_code"] is True

    challenge = response.json()["challenge_id"]
    failed = client.post("/oauth2/verify", json={"challenge_id": challenge, "code": "123456"})
    assert failed.status_code == 400

    ok = client.post(
        "/oauth2/verify",
        json={"challenge_id": challenge, "code": "123456", "national_code": "NC-55"},
    )
    assert ok.status_code == 200
