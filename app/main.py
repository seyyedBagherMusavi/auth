from __future__ import annotations

from datetime import datetime, timedelta, timezone
from typing import Dict, List, Optional, Set
from uuid import uuid4

from fastapi import Depends, FastAPI, Form, Header, HTTPException, Query, status
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from jose import JWTError, jwt
from passlib.context import CryptContext
from pydantic import BaseModel, Field

app = FastAPI(title="Multi-tenant OAuth2 Auth Server", version="0.1.0")

SECRET_KEY = "change-me"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/oauth2/token")


class Tenant(BaseModel):
    id: str
    name: str


class User(BaseModel):
    id: str
    username: str
    password_hash: str
    tenant_id: str
    tenant_name: str
    roles: Set[str] = Field(default_factory=set)
    permissions: Set[str] = Field(default_factory=set)
    phones: List[str] = Field(default_factory=list)
    national_code: Optional[str] = None
    is_system_user: bool = False


class Session(BaseModel):
    id: str
    user_id: str
    issued_at: datetime
    revoked: bool = False


class ActivityLog(BaseModel):
    id: str
    user_id: str
    tenant_id: str
    action: str
    created_at: datetime
    meta: Dict[str, str] = Field(default_factory=dict)


class RegistrationRequest(BaseModel):
    username: str
    password: str
    tenant_id: str
    phones: List[str]
    national_code: Optional[str] = None
    captcha_token: str


class UserView(BaseModel):
    id: str
    username: str
    tenant_id: str
    tenant_name: str
    roles: List[str]
    permissions: List[str]
    phones: List[str]
    national_code: Optional[str]


class Verify2FARequest(BaseModel):
    challenge_id: str
    code: str
    national_code: Optional[str] = None


class ImpersonationRequest(BaseModel):
    target_user_id: str


tenants: Dict[str, Tenant] = {}
users: Dict[str, User] = {}
roles_to_users: Dict[str, Set[str]] = {}
permissions_to_users: Dict[str, Set[str]] = {}
sessions: Dict[str, Session] = {}
activity_logs: List[ActivityLog] = []
pending_2fa: Dict[str, Dict[str, str]] = {}


def add_log(user: User, action: str, meta: Optional[Dict[str, str]] = None) -> None:
    activity_logs.append(
        ActivityLog(
            id=str(uuid4()),
            user_id=user.id,
            tenant_id=user.tenant_id,
            action=action,
            created_at=datetime.now(timezone.utc),
            meta=meta or {},
        )
    )


def fake_captcha_validate(token: str) -> bool:
    return token == "valid-captcha"


def fake_sms_send(phone: str, code: str) -> None:
    # Hook for your SMS service.
    print(f"SMS sent to {phone}: {code}")


def hash_password(password: str) -> str:
    return pwd_context.hash(password)


def verify_password(password: str, password_hash: str) -> bool:
    return pwd_context.verify(password, password_hash)


def create_access_token(data: dict, expires_delta: Optional[timedelta] = None) -> str:
    to_encode = data.copy()
    expire = datetime.now(timezone.utc) + (expires_delta or timedelta(minutes=15))
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)


def get_user_by_username(username: str, tenant_id: str) -> Optional[User]:
    return next((u for u in users.values() if u.username == username and u.tenant_id == tenant_id), None)


def current_user(token: str = Depends(oauth2_scheme)) -> User:
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id: str = payload.get("sub")
        session_id: str = payload.get("sid")
        if user_id is None or session_id is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
    session = sessions.get(session_id)
    if session is None or session.revoked:
        raise credentials_exception
    user = users.get(user_id)
    if user is None:
        raise credentials_exception
    return user


def bootstrap() -> None:
    tenant = Tenant(id="system", name="System")
    tenants[tenant.id] = tenant
    admin = User(
        id=str(uuid4()),
        username="sysadmin",
        password_hash=hash_password("sysadmin123"),
        tenant_id=tenant.id,
        tenant_name=tenant.name,
        roles={"sys_admin"},
        permissions={"*"},
        phones=["+10000000000"],
        is_system_user=True,
    )
    users[admin.id] = admin
    roles_to_users.setdefault("sys_admin", set()).add(admin.id)


bootstrap()


@app.post("/tenants")
def create_tenant(name: str = Form(...)):
    tenant = Tenant(id=str(uuid4()), name=name)
    tenants[tenant.id] = tenant
    return tenant


@app.post("/registration", response_model=UserView)
def register(payload: RegistrationRequest):
    if not fake_captcha_validate(payload.captcha_token):
        raise HTTPException(status_code=400, detail="Invalid captcha")
    tenant = tenants.get(payload.tenant_id)
    if not tenant:
        raise HTTPException(status_code=404, detail="Tenant not found")
    if get_user_by_username(payload.username, payload.tenant_id):
        raise HTTPException(status_code=409, detail="User already exists")
    if not payload.phones:
        raise HTTPException(status_code=400, detail="At least one phone number is required")
    user = User(
        id=str(uuid4()),
        username=payload.username,
        password_hash=hash_password(payload.password),
        tenant_id=tenant.id,
        tenant_name=tenant.name,
        phones=payload.phones,
        national_code=payload.national_code,
    )
    users[user.id] = user
    add_log(user, "register")
    return UserView(
        id=user.id,
        username=user.username,
        tenant_id=user.tenant_id,
        tenant_name=user.tenant_name,
        roles=sorted(user.roles),
        permissions=sorted(user.permissions),
        phones=user.phones,
        national_code=user.national_code,
    )


@app.post("/oauth2/token")
def login(form_data: OAuth2PasswordRequestForm = Depends(), x_tenant_id: str = Header(...)):
    user = get_user_by_username(form_data.username, x_tenant_id)
    if not user or not verify_password(form_data.password, user.password_hash):
        raise HTTPException(status_code=401, detail="Incorrect username or password")
    challenge_id = str(uuid4())
    code = "123456"
    pending_2fa[challenge_id] = {"user_id": user.id, "code": code}
    fake_sms_send(user.phones[0], code)
    if len(user.phones) > 1 and not user.national_code:
        return {
            "challenge_id": challenge_id,
            "requires_national_code": True,
            "message": "User has multiple phones; national code required",
        }
    return {"challenge_id": challenge_id, "requires_national_code": False, "message": "2FA code sent"}


@app.post("/oauth2/verify")
def verify_2fa(payload: Verify2FARequest):
    challenge = pending_2fa.get(payload.challenge_id)
    if not challenge or challenge["code"] != payload.code:
        raise HTTPException(status_code=401, detail="Invalid challenge or code")
    user = users[challenge["user_id"]]
    if len(user.phones) > 1 and not user.national_code and not payload.national_code:
        raise HTTPException(status_code=400, detail="National code is required")
    if payload.national_code and not user.national_code:
        user.national_code = payload.national_code
    session = Session(id=str(uuid4()), user_id=user.id, issued_at=datetime.now(timezone.utc))
    sessions[session.id] = session
    token = create_access_token(
        {
            "sub": user.id,
            "sid": session.id,
            "username": user.username,
            "tenant_id": user.tenant_id,
            "tenant_name": user.tenant_name,
            "roles": sorted(user.roles),
            "impersonated": False,
        },
        timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES),
    )
    add_log(user, "login")
    del pending_2fa[payload.challenge_id]
    return {"access_token": token, "token_type": "bearer"}


@app.get("/users/me")
def me(user: User = Depends(current_user)):
    add_log(user, "view_profile")
    return {
        "id": user.id,
        "username": user.username,
        "tenant_id": user.tenant_id,
        "tenant_name": user.tenant_name,
        "roles": sorted(user.roles),
        "permissions": sorted(user.permissions),
    }


@app.get("/roles/{role}/users")
def users_for_role(role: str, user: User = Depends(current_user)):
    ids = roles_to_users.get(role, set())
    add_log(user, "list_role_users", {"role": role})
    return [users[u].username for u in ids if u in users]


@app.get("/permissions/{permission}/users")
def users_for_permission(permission: str, user: User = Depends(current_user)):
    ids = permissions_to_users.get(permission, set())
    add_log(user, "list_permission_users", {"permission": permission})
    return [users[u].username for u in ids if u in users]


@app.post("/impersonation/start")
def start_impersonation(payload: ImpersonationRequest, actor: User = Depends(current_user)):
    if "sys_admin" not in actor.roles:
        raise HTTPException(status_code=403, detail="Only system admin can impersonate")
    target = users.get(payload.target_user_id)
    if not target:
        raise HTTPException(status_code=404, detail="Target user not found")
    session = Session(id=str(uuid4()), user_id=target.id, issued_at=datetime.now(timezone.utc))
    sessions[session.id] = session
    token = create_access_token(
        {
            "sub": target.id,
            "sid": session.id,
            "username": target.username,
            "tenant_id": target.tenant_id,
            "tenant_name": target.tenant_name,
            "roles": sorted(target.roles),
            "impersonated": True,
            "impersonator": actor.username,
        },
        timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES),
    )
    add_log(actor, "impersonation_start", {"target": target.username})
    return {"access_token": token, "token_type": "bearer"}


@app.get("/sessions")
def list_sessions(user: User = Depends(current_user)):
    return [s for s in sessions.values() if s.user_id == user.id and not s.revoked]


@app.post("/sessions/revoke-others")
def revoke_other_sessions(current_session_id: str = Form(...), user: User = Depends(current_user)):
    revoked = 0
    for s in sessions.values():
        if s.user_id == user.id and s.id != current_session_id and not s.revoked:
            s.revoked = True
            revoked += 1
    add_log(user, "revoke_other_sessions", {"count": str(revoked)})
    return {"revoked": revoked}


@app.get("/activity-logs")
def get_logs(
    user: User = Depends(current_user),
    action: Optional[str] = Query(default=None),
):
    filtered = [l for l in activity_logs if l.tenant_id == user.tenant_id]
    if action:
        filtered = [l for l in filtered if l.action == action]
    return filtered


@app.get("/reports/users-activity")
def users_activity_report(user: User = Depends(current_user)):
    # In production this should query Elasticsearch indexes.
    per_user_last_login: Dict[str, datetime] = {}
    for log in activity_logs:
        if log.tenant_id == user.tenant_id and log.action == "login":
            per_user_last_login[log.user_id] = max(per_user_last_login.get(log.user_id, log.created_at), log.created_at)
    return [
        {
            "username": users[uid].username,
            "last_login": ts.isoformat(),
        }
        for uid, ts in per_user_last_login.items()
        if uid in users
    ]
